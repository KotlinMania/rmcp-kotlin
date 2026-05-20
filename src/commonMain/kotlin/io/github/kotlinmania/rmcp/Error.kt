// port-lint: source error.rs
package io.github.kotlinmania.rmcp

import io.github.kotlinmania.rmcp.model.ErrorData as ModelErrorData

typealias ErrorData = ModelErrorData

/**
 * Deprecated: use [ErrorData] instead. [ErrorData] could become [RmcpError] in the future.
 */
@Deprecated("Use ErrorData instead")
typealias Error = ModelErrorData

fun ErrorData.fmt(): String =
    buildString {
        append(code.value)
        append(": ")
        append(message)
        data?.let {
            append("(")
            append(it)
            append(")")
        }
    }

/**
 * This is a unified error type for the errors that could be returned by the service.
 */
sealed class RmcpError(
    message: String,
    cause: Throwable? = null,
) : Throwable(message, cause) {
    data class Service(
        val error: Throwable,
    ) : RmcpError("Service error: ${error.message}", error)

    data class ClientInitialize(
        val error: Throwable,
    ) : RmcpError("Client initialization error: ${error.message}", error)

    data class ServerInitialize(
        val error: Throwable,
    ) : RmcpError("Server initialization error: ${error.message}", error)

    data class Runtime(
        val error: Throwable,
    ) : RmcpError("Runtime error: ${error.message}", error)

    data class TransportCreation(
        val intoTransportTypeName: String,
        val intoTransportTypeId: String,
        val error: Throwable,
    ) : RmcpError("Transport creation error: ${error.message}", error)

    data class TaskError(
        val error: String,
    ) : RmcpError("Task error: $error")

    companion object {
        inline fun <reified T : Any> transportCreation(error: Throwable): RmcpError =
            TransportCreation(
                intoTransportTypeName = T::class.simpleName ?: "unknown",
                intoTransportTypeId = T::class.toString(),
                error = error,
            )
    }
}
