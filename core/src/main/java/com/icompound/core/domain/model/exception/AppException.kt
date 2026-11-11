package com.icompound.core.domain.model.exception

import androidx.annotation.StringRes
import kotlin.reflect.KClass

sealed class AppException(
    message: String?,
) : Exception(message) {
    sealed class Network(
        message: String? = null,
    ) : AppException(message) {
        data class Retrial(
            @StringRes val messageRes: Int,
            override val message: String?,
        ) : Network(message)

        data class Unhandled(
            @StringRes val messageRes: Int,
            override val message: String?,
        ) : Network(message)

        // Certificate pinning / TLS trust failure
        data object UntrustedConnection : Network("The connection is not trusted.")
    }

    sealed class Client(
        message: String? = null,
    ) : AppException(message) {
        data object Unauthorized : Client("Unauthorized Access.")

        data object SessionExpired : Client("Session expired. Please sign in again.")

        // Was extending PetsException directly, so `is Client` never matched it.
        data class ResponseValidation(
            val errors: Map<String, String> = emptyMap(),
            override val message: String?,
        ) : Client(message)

        data class Unhandled(
            val httpErrorCode: Int,
            override val message: String?,
        ) : Client("Unhandled client error with code:$httpErrorCode, and the failure reason: $message")
    }

    sealed class Server(
        message: String? = null,
    ) : AppException(message) {
        data class InternalServerError(
            val httpErrorCode: Int,
            override val message: String?,
        ) : Server("Internal server error with code:$httpErrorCode, and the failure reason: $message")
    }

    sealed class Local(
        message: String? = null,
    ) : AppException(message) {
        data class RequestValidation(
            val clazz: KClass<*>,
            override val message: String? = null,
        ) : Local(
                buildString {
                    append("There is missing input for this class: ${clazz.simpleName}")
                    message?.let { append(", message: $it") }
                },
            )

        data class IOOperation(
            @StringRes val messageRes: Int,
            override val message: String? = "",
        ) : Local(message)
    }

    data class Unknown(
        override val message: String?,
    ) : AppException(message)

    fun isUnauthorized() = this == Client.Unauthorized
}
