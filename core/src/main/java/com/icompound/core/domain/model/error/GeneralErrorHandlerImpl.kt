package com.icompound.core.domain.model.error

import com.icompound.core.R
import com.icompound.core.domain.model.exception.AppException
import com.icompound.core.util.AppLogger
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Retrofit implementation of ErrorHandler that maps Throwables to domain-level [AppException].
 */
internal class GeneralErrorHandlerImpl : ErrorHandler {
    override fun getError(throwable: Throwable): AppException {
        AppLogger.e("Mapping error", throwable) // was printStackTrace()
        return when (throwable) {
            // Must come BEFORE IOException (it is a subclass of it): pinning failure.
            is SSLPeerUnverifiedException -> AppException.Network.UntrustedConnection

            is IOException -> AppException.Local.IOOperation(R.string.error_io_unexpected_message, throwable.message)

            is HttpException -> mapHttpStatusCode(throwable)

            is AppException -> throwable

            // already mapped (e.g. RequestValidation)
            else -> AppException.Unknown(throwable.message)
        }
    }

    private fun mapHttpStatusCode(exception: HttpException): AppException {
        val errorBody = exception.response()?.errorBody()?.string()

        val json =
            try {
                if (!errorBody.isNullOrBlank()) JSONObject(errorBody) else JSONObject()
            } catch (_: Exception) {
                JSONObject() // fallback if invalid JSON
            }

        val message = extractErrorMessage(json, exception.message())
        val code = json.optInt("code", exception.code())

        return when (exception.code()) {
            // 401 → Unauthorized
            401 -> {
                AppException.Client.Unauthorized
            }

            // 422 → Validation / client-side data issues
            422 -> {
                val errors = extractErrorMap(json)
                AppException.Client.ResponseValidation(errors = errors, message = message)
            }

            // 4xx → Other client-side issues
            in 400..499 -> {
                AppException.Client.Unhandled(code, message)
            }

            // 5xx → Server-side failures
            in 500..599 -> {
                AppException.Server.InternalServerError(code, message)
            }

            // Fallback
            else -> {
                AppException.Unknown(message)
            }
        }
    }

    /**
     * Extracts the best available error message from a JSON response.
     */
    private fun extractErrorMessage(
        json: JSONObject,
        fallback: String?,
    ): String? =
        when {
            json.has("details") -> json.optString("details", fallback!!)
            json.has("error_description") -> json.optString("error_description", fallback!!)
            json.has("error") -> json.optString("error", fallback!!)
            json.has("message") -> json.optString("message", fallback!!)
            json.has("title") -> json.optString("title", fallback!!)
            else -> fallback
        }

    /**
     * Extracts validation error map for ResponseValidation type.
     * Expected structure:
     * {
     *   "errors": {
     *      "email": "Invalid email",
     *      "password": "Too short"
     *   }
     * }
     */
    private fun extractErrorMap(json: JSONObject): Map<String, String> {
        if (!json.has("errors")) return emptyMap()

        return try {
            val errorsJson = json.getJSONObject("errors")
            val result = mutableMapOf<String, String>()
            val keys = errorsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = errorsJson.optString(key)
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
