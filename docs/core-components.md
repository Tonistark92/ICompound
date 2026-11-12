# Core components reference

> Every base class of the app, what it does and how to use it. Package paths follow [project-structure.md](project-structure.md); adjust imports to your module names.
> Items marked **assumed** are files that were not reviewed when this document was written (`HeaderInterceptor`, `kVaultModule`).

## 1. `Resource` – the result wrapper

`core/data/model/Resource.kt`

```kotlin
package com.icompound.core.data.model

import com.icompound.core.common.data.model.exception.AppException

sealed class Resource<out Model> {

    /** Loading indicator. `partialData` is optional cached data to show while loading. */
    data class Progress<Model>(val loading: Boolean, val partialData: Model? = null) : Resource<Model>()

    data class Success<out Model>(val model: Model) : Resource<Model>()

    data class Failure(val exception: AppException) : Resource<Nothing>()

    companion object {
        fun <Model> loading(loading: Boolean = true, partialData: Model? = null): Resource<Model> =
            Progress(loading, partialData)

        fun <Model> success(model: Model): Resource<Model> = Success(model)
        fun <Model> failure(exception: AppException): Resource<Model> = Failure(exception)
    }
}
```

**Emission order for a normal call** (`multipleInvoke = false`):
`Progress(true)` → `Success(data)` → `Progress(false)`.
On error: `Progress(true)` → `Failure(e)` → `Progress(false)`.
Always handle all three branches in a `when` (the compiler enforces it).

---

## 2. `AppException` – the only error type the UI sees

`core/data/model/exception/AppException.kt`

```kotlin
package com.icompound.core.data.model.exception

import androidx.annotation.StringRes
import kotlin.reflect.KClass

sealed class AppException(message: String?) : Exception(message) {

    sealed class Network(message: String? = null) : AppException(message) {
        data class Retrial(@StringRes val messageRes: Int, override val message: String?) : Network(message)
        data class Unhandled(@StringRes val messageRes: Int, override val message: String?) : Network(message)
        /** TLS / certificate-pinning failure. */
        data object UntrustedConnection : Network("The connection is not trusted.")
    }

    sealed class Client(message: String? = null) : AppException(message) {
        data object Unauthorized : Client("Unauthorized Access.")
        data object SessionExpired : Client("Session expired. Please sign in again.")

        /** HTTP 422. `errors` = field name → message, ready to show under input fields. */
        data class ResponseValidation(
            val errors: Map<String, String> = emptyMap(),
            override val message: String?,
        ) : Client(message)

        data class Unhandled(val httpErrorCode: Int, override val message: String?) :
            Client("Unhandled client error with code:$httpErrorCode, and the failure reason: $message")
    }

    sealed class Server(message: String? = null) : AppException(message) {
        data class InternalServerError(val httpErrorCode: Int, override val message: String?) :
            Server("Internal server error with code:$httpErrorCode, and the failure reason: $message")
    }

    sealed class Local(message: String? = null) : AppException(message) {
        /** Thrown by `validateRequestContract()` – a developer/input mistake, not a server error. */
        data class RequestValidation(val clazz: KClass<*>, override val message: String? = null) :
            Local(buildString {
                append("There is missing input for this class: ${clazz.simpleName}")
                message?.let { append(", message: $it") }
            })

        data class IOOperation(@StringRes val messageRes: Int, override val message: String? = "") : Local(message)
    }

    data class Unknown(override val message: String?) : AppException(message)

    fun isUnauthorized() = this == Client.Unauthorized
}
```

**HTTP → exception map** (done by `GeneralErrorHandlerImpl`):

| Cause | Result |
|---|---|
| `SSLPeerUnverifiedException` (pinning) | `Network.UntrustedConnection` |
| other `IOException` (no internet, timeout) | `Local.IOOperation` |
| HTTP 401 | `Client.Unauthorized` |
| HTTP 422 | `Client.ResponseValidation(errors, message)` |
| other 4xx | `Client.Unhandled(code, message)` |
| 5xx | `Server.InternalServerError(code, message)` |
| thrown `AppException` (e.g. request validation) | passed through unchanged |
| anything else | `Unknown` |

---

## 3. `ErrorHandler`

`core/domain/error/ErrorHandler.kt`

```kotlin
interface ErrorHandler {
    fun getError(throwable: Throwable): AppException
}
```

`core/data/error/GeneralErrorHandlerImpl.kt`

```kotlin
internal class GeneralErrorHandlerImpl : ErrorHandler {

    override fun getError(throwable: Throwable): AppException {
        AppLogger.e("Mapping error", throwable)               // debug-only, no printStackTrace()
        return when (throwable) {
            is SSLPeerUnverifiedException -> AppException.Network.UntrustedConnection   // BEFORE IOException
            is IOException -> AppException.Local.IOOperation(R.string.error_io_unexpected_message, throwable.message)
            is HttpException -> mapHttpStatusCode(throwable)
            is AppException -> throwable                       // already mapped (e.g. RequestValidation)
            else -> AppException.Unknown(throwable.message)
        }
    }

    private fun mapHttpStatusCode(exception: HttpException): AppException {
        val errorBody = exception.response()?.errorBody()?.string()
        val json = try {
            if (!errorBody.isNullOrBlank()) JSONObject(errorBody) else JSONObject()
        } catch (_: Exception) { JSONObject() }                 // invalid JSON → empty

        val message = extractErrorMessage(json, exception.message())
        val code = json.optInt("code", exception.code())

        return when (exception.code()) {
            401 -> AppException.Client.Unauthorized
            422 -> AppException.Client.ResponseValidation(errors = extractErrorMap(json), message = message)
            in 400..499 -> AppException.Client.Unhandled(code, message)
            in 500..599 -> AppException.Server.InternalServerError(code, message)
            else -> AppException.Unknown(message)
        }
    }

    /** First non-empty of: details, error_description, error, message, title. */
    private fun extractErrorMessage(json: JSONObject, fallback: String?): String? = when {
        json.has("details") -> json.optString("details", fallback)
        json.has("error_description") -> json.optString("error_description", fallback)
        json.has("error") -> json.optString("error", fallback)
        json.has("message") -> json.optString("message", fallback)
        json.has("title") -> json.optString("title", fallback)
        else -> fallback
    }

    /** Expects { "errors": { "email": "Invalid email", "password": "Too short" } } */
    private fun extractErrorMap(json: JSONObject): Map<String, String> {
        if (!json.has("errors")) return emptyMap()
        return try {
            val errorsJson = json.getJSONObject("errors")
            buildMap { errorsJson.keys().forEach { key -> put(key, errorsJson.optString(key)) } }
        } catch (_: Exception) { emptyMap() }
    }
}
```

> If the Icompound backend returns a different error JSON shape, **only this file changes**.

---

## 4. `DispatcherProvider`

`core/presentation/base/DispatcherProvider.kt`

```kotlin
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
}
```

Why: `Dispatchers.Main/IO` hard-coded inside classes cannot be replaced in unit tests. Injecting this lets tests run on a `StandardTestDispatcher`.

---

## 5. `BaseUseCase` – the error boundary

`core/domain/usecase/BaseUseCase.kt`

```kotlin
abstract class BaseUseCase<Domain, in Body>(
    private val errorHandler: ErrorHandler,
    private val dispatchers: DispatcherProvider,
) {
    protected fun requireBody(body: Body?): @UnsafeVariance Body =
        body ?: throw IllegalArgumentException("UseCase body is required and was null")

    /** Implement this: validate → call repo → emit. Exceptions thrown here become Resource.Failure. */
    protected abstract fun executeDS(body: Body? = null): Flow<Domain>

    /** Callback style – call from a ViewModel. */
    operator fun invoke(
        scope: CoroutineScope,
        body: Body? = null,
        multipleInvoke: Boolean = false,
        onResult: (Resource<Domain>) -> Unit,
    ) {
        scope.launch(dispatchers.main) {
            if (!multipleInvoke) onResult(Resource.loading())
            runFlow(executeDS(body), onResult).collect {
                onResult(Resource.success(it))
                if (!multipleInvoke) onResult(Resource.loading(false))
            }
        }
    }

    /** Flow style – easier to test and to combine. */
    operator fun invoke(body: Body? = null, multipleInvoke: Boolean = false): Flow<Resource<Domain>> =
        channelFlow {
            if (!multipleInvoke) send(Resource.loading())
            runFlow(executeDS(body)) { send(it) }.collect {
                send(Resource.success(it))
                if (!multipleInvoke) send(Resource.loading(false))
            }
        }

    private fun <M> runFlow(
        requestExecution: Flow<M>,
        onResult: suspend (Resource<Domain>) -> Unit,
    ): Flow<M> = requestExecution
        .catch { e ->
            onResult(Resource.failure(errorHandler.getError(e)))
            onResult(Resource.loading(false))
        }
        .flowOn(dispatchers.io)          // executeDS runs on IO; results are delivered on the caller (Main)
}
```

**How it behaves**

- `executeDS` runs on **IO**; `onResult` is called on **Main**.
- `multipleInvoke = false` (default): emits loading true/false automatically. Use `true` when the flow emits several values or you manage loading yourself.
- You never write `try/catch` in a use case. Throw (or let the repo throw) and the base class handles it.

**Which base class?**

| Situation | Extend | Body type |
|---|---|---|
| Pre-login call, no inputs (`GetManagerContactUC`) | `BaseUseCase<Domain, Unit>` | `Unit` – call it as `uc(Unit)` |
| Pre-login call with inputs | `BaseUseCase<Domain, XxxRequest>` | a request class |
| The call that returns the token / user | `BaseUseCase` + inject `ISessionRepository` | a request class |
| Any call after login (needs the user id) | `AuthenticatedUseCase<Domain, Body>` | params or a request class |

**Template for every use case**

```kotlin
class XxxUC(
    private val repo: IXxxRepo,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<DomainResult, XxxRequest>(errorHandler, dispatchers) {

    override fun executeDS(body: XxxRequest?): Flow<DomainResult> = flow {
        val request = requireBody(body)
        request.validateRequestContract()          // 1. validate input
        emit(repo.doSomething(request.remoteMap))  // 2. call repo, 3. emit
    }
}
```

---

## 6. Request validation

Three small files in `core/domain/validation/`.

```kotlin
enum class RequestContractType { HEADERS, QUERIES, BODY }

data class RemoteRequest(
    val requestBody: HashMap<String, Any> = hashMapOf(),
    val requestQueries: HashMap<String, Any> = hashMapOf(),
    val requestHeaders: HashMap<String, Any> = hashMapOf(),
)
```

```kotlin
interface IRequestValidation {
    fun isInitialState(): Boolean                       // true = nothing was filled in yet → fail fast
    val remoteMap: RemoteRequest                        // what the repo will send

    /** contractType → (key → isRequired). true = required, false = optional. */
    fun getRequestContracts(): HashMap<RequestContractType, HashMap<String, Boolean>>

    /** Optional keys that actually have a value (useful for building partial updates). */
    fun getOptionalKeys(contractType: RequestContractType): Map<String, Any> {
        val requestContract = getRequestContracts()[contractType] ?: emptyMap()
        val requestMap = remoteMap.requestBody + remoteMap.requestQueries + remoteMap.requestHeaders
        return requestContract
            .filterNot { it.value }
            .mapNotNull { (key, _) -> requestMap[key]?.let { key to it } }
            .toMap()
    }

    fun validateRequestContract() {
        val contract = getRequestContracts()
        if (isInitialState()) throwIsInitialStateException()
        val requestMap = remoteMap.requestBody + remoteMap.requestQueries + remoteMap.requestHeaders
        if (!isRequestSizeValid(contract, requestMap)) throwWhenMapSizeNotValid()
        validateRemoteRequest(contract, requestMap)
    }

    private fun isRequestSizeValid(
        contract: HashMap<RequestContractType, HashMap<String, Boolean>>,
        requestMap: Map<String, Any>,
    ): Boolean {
        val minExpectedSize = contract.values.sumOf { map -> map.count { it.value } }   // required keys
        val maxExpectedSize = contract.values.sumOf { it.size }                         // all keys
        return requestMap.size in minExpectedSize..maxExpectedSize
    }

    private fun validateRemoteRequest(
        contractKeys: HashMap<RequestContractType, HashMap<String, Boolean>>,
        requestBody: Map<String, Any>,
    ) {
        contractKeys.forEach { (contractType, keyMap) ->
            keyMap.forEach { (key, isRequired) ->
                val value = requestBody[key]
                if (isRequired && key !in requestBody) throwWhenMissingKey(contractType, key)
                when (value) {
                    is String, is ByteArray -> {
                        val isEmpty = (value as? String)?.isEmpty() == true || (value as? ByteArray)?.isEmpty() == true
                        if (isRequired && isEmpty) throwWhenKeyHasEmptyValue(contractType, key)   // fixed precedence
                    }
                }
            }
        }
    }

    private fun throwWhenKeyHasEmptyValue(type: RequestContractType, key: String): Nothing =
        throw AppException.Local.RequestValidation(this::class, "Your ${type.name} Request key: $key has an empty value.")

    private fun throwWhenMissingKey(type: RequestContractType, key: String): Nothing =
        throw AppException.Local.RequestValidation(this::class, "Your ${type.name} Request has a missing key: $key.")

    private fun throwWhenMapSizeNotValid(): Nothing =
        throw AppException.Local.RequestValidation(this::class, "Your Remote Request does not meet the minimum contract requirements.")

    private fun throwIsInitialStateException(): Nothing =
        throw AppException.Local.RequestValidation(this::class, "Your Remote Request is in initial state.")
}
```

**What the contract checks**

1. `isInitialState()` is false.
2. Total number of values (body + queries + headers) is between *number of required keys* and *number of all declared keys* → **unexpected extra keys are rejected**.
3. Every required key exists and, if it is a `String`/`ByteArray`, is not empty.

**Gotchas**

- Body, queries and headers are **merged into one map** for validation, so a key must not appear in two of them (e.g. `id` in both queries and body).
- Anything you put in `remoteMap` must be declared in the contract (required or optional), otherwise the size check fails.
- Validation errors surface as `Resource.Failure(AppException.Local.RequestValidation)` – use them to show "fill the form" messages, not server errors.

---

## 7. `BaseDto`, `BaseDomain`, `Mapper`

```kotlin
// core/data/model/dto/BaseDto.kt
open class BaseDto(
    @SerializedName("message") var message: String? = null,
    @SerializedName("code") var code: String? = null,
)

// core/domain/model/BaseDomain.kt   (may change as the Icompound API takes shape)
open class BaseDomain(
    var message: String = "",
    var codeDto: String = "",
)
```

**Rule for DTOs:** Gson ignores Kotlin null-safety, so **every DTO field is nullable with a default**. The mapper supplies real defaults (`?: ""`). Domain models are non-null and clean.

```kotlin
// core/data/mapper/Mapper.kt
internal abstract class Mapper<Dto, Domain, Entity> {
    abstract fun dtoToDomain(model: Dto): Domain                                  // required

    open fun domainToDto(model: Domain): Dto = throw NotImplementedError("override and implement this method")
    open fun entityToDomain(model: Entity): Domain = throw NotImplementedError("override and implement this method")
    open fun domainToEntity(model: Domain): Entity = throw NotImplementedError("override and implement this method")
    open fun entityToDto(model: Entity): Dto = throw NotImplementedError("override and implement this method")
    open fun dtoToEntity(model: Dto): Entity = throw NotImplementedError("override and implement this method")

    // List helpers (null list → empty list)
    fun dtoToDomain(list: List<Dto>?): List<Domain> = (list ?: emptyList()).map(::dtoToDomain)
    fun domainToDto(list: List<Domain>?): List<Dto> = (list ?: emptyList()).map(::domainToDto)
    fun entityToDomain(list: List<Entity>): List<Domain> = list.map(::entityToDomain)
    fun domainToEntity(list: List<Domain>): List<Entity> = list.map(::domainToEntity)
    fun entityToDto(list: List<Entity>): List<Dto> = list.map(::entityToDto)
    fun dtoToEntity(list: List<Dto>): List<Entity> = list.map(::dtoToEntity)

    /** Copies `message` / `code` from the DTO into the domain object. */
    protected fun BaseDto.mapBaseFields(target: BaseDomain) {
        target.message = message ?: ""
        target.codeDto = code ?: ""
    }
}
```

Only override what you need. No database for the feature? Use `Unit` as the `Entity` type: `Mapper<XxxDto, Xxx, Unit>()`.

---

## 8. Network layer

#### `INetworkProvider` (domain contract)

`core/domain/repository/remote/INetworkProvider.kt`

```kotlin
interface INetworkProvider {
    suspend fun <ResponseBody, RequestBody> delete(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: RequestBody? = null): ResponseBody
    suspend fun <ResponseBody, RequestBody> post(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: RequestBody? = null): ResponseBody
    suspend fun <ResponseBody> postFormData(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: Map<String, Any>? = null): ResponseBody
    suspend fun <ResponseBody, RequestBody> put(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: RequestBody? = null): ResponseBody
    suspend fun <ResponseBody, RequestBody> patch(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: RequestBody? = null): ResponseBody
    suspend fun <ResponseBody, RequestBody> postWithHeaderResponse(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null, requestBody: RequestBody? = null): Pair<ResponseBody, Map<String, String>>
    suspend fun <ResponseBody> get(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null): ResponseBody
    suspend fun <ResponseBody> getWithHeaderResponse(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>? = null, queryParams: Map<String, Any>? = null): Pair<ResponseBody, Map<String, String>>
    suspend fun <ResponseBody, RequestBody> postWithImagesFile(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?, queryParams: Map<String, Any>?, requestBody: RequestBody?, files: HashMap<String, File>): ResponseBody
    suspend fun <ResponseBody, RequestBody> putWithImagesFile(responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?, queryParams: Map<String, Any>?, requestBody: RequestBody?, files: HashMap<String, File>): ResponseBody
}
```

**How to call it from a repo**

- `responseWrappedModel` = the DTO type the JSON is parsed into: `MyDto::class.java`, or for generics `object : TypeToken<ApiList<MyDto>>() {}.type`.
- `pathUrl` = path **without** the base URL, no leading slash (`"maintenance/tickets"`). The base URL must end with `/`.
- `headers` / `queryParams` / `requestBody` come straight from `RemoteRequest`.
- The generic parameters are `<ResponseBody>` (what you get back) and `<RequestBody>` (what you send).
- HTTP errors are thrown as `HttpException` and handled by the use case. **Do not catch them in the repo.**

#### JSON helpers (`core/android/extentions/JsonExtensions.kt`)

```kotlin
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type

fun <M> M.toJson(): String = Gson().toJson(this)

fun <M> String.getModelFromJSON(tokenType: Type): M = Gson().fromJson(this, tokenType)

fun <M> JSONObject.getModelFromJSON(tokenType: Type): M =
    this.toString().getModelFromJSON(tokenType)

fun <M> String.getListOfModelFromJSON(tokenType: Type): ArrayList<M> =
    Gson().fromJson(this, tokenType)

fun <M> JSONArray.getListOfModelFromJSON(tokenType: Type): ArrayList<M> =
    this.toString().getListOfModelFromJSON(tokenType)

fun JSONArray.jsonArrayToHashMap(): HashMap<String, String> {
    val hashMap = HashMap<String, String>()
    for (i in 0 until length()) {
        val jsonObject = getJSONObject(i)
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.optString(key, "")
            hashMap[key] = value
        }
    }
    return hashMap
}
```

**How they are used**

| Function | Use |
|---|---|
| `String.getModelFromJSON<M>(type)` | Parse a response body into a DTO. Called by `RetrofitNetworkProvider` for every call. |
| `Any.toJson()` | Serialize a request body (used by the multipart upload functions). |
| `String.getListOfModelFromJSON<M>(type)` | Parse a JSON array into `ArrayList<M>`; `type` = `object : TypeToken<ArrayList<MyDto>>() {}.type`. |
| `JSONObject` / `JSONArray` overloads | Same, when you already hold an `org.json` object (e.g. inside `GeneralErrorHandlerImpl`). |
| `JSONArray.jsonArrayToHashMap()` | Flattens `[{"k":"v"}, ...]` into one map. If two objects share a key, the later one wins. |

**Things to know**

- `getModelFromJSON` returns `null` (Gson) for an empty body. The return type is non-null `M`, so an empty response causes a failure later (mapped to `Unknown`). For endpoints that answer `204`/empty, don't use these helpers; follow the `Unit` pattern in `putWithImagesFile`.
- **Recommended improvement:** every call creates a new `Gson()`. That is wasteful and it ignores any customised `Gson` registered in Koin (date formats, adapters). Use one shared instance:

```kotlin
// JsonExtensions.kt
internal val appGson: Gson = GsonBuilder().create()

fun <M> M.toJson(): String = appGson.toJson(this)
fun <M> String.getModelFromJSON(tokenType: Type): M = appGson.fromJson(this, tokenType)
fun <M> String.getListOfModelFromJSON(tokenType: Type): ArrayList<M> = appGson.fromJson(this, tokenType)

// NetworkModule.kt
single { appGson }          // instead of GsonBuilder().create()
```

#### `ApiService` (Retrofit – one generic interface for the whole app)

`core/data/repository/remote/ApiService.kt`

```kotlin
interface ApiService {

    // Changed: Retrofit rejects @Body on @DELETE, so a DELETE with a body must use @HTTP(hasBody = true).
    @HTTP(method = "DELETE", path = "{pathUrl}", hasBody = true)
    @JvmSuppressWildcards
    suspend fun delete(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): ResponseBody

    @POST("{pathUrl}") @JvmSuppressWildcards
    suspend fun post(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): ResponseBody

    @FormUrlEncoded @POST("{pathUrl}") @JvmSuppressWildcards
    suspend fun postFormData(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @FieldMap fields: Map<String, @JvmSuppressWildcards Any>,
        @HeaderMap headerMap: Map<String, Any> = emptyMap(),
        @QueryMap queryParams: Map<String, Any> = emptyMap(),
    ): ResponseBody

    @PUT("{pathUrl}") @JvmSuppressWildcards
    suspend fun put(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): ResponseBody

    @PATCH("{pathUrl}") @JvmSuppressWildcards
    suspend fun patch(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): ResponseBody

    @POST("{pathUrl}") @JvmSuppressWildcards
    suspend fun postWithHeaderResponse(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): Response<ResponseBody>

    @GET("{pathUrl}") @JvmSuppressWildcards
    suspend fun get(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): ResponseBody

    @GET("{pathUrl}") @JvmSuppressWildcards
    suspend fun getWithHeaderResponse(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): Response<ResponseBody>

    // NOTE: currently unused (kept as-is for now). The two @Url functions below are the ones used by the provider.
    @Multipart @POST("{pathUrl}") @JvmSuppressWildcards
    suspend fun postWithImagesFile(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @Part files: List<MultipartBody.Part>, @Part("data") requestBody: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>,
    ): Response<ResponseBody>

    @Multipart @POST
    suspend fun postMultipart(
        @Url pathUrl: String, @HeaderMap headerMap: Map<String, String>, @QueryMap queryParams: Map<String, String>,
        @Part("body") body: RequestBody?, @Part files: List<MultipartBody.Part>,
    ): Response<ResponseBody>

    @Multipart @PUT
    suspend fun putMultipart(
        @Url pathUrl: String, @HeaderMap headerMap: Map<String, String>, @QueryMap queryParams: Map<String, String>,
        @Part("body") body: RequestBody?, @Part files: List<MultipartBody.Part>,
    ): Response<ResponseBody>
}
```

You normally never edit `ApiService`: new endpoints are just new `pathUrl` strings in a repo.

#### `RetrofitNetworkProvider`

`core/data/repository/remote/RetrofitNetworkProvider.kt`

```kotlin
class RetrofitNetworkProvider(private val apiService: ApiService) : INetworkProvider {

    override suspend fun <ResponseBody, RequestBody> post(
        responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?,
        queryParams: Map<String, Any>?, requestBody: RequestBody?,
    ): ResponseBody {
        val response = apiService.post(
            pathUrl = pathUrl, headerMap = headers ?: hashMapOf(),
            queryParams = queryParams ?: hashMapOf(), body = requestBody ?: Unit,
        )
        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody> get(
        responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?, queryParams: Map<String, Any>?,
    ): ResponseBody {
        val response = apiService.get(
            pathUrl = pathUrl, headerMap = headers ?: hashMapOf(), queryParams = queryParams ?: hashMapOf(),
        )
        return response.string().getModelFromJSON(responseWrappedModel)
    }

    // delete / put / patch / postFormData follow exactly the same shape as post()/get():
    //   call apiService.<verb>(...) with `headers ?: hashMapOf()`, `queryParams ?: hashMapOf()`,
    //   `requestBody ?: Unit`, then `response.string().getModelFromJSON(responseWrappedModel)`.

    override suspend fun <ResponseBody, RequestBody> postWithHeaderResponse(
        responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?,
        queryParams: Map<String, Any>?, requestBody: RequestBody?,
    ): Pair<ResponseBody, Map<String, String>> {
        val response = apiService.postWithHeaderResponse(
            pathUrl = pathUrl, headerMap = headers ?: hashMapOf(),
            queryParams = queryParams ?: hashMapOf(), body = requestBody ?: Unit,
        )
        return when {
            response.isSuccessful -> Pair(
                (response.body() ?: "".toResponseBody()).string().getModelFromJSON(responseWrappedModel),
                response.headers().toMap(),
            )
            else -> throw HttpException(response)
        }
    }
    // getWithHeaderResponse: identical idea with apiService.getWithHeaderResponse(...)

    /**
     * Changed: the response body can only be read ONCE. The old postWithImagesFile called
     * `response.body()?.string()` twice (in the `when` condition and again in the branch), so the
     * second read returned nothing. Read it once into `raw`, exactly like putWithImagesFile does.
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun <ResponseBody, RequestBody> postWithImagesFile(
        responseWrappedModel: Type, pathUrl: String, headers: Map<String, Any>?,
        queryParams: Map<String, Any>?, requestBody: RequestBody?, files: HashMap<String, File>,
    ): ResponseBody {
        val multipartFiles = files.map { (attributeName, file) -> file.asMultipart(attributeName) }
        val jsonRequestBody = requestBody?.toJson()?.toRequestBody("application/json".toMediaTypeOrNull())
        val headersString = headers?.mapValues { it.value.toString() } ?: emptyMap()
        val queryParamsString = queryParams?.mapValues { it.value.toString() } ?: emptyMap()

        val response = apiService.postMultipart(
            pathUrl = pathUrl, headerMap = headersString, queryParams = queryParamsString,
            body = jsonRequestBody, files = multipartFiles,
        )

        val raw = response.body()?.string()          // read once
        return when {
            responseWrappedModel == Nothing::class.java || response.code() == 204 || raw.isNullOrBlank() ->
                Unit as ResponseBody
            response.isSuccessful ->
                raw.getModelFromJSON(responseWrappedModel) ?: throw IOException("Empty response body after parsing -> $raw")
            else -> throw HttpException(response)
        }
    }
    // putWithImagesFile: unchanged – same pattern using apiService.putMultipart(...)

    private fun File.asMultipart(partName: String): MultipartBody.Part =
        MultipartBody.Part.createFormData(partName, name, asRequestBody("image/*".toMediaTypeOrNull()))
}
```

(The commented-out old `postWithImagesFile` from the original file should be deleted, not kept.)

---

## 9. Auth: tokens, interceptor, Authenticator, pinning

How a 401 is handled end to end:

```
request ──▶ HeaderInterceptor adds "Authorization: Bearer <access>"
        ◀── 401
        ──▶ TokenAuthenticator: (a) skip if login/refresh call, (b) give up after 2 tries,
                                 (c) if another thread already refreshed → retry with new token,
                                 (d) else call refresh API (blocking), save tokens, retry,
                                 (e) on failure → clear tokens + SessionEvents.notifyExpired() → app goes to login
```

#### `TokenStore` and `SessionEvents`

```kotlin
interface TokenStore {
    val accessToken: String?
    val refreshToken: String?
    fun save(access: String, refresh: String?)
    fun clear()
}

/** KVault implementation (assumed API – adjust to your KVault version). */
class KVaultTokenStore(private val vault: KVault) : TokenStore {
    override val accessToken: String? get() = vault.string(forKey = "access_token")
    override val refreshToken: String? get() = vault.string(forKey = "refresh_token")
    override fun save(access: String, refresh: String?) {
        vault.set("access_token", access)
        refresh?.let { vault.set("refresh_token", it) }
    }
    override fun clear() { vault.deleteObject("access_token"); vault.deleteObject("refresh_token") }
}

/** Lets the network layer say "go back to login" without knowing about UI. */
class SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired: SharedFlow<Unit> = _expired
    fun notifyExpired() { _expired.tryEmit(Unit) }
}
```

In each app's root (Activity / root composable) collect `sessionEvents.expired` once and navigate to login, clearing the back stack.

#### `HeaderInterceptor` (assumed – check against the real file)

```kotlin
class HeaderInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder().header("Accept", "application/json")
        // Login / refresh calls carry the marker header and must NOT get a bearer token.
        if (request.header(TokenAuthenticator.NO_AUTH_HEADER) == null) {
            tokenStore.accessToken?.let { builder.header("Authorization", "Bearer $it") }
        }
        return chain.proceed(builder.build())
    }
}
```

> Do **not** strip the marker header here: OkHttp application interceptors run *before* the retry/authenticator stage, so the `Authenticator` must still see the marker on `response.request`. The server ignores the unknown header.

#### Refresh API and `TokenAuthenticator`

```kotlin
interface AuthRefreshApi {
    // PLACEHOLDER: replace path / body / response with the real Icompound backend contract.
    @POST("auth/refresh")
    fun refresh(@Body body: RefreshRequest): Call<RefreshResponse>      // blocking Call: Authenticator is synchronous
}
data class RefreshRequest(@SerializedName("refresh_token") val refreshToken: String)
data class RefreshResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
)
```

```kotlin
class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val refreshApi: AuthRefreshApi,
    private val sessionEvents: SessionEvents,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header(NO_AUTH_HEADER) != null) return null     // login/refresh: a 401 is a real answer
        if (response.request.header("Authorization").isNullOrBlank()) return null   // never had a token → nothing to refresh
        if (response.responseCount() >= 2) return expire()                    // already retried once

        synchronized(this) {
            val sentToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            val currentToken = tokenStore.accessToken

            // Another request refreshed while we waited for the lock → just retry with the new token.
            if (!currentToken.isNullOrBlank() && currentToken != sentToken) return response.request.withToken(currentToken)

            val refreshToken = tokenStore.refreshToken ?: return expire()
            val refreshed = runCatching { refreshApi.refresh(RefreshRequest(refreshToken)).execute() }
                .getOrNull()?.takeIf { it.isSuccessful }?.body() ?: return expire()

            tokenStore.save(refreshed.accessToken, refreshed.refreshToken ?: refreshToken)
            return response.request.withToken(refreshed.accessToken)
        }
    }

    private fun expire(): Request? { tokenStore.clear(); sessionEvents.notifyExpired(); return null }

    private fun Request.withToken(token: String) = newBuilder().header("Authorization", "Bearer $token").build()

    private fun Response.responseCount(): Int {
        var count = 0; var r: Response? = this
        while (r != null) { count++; r = r.priorResponse }
        return count
    }

    companion object { const val NO_AUTH_HEADER = "X-No-Auth" }
}
```

**Using the marker:** requests that must not carry a token (login, token generation, refresh) put `"X-No-Auth" to "true"` into `requestHeaders` and declare it in the contract (see `CheckNumberRequest` in [adding-a-feature.md](adding-a-feature.md)).

> If the backend has no refresh token and issues tokens from api key + secret only, "refresh" = call the token endpoint again. Adapt `AuthRefreshApi` accordingly.

#### Certificate pinning

Get the pin (do it for the intermediate CA or leaf, plus a **backup**):

```bash
openssl s_client -servername api.icompound.com -connect api.icompound.com:443 </dev/null 2>/dev/null \
 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der \
 | openssl dgst -sha256 -binary | openssl enc -base64
```

`build.gradle` (per flavor / build type):

```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.icompound.com/\"")
buildConfigField("String", "PIN_PRIMARY", "\"sha256/AAAA...=\"")
buildConfigField("String", "PIN_BACKUP",  "\"sha256/BBBB...=\"")
```

```kotlin
private fun certificatePinner(): CertificatePinner =
    CertificatePinner.Builder()
        .add(BuildConfig.BASE_URL.toHttpUrl().host, BuildConfig.PIN_PRIMARY, BuildConfig.PIN_BACKUP)
        .build()
```

Rules: always ship **two pins**; pinning is applied only in **release** so Charles/Proxyman work in debug; keep a calendar reminder for certificate renewal. A pinning failure arrives in the UI as `AppException.Network.UntrustedConnection`.

---

## 10. Koin modules

`core/di/NetworkModule.kt`

```kotlin
private const val REFRESH = "refresh"

internal val networkModule = module {
    single { GsonBuilder().create() }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single { SessionEvents() }
    single<ApiService> { get<Retrofit>().create(ApiService::class.java) }
    single<INetworkProvider> { RetrofitNetworkProvider(get()) }

    // Refresh client: NO authenticator (a failing refresh must not trigger another refresh), same pinning.
    single<OkHttpClient>(named(REFRESH)) { baseClient(androidContext()).build() }
    single<AuthRefreshApi> {
        Retrofit.Builder()
            .client(get(named(REFRESH)))
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
            .create(AuthRefreshApi::class.java)
    }
    single { TokenAuthenticator(get(), get(), get()) }

    // Main client
    single<OkHttpClient> {
        baseClient(androidContext())
            .addInterceptor(HeaderInterceptor(get()))
            .authenticator(get<TokenAuthenticator>())
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .client(get<OkHttpClient>())
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
}

private fun baseClient(context: Context): OkHttpClient.Builder =
    OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024))   // was a 1 KB cache in a random temp dir
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    redactHeader("Authorization"); redactHeader("Cookie")
                })
            } else {
                certificatePinner(certificatePinner())
            }
        }
```

`core/di/FeaturesModule.kt` and `CoreDiModule.kt`

Each **feature** owns one public Koin module in its `di/` package (see [adding-a-feature.md](adding-a-feature.md)); each **app** assembles them:

```kotlin
// core
val coreDiModule = module {
    includes(networkModule, kVaultModule, sessionDIModule)
    single<ErrorHandler> { GeneralErrorHandlerImpl() }
}

// feature: phone_auth/di/PhoneAuthModule.kt
val phoneAuthModule = module {
    factory { ManagerContactMapper() }
    single<IPhoneAuthRepository> { PhoneAuthRepository(get(), get()) }
    factoryOf(::GetManagerContactUC)
    viewModelOf(::PhoneAuthViewModel)          // ViewModels are NEVER `single` – they must follow the screen lifecycle
}

// app
class IcompoundAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@IcompoundAdminApp)
            modules(coreDiModule, phoneAuthModule /*, maintenanceModule, billingModule, ... */)
        }
    }
}
```

`kVaultModule` (assumed) must provide `single<TokenStore> { KVaultTokenStore(get()) }`.

**Koin cheat sheet:** repositories `single`, mappers `factory`, use cases `factoryOf(::XxxUC)`, ViewModels `viewModelOf(::XxxViewModel)`.

---

## 11. `AppLogger`

```kotlin
object AppLogger {
    private const val TAG = "ICOMPOUND"
    var isDebuggable = BuildConfig.DEBUG
    fun d(message: String) { if (isDebuggable) Log.d(TAG, message) }
    fun i(message: String) { if (isDebuggable) Log.i(TAG, message) }
    fun w(message: String) { if (isDebuggable) Log.w(TAG, message) }
    fun e(message: String, throwable: Throwable? = null) { if (isDebuggable) Log.e(TAG, message, throwable) }
}
```

All logging is debug-only. **Never log tokens, passwords, phone numbers or resident data**, even in debug (state objects are logged by `MVIViewModel`, so keep secrets out of `ViewState`; override `toString()` to mask a field if you must hold one).

---

## 12. MVI presentation

#### Contract markers and `MVIViewModel`

`core/presentation/base/MVIViewModel.kt`

```kotlin
interface ViewAction     // what the user/UI asks for
interface ViewEvent      // one-shot effects: navigate, snackbar, toast
interface ViewState      // everything needed to draw the screen

abstract class MVIViewModel<A : ViewAction, E : ViewEvent, S : ViewState>(
    private val initialState: S,
    protected val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<S> = _viewState.asStateFlow()

    private val eventChannel = Channel<E>(Channel.UNLIMITED)
    val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    private val _actionFlow = MutableSharedFlow<A>(extraBufferCapacity = Int.MAX_VALUE)

    val currentViewState: S get() = viewState.value

    init { viewModelScope.launch { _actionFlow.collect { onActionTrigger(it) } } }

    /** The ONLY function the UI calls. */
    fun processIntent(action: A) {
        AppLogger.d("Received action: $action")
        if (!_actionFlow.tryEmit(action)) AppLogger.e("Failed to emit action: $action")   // was check() → crash
    }

    protected fun sendEvent(event: E) { AppLogger.d("Emitting single event: $event"); eventChannel.trySend(event) }

    fun setState(newState: S) { _viewState.value = newState }

    /** Atomic (compare-and-set), safe from several coroutines at once. */
    protected fun updateState(transform: S.() -> S) {
        _viewState.update { it.transform() }
        AppLogger.d("Updated state: ${_viewState.value}")
    }

    /**
     * Collects a Flow-style use case in a cancellable Job and routes every Resource.
     * Keep the returned Job when the work must be cancelled (back pressed, resend, new search).
     */
    protected fun <T> Flow<Resource<T>>.launchResult(
        onProgress: (Boolean) -> Unit = {},
        onFailure: (AppException) -> Unit = {},
        onSuccess: (T) -> Unit,
    ): Job = viewModelScope.launch {
        collect {
            when (it) {
                is Resource.Progress -> onProgress(it.loading)
                is Resource.Success -> onSuccess(it.model)
                is Resource.Failure -> onFailure(it.exception)
            }
        }
    }

    protected abstract fun onActionTrigger(action: A)

    override fun onCleared() { super.onCleared(); eventChannel.close() }
}
```

#### Writing a screen's contract

```kotlin
internal sealed interface PhoneAuthAction : ViewAction { data object SendCodeClicked : PhoneAuthAction }
internal sealed interface PhoneAuthEvent : ViewEvent { data object NavigateToHome : PhoneAuthEvent }
internal data class PhoneAuthState(
    val isLoading: Boolean = false,
    val error: AppException? = null,
) : ViewState
```

Rules: `Action` = verbs the user does; `Event` = effects that must happen **once**; `State` = defaults for everything so `State()` is a valid initial state.

#### Handling `Resource` in the ViewModel (the standard pattern)

```kotlin
useCase(scope = viewModelScope, body = request) { result ->
    when (result) {
        is Resource.Progress -> updateState { copy(isLoading = result.loading) }
        is Resource.Success  -> { updateState { copy(isLoading = false) }; /* map data into state or sendEvent */ }
        is Resource.Failure  -> updateState { copy(isLoading = false, error = result.exception) }
    }
}
```

#### Compose screen pattern

```kotlin
@Composable
internal fun PhoneAuthRoute(
    onNavigateHome: () -> Unit,
    viewModel: PhoneAuthViewModel = koinViewModel(),
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.singleEvent.collect { event ->
            when (event) { PhoneAuthEvent.NavigateToHome -> onNavigateHome() }
        }
    }
    LaunchedEffect(viewModel) { viewModel.processIntent(PhoneAuthAction.SendCodeClicked) }

    PhoneAuthScreen(state = state, onRetry = { viewModel.processIntent(PhoneAuthAction.SendCodeClicked) })
}

@Composable
private fun PhoneAuthScreen(state: PhoneAuthState, onRetry: () -> Unit) {   // stateless → previewable
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.error.asMessage())
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}
```

#### Showing errors to the user

Never show raw `exception.message` (it can contain technical text). Map once, reuse everywhere:

```kotlin
@Composable
fun AppException.asMessage(): String = when (this) {
    is AppException.Local.IOOperation          -> stringResource(messageRes)
    AppException.Client.SessionExpired         -> stringResource(R.string.error_session_expired)
    AppException.Network.UntrustedConnection   -> stringResource(R.string.error_untrusted_connection)
    is AppException.Client.ResponseValidation  -> errors.values.firstOrNull() ?: stringResource(R.string.error_invalid_data)
    is AppException.Local.RequestValidation    -> stringResource(R.string.error_fill_required_fields)
    is AppException.Server                     -> stringResource(R.string.error_server)
    else                                       -> stringResource(R.string.error_unknown)
}
```

For form screens use `(error as? AppException.Client.ResponseValidation)?.errors?.get("email")` to show per-field errors.

## 13. Extensions (`core/android/extentions/Extensions.kt`)

```kotlin
fun Date.getCurrentDateTimeUTC(pattern: String): String {
    val dateFormatGmt = SimpleDateFormat(pattern, Locale.US)
    dateFormatGmt.timeZone = TimeZone.getTimeZone("UTC")
    return dateFormatGmt.format(this)
}

fun Double.formatDecimalByPattern(pattern: String = "#00.00"): String {
    val decimalFormat = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
    return decimalFormat.format(this)
}

fun ByteArray.base64Encode(): String = Base64.encodeToString(this, Base64.NO_WRAP)
fun String.base64Decode(): ByteArray = Base64.decode(this, Base64.NO_WRAP)

fun ByteArray.clear() { fill(0) }
fun CharArray.clear() { fill('0') }

fun Editable.toCharArray(): CharArray {
    val chars = CharArray(length)
    for (i in indices) chars[i] = get(i)
    return chars
}

fun Long.getRemainingTime(): String {
    val hours: Int = ((this / 1000) / 3600).toInt()
    val minutes: Int = (((this / 1000) % 3600) / 60).toInt()
    val seconds: Int = ((this / 1000) % 60).toInt()
    return if (hours > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
```

| Function | What it does / how to use it |
|---|---|
| `Date.getCurrentDateTimeUTC(pattern)` | Formats **the receiver date** in UTC, e.g. `Date().getCurrentDateTimeUTC("yyyy-MM-dd'T'HH:mm:ss'Z'")` for "now". Despite the name, it does not read the current time by itself. |
| `Double.formatDecimalByPattern(pattern)` | Locale-independent number formatting. The default `"#00.00"` pads the integer part to two digits (`5.5` → `05.50`); pass `"#0.00"` if you want `5.50`. |
| `ByteArray.base64Encode()` / `String.base64Decode()` | Base64 without line wraps (safe for headers and JSON). |
| `ByteArray.clear()` / `CharArray.clear()` | Wipe sensitive data (passwords, keys) from memory after use. Prefer `'\u0000'` over `'0'` in `CharArray.clear()` – it works either way, but a null char is the conventional wipe value. |
| `Editable.toCharArray()` | Reads an `EditText` value as `CharArray` so passwords never become a `String` in memory; call `clear()` when done. |
| `Long.getRemainingTime()` | Milliseconds → `mm:ss` or `h:mm:ss` countdown text (OTP timers, session timers). |

---

## 14. Session and `AuthenticatedUseCase`

**Problem:** after login almost every endpoint needs the user id (and the token must be attached to every call).

**Rules**

1. The **token** lives only in `TokenStore` (encrypted). `HeaderInterceptor` adds `Authorization: Bearer …` to every call and `TokenAuthenticator` reacts to a 401. **Use cases never read or attach the token** (`token()` exists only for an endpoint that insists on it as a parameter).
2. The **user id** and profile live in preferences, behind `ISessionRepository`. Use cases that need the id extend `AuthenticatedUseCase` and call `userId()`.
3. The **use case that returns the token/user** (verify OTP / login) calls `session.saveSession(user)` *before* emitting, so the next request already carries the token.
4. The profile JSON is stored **without the token**.
5. Logout / expired session → `session.clearSession()`.

```kotlin
package com.icompound.core.domain.repo.local

import com.icompound.core.domain.model.User

/**
 * The logged-in session.
 * Token   -> TokenStore (encrypted; read synchronously by HeaderInterceptor / TokenAuthenticator).
 * User id -> preferences, for endpoints that take the id as a parameter.
 * Profile -> preferences (without the token).
 */
interface ISessionRepository {
    /** Persist everything after a successful login. Completes before the login use case emits. */
    suspend fun saveSession(user: User)

    /** Current user id, or throws AppException.Client.Unauthorized when nobody is logged in. */
    suspend fun requireUserId(): String

    /** Current access token, or throws Unauthorized. Only for the rare endpoint that wants the token as a parameter;
     *  normal calls get "Authorization: Bearer ..." from HeaderInterceptor automatically. */
    fun requireToken(): String

    /** Logout / session expired. */
    suspend fun clearSession()
}
```

```kotlin
package com.icompound.core.data.repo.local

import com.icompound.core.data.model.exception.AppException          // adjust to your package
import com.icompound.core.domain.model.User
import com.icompound.core.domain.repo.local.IPreferenceRepository
import com.icompound.core.domain.repo.local.ISessionRepository
import com.icompound.core.network.auth.TokenStore                      // adjust to your package
import com.icompound.core.util.toJson
import kotlinx.coroutines.flow.first

internal class SessionRepositoryImpl(
    private val tokenStore: TokenStore,
    private val preferences: IPreferenceRepository,
) : ISessionRepository {

    override suspend fun saveSession(user: User) {
        // id and token are nullable in User. Never store "null" / "" as a session.
        val id = user.id?.takeIf { it.isNotBlank() }
            ?: throw AppException.Unknown("Login response has no user id")
        val token = user.token?.takeIf { it.isNotBlank() }
            ?: throw AppException.Unknown("Login response has no token")

        // Token ONLY in the encrypted TokenStore: the interceptor adds it to every request and the
        // authenticator manages it there (one source of truth). The backend returns no refresh token
        // today, so a 401 means "session expired" (TokenAuthenticator already handles a missing refresh token).
        tokenStore.save(access = token, refresh = null)

        preferences.setUserId(id)
        preferences.setUserData(user.copy(token = null).toJson())      // never write the token into plain prefs
        preferences.setIsLogin(true)
    }

    override suspend fun requireUserId(): String =
        preferences.getUserId().first().ifBlank { throw AppException.Client.Unauthorized }

    override fun requireToken(): String =
        tokenStore.accessToken?.takeIf { it.isNotBlank() } ?: throw AppException.Client.Unauthorized

    override suspend fun clearSession() {
        tokenStore.clear()
        preferences.setUserId("")
        preferences.setUserData("")
        preferences.setIsLogin(false)
    }
}
```

```kotlin
package com.icompound.core.domain.usecase

import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.repo.local.ISessionRepository
import com.icompound.core.presentation.base.DispatcherProvider
```

Example: the use case that creates the session.

```kotlin
package com.icompound.core.features.phoneauth.domain.interactor

import com.icompound.core.domain.model.User
import com.icompound.core.domain.model.error.ErrorHandler
import com.icompound.core.domain.repo.local.ISessionRepository
import com.icompound.core.domain.usecase.BaseUseCase
import com.icompound.core.features.phoneauth.domain.model.request.VerifyOtpRequest   // you will create this one
import com.icompound.core.features.phoneauth.domain.repository.IPhoneAuthRepo
import com.icompound.core.presentation.base.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * EXAMPLE of where saving the session belongs: the use case that RETURNS the token and user
 * (verify OTP / login), not GetManagerContactUC (which runs before login and has nothing to save).
 */
class VerifyOtpUC(
    private val repo: IPhoneAuthRepo,                 // needs: suspend fun verifyOtp(request: RemoteRequest): User
    private val session: ISessionRepository,
    errorHandler: ErrorHandler,
    dispatchers: DispatcherProvider,
) : BaseUseCase<User, VerifyOtpRequest>(errorHandler, dispatchers) {

    override fun executeDS(body: VerifyOtpRequest?): Flow<User> = flow {
        val request = requireBody(body)
        request.validateRequestContract()
        val user = repo.verifyOtp(request.remoteMap)
        session.saveSession(user)        // suspend, finishes BEFORE we emit, so the next request already has the token
        emit(user)
    }
}
```

Path parameters (such as the user id) are passed as **function arguments**, not inside `RemoteRequest` (which only has body / queries / headers):

```kotlin
// IBillingRepository
suspend fun getBills(userId: String, request: RemoteRequest): Bills
// BillingRepository
network.get<BillsResponseDto>(BillsResponseDto::class.java, "users/$userId/bills", request.requestHeaders, request.requestQueries)
```

---

## 15. Flow-style use case + `launchResult` (preferred in ViewModels)

The callback form `useCase(scope, body) { … }` cannot be cancelled. The Flow form returns a `Flow<Resource<T>>` you collect in a `Job`, so back / resend / retry can cancel work in flight:

```kotlin
private var managerJob: Job? = null

private fun loadManager() {
    if (managerJob?.isActive == true) return
    updateState { copy(manager = ManagerContactState.Loading) }
    managerJob = getManagerContact(Unit).launchResult(
        onFailure = { updateState { copy(manager = ManagerContactState.Failed) } },
    ) { contact -> updateState { copy(manager = ManagerContactState.Loaded(contact)) } }
}
```

Use `onProgress` when a spinner should follow the request; ignore it when the screen shows its own loading state.
