// port-lint: source model.rs
package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject as KotlinxJsonObject
import kotlinx.serialization.json.buildJsonObject

/**
 * A JSON object type alias for convenient handling of JSON data.
 *
 * You can use [object] to create a JSON object quickly. This is commonly used
 * for storing arbitrary JSON data in MCP messages.
 */
typealias JsonObject = KotlinxJsonObject

/**
 * Unwrap the JSON object under [JsonElement].
 *
 * This returns an empty object when the value is not an object.
 */
fun `object`(value: JsonElement): JsonObject =
    value as? KotlinxJsonObject ?: buildJsonObject {}

/**
 * This is commonly used for representing empty objects in MCP messages.
 *
 * Without returning any specific data.
 */
@Serializable
class EmptyObject

interface ConstString {
    val value: String

    fun asStr(): String = value
}

data object JsonRpcVersion2Point0 : ConstString {
    override val value: String = "2.0"
}

/**
 * Represents the MCP protocol version used for communication.
 *
 * This ensures compatibility between clients and servers by specifying which
 * version of the Model Context Protocol is being used.
 */
@Serializable
data class ProtocolVersion(
    val version: String = LATEST.version,
) : Comparable<ProtocolVersion> {
    override fun toString(): String = version

    override fun compareTo(other: ProtocolVersion): Int =
        version.compareTo(other.version)

    companion object {
        val V_2025_06_18: ProtocolVersion = ProtocolVersion("2025-06-18")
        val V_2025_03_26: ProtocolVersion = ProtocolVersion("2025-03-26")
        val V_2024_11_05: ProtocolVersion = ProtocolVersion("2024-11-05")

        // Keep LATEST at 2025-03-26 until full 2025-06-18 compliance and automated testing are in place.
        val LATEST: ProtocolVersion = V_2025_03_26
    }
}

/**
 * A flexible identifier type that can be either a number or a string.
 *
 * This is commonly used for request IDs and other identifiers in JSON-RPC
 * where the specification allows both numeric and string values.
 */
@Serializable
sealed class NumberOrString {
    /**
     * A numeric identifier.
     */
    @Serializable
    data class Number(val value: Long) : NumberOrString()

    /**
     * A string identifier.
     */
    @Serializable
    data class StringValue(val value: String) : NumberOrString()
}

/**
 * Type alias for request identifiers used in JSON-RPC communication.
 */
typealias RequestId = NumberOrString

typealias ClientJsonRpcMessage = JsonElement

typealias ServerJsonRpcMessage = JsonElement

/**
 * A token used to track the progress of long-running operations.
 *
 * Progress tokens allow clients and servers to associate progress notifications
 * with specific requests, enabling real-time updates on operation status.
 */
@Serializable
data class ProgressToken(
    val value: NumberOrString,
)

/**
 * Represents the role of a participant in a conversation or message exchange.
 *
 * Used in sampling and chat contexts to distinguish between different types of
 * message senders in the conversation flow.
 */
@Serializable
enum class Role {
    /**
     * A human user or client making a request.
     */
    @SerialName("user")
    User,

    /**
     * An AI assistant or server providing a response.
     */
    @SerialName("assistant")
    Assistant,
}

@Serializable
data class Icon(
    val src: String,
    @SerialName("mimeType")
    val mimeType: String? = null,
    val sizes: List<String>? = null,
)

/**
 * Standard JSON-RPC error codes used throughout the MCP protocol.
 *
 * These codes follow the JSON-RPC 2.0 specification and provide standardized
 * error reporting across all MCP implementations.
 */
@Serializable
data class ErrorCode(
    val value: Int = 0,
) {
    companion object {
        val RESOURCE_NOT_FOUND: ErrorCode = ErrorCode(-32002)
        val INVALID_REQUEST: ErrorCode = ErrorCode(-32600)
        val METHOD_NOT_FOUND: ErrorCode = ErrorCode(-32601)
        val INVALID_PARAMS: ErrorCode = ErrorCode(-32602)
        val INTERNAL_ERROR: ErrorCode = ErrorCode(-32603)
        val PARSE_ERROR: ErrorCode = ErrorCode(-32700)
        val URL_ELICITATION_REQUIRED: ErrorCode = ErrorCode(-32042)
    }
}

/**
 * Error information for JSON-RPC error responses.
 *
 * This structure follows the JSON-RPC 2.0 specification for error reporting,
 * providing a standardized way to communicate errors between clients and servers.
 */
@Serializable
data class ErrorData(
    /**
     * The error type that occurred, using standard JSON-RPC error codes.
     */
    val code: ErrorCode,

    /**
     * A short description of the error. The message should be limited to a concise single sentence.
     */
    val message: String,

    /**
     * Additional information about the error. The value of this member is defined by the sender.
     */
    val data: JsonElement? = null,
) {
    companion object {
        fun new(code: ErrorCode, message: String, data: JsonElement?): ErrorData =
            ErrorData(code, message, data)

        fun resourceNotFound(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.RESOURCE_NOT_FOUND, message, data)

        fun parseError(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.PARSE_ERROR, message, data)

        fun invalidRequest(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.INVALID_REQUEST, message, data)

        fun methodNotFound(method: ConstString): ErrorData =
            new(ErrorCode.METHOD_NOT_FOUND, method.value, null)

        fun invalidParams(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.INVALID_PARAMS, message, data)

        fun internalError(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.INTERNAL_ERROR, message, data)

        fun urlElicitationRequired(message: String, data: JsonElement?): ErrorData =
            new(ErrorCode.URL_ELICITATION_REQUIRED, message, data)
    }
}
