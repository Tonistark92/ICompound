package com.icompound.core.data.repo.remote

import com.icompound.core.domain.repo.remote.INetworkProvider
import com.icompound.core.util.getModelFromJSON
import com.icompound.core.util.toJson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

class RetrofitNetworkProvider(
    private val apiService: ApiService,
) : INetworkProvider {
    override suspend fun <ResponseBody, RequestBody> delete(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
    ): ResponseBody {
        val response =
            apiService.delete(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                body = requestBody ?: Unit,
            )
        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody, RequestBody> post(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
    ): ResponseBody {
        val response =
            apiService.post(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                body = requestBody ?: Unit,
            )

        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody> postFormData(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: Map<String, Any>?,
    ): ResponseBody {
        val response =
            apiService.postFormData(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                fields = requestBody ?: emptyMap(),
            )

        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody, RequestBody> put(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
    ): ResponseBody {
        val response =
            apiService.put(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                body = requestBody ?: Unit,
            )

        return response.string().getModelFromJSON(responseWrappedModel)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <ResponseBody, RequestBody> putWithImagesFile(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
        files: HashMap<String, File>,
    ): ResponseBody {
        // Convert HashMap to MultipartBody.Part list
        val multipartFiles =
            files.map { (attributeName, file) ->
                file.asMultipart(attributeName)
            }

        // Convert request body to JSON
        val jsonRequestBody =
            requestBody
                ?.toJson()
                ?.toRequestBody("application/json".toMediaTypeOrNull())

        // Convert headers & query params to Map<String, String>
        val headersString = headers?.mapValues { it.value.toString() } ?: emptyMap()
        val queryParamsString = queryParams?.mapValues { it.value.toString() } ?: emptyMap()

        // Make API call with multipart
        val response =
            apiService.putMultipart(
                pathUrl = pathUrl,
                headerMap = headersString,
                queryParams = queryParamsString,
                body = jsonRequestBody,
                files = multipartFiles,
            )

        // Read the raw response once
        val raw = response.body()?.string()

        return when {
            // 204 or explicit “no content” – return Unit
            responseWrappedModel == Nothing::class.java ||
                response.code() == 204 ||
                raw.isNullOrBlank() -> {
                Unit as ResponseBody
            }

            response.isSuccessful -> {
                // Now parse `raw` exactly once
                raw.getModelFromJSON(responseWrappedModel)
                    ?: throw IOException("Empty response body after parsing -> $raw")
            }

            else -> {
                throw HttpException(response)
            }
        }
    }

    override suspend fun <ResponseBody, RequestBody> patch(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
    ): ResponseBody {
        val response =
            apiService.patch(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                body = requestBody ?: Unit,
            )
        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody, RequestBody> postWithHeaderResponse(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
    ): Pair<ResponseBody, Map<String, String>> {
        val response =
            apiService.postWithHeaderResponse(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
                body = requestBody ?: Unit,
            )
        return when {
            response.isSuccessful -> {
                Pair(
                    (response.body() ?: "".toResponseBody())
                        .string()
                        .getModelFromJSON(responseWrappedModel),
                    response.headers().toMap(),
                )
            }

            else -> {
                throw HttpException(response)
            }
        }
    }

    override suspend fun <ResponseBody> get(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
    ): ResponseBody {
        val response =
            apiService.get(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
            )
        return response.string().getModelFromJSON(responseWrappedModel)
    }

    override suspend fun <ResponseBody> getWithHeaderResponse(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
    ): Pair<ResponseBody, Map<String, String>> {
        val response =
            apiService.getWithHeaderResponse(
                pathUrl = pathUrl,
                headerMap = headers ?: hashMapOf(),
                queryParams = queryParams ?: hashMapOf(),
            )
        return when {
            response.isSuccessful -> {
                Pair(
                    (response.body() ?: "".toResponseBody())
                        .string()
                        .getModelFromJSON(responseWrappedModel),
                    response.headers().toMap(),
                )
            }

            else -> {
                throw HttpException(response)
            }
        }
    }

//    override suspend fun <ResponseBody, RequestBody> postWithImagesFile(
//        responseWrappedModel: Type,
//        pathUrl: String,
//        headers: Map<String, Any>?,
//        queryParams: Map<String, Any>?,
//        requestBody: RequestBody?,
//        files: HashMap<String, File>
//    ): ResponseBody {
//        // Convert HashMap to MultipartBody.Part list
//        val multipartFiles = files.map { (attributeName, file) ->
//            file.asMultipart(attributeName)
//        }
//
//        // Convert request body to JSON
//        val jsonRequestBody = requestBody?.toJson()
//            ?.toRequestBody("application/json".toMediaTypeOrNull())
//
//        // Convert headers & query params to Map<String, String>
//        val headersString = headers?.mapValues { it.value.toString() } ?: emptyMap()
//        val queryParamsString = queryParams?.mapValues { it.value.toString() } ?: emptyMap()
//
//        // Make API call with multipart
//        val response = apiService.postMultipart(
//            pathUrl = pathUrl,
//            headerMap = headersString,
//            queryParams = queryParamsString,
//            body = jsonRequestBody,
//            files = multipartFiles
//        )
//
//        return when {
//            response.isSuccessful -> response.body()?.string()
//                ?.getModelFromJSON(responseWrappedModel)
//                ?: throw IOException("Empty response body")
//
//            else -> throw HttpException(response)
//        }
//    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <ResponseBody, RequestBody> postWithImagesFile(
        responseWrappedModel: Type,
        pathUrl: String,
        headers: Map<String, Any>?,
        queryParams: Map<String, Any>?,
        requestBody: RequestBody?,
        files: HashMap<String, File>,
    ): ResponseBody {
        // Convert HashMap to MultipartBody.Part list
        val multipartFiles =
            files.map { (attributeName, file) ->
                file.asMultipart(attributeName)
            }

        // Convert request body to JSON
        val jsonRequestBody =
            requestBody
                ?.toJson()
                ?.toRequestBody("application/json".toMediaTypeOrNull())

        // Convert headers & query params to Map<String, String>
        val headersString = headers?.mapValues { it.value.toString() } ?: emptyMap()
        val queryParamsString = queryParams?.mapValues { it.value.toString() } ?: emptyMap()

        // Make API call with multipart
        val response =
            apiService.postMultipart(
                pathUrl = pathUrl,
                headerMap = headersString,
                queryParams = queryParamsString,
                body = jsonRequestBody,
                files = multipartFiles,
            )

        return when {
            responseWrappedModel == Nothing::class.java || response.code() == 204 || response.body()?.string().isNullOrBlank() -> {
                Unit as ResponseBody
            }

            response.isSuccessful -> {
                response.body()?.string()?.getModelFromJSON(responseWrappedModel)
                    ?: throw IOException("Empty response body")
            }

            else -> {
                throw HttpException(response)
            }
        }
    }

    private fun File.asMultipart(partName: String): MultipartBody.Part {
        val requestBody = this.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, this.name, requestBody)
    }
}
