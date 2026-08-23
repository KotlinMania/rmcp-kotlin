// port-lint: source model.rs
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.JsonObject as KotlinxJsonObject

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
class EmptyObject {
    override fun equals(other: Any?): Boolean =
        other is EmptyObject

    override fun hashCode(): Int =
        0

    override fun toString(): String =
        "EmptyObject"
}

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
@Serializable(with = NumberOrStringSerializer::class)
sealed class NumberOrString {
    /**
     * A numeric identifier.
     */
    data class Number(
        val value: Long,
    ) : NumberOrString()

    /**
     * A string identifier.
     */
    data class StringValue(
        val value: String,
    ) : NumberOrString()

    fun intoJsonValue(): JsonElement =
        when (this) {
            is Number -> JsonPrimitive(value)
            is StringValue -> JsonPrimitive(value)
        }

    override fun toString(): String =
        when (this) {
            is Number -> value.toString()
            is StringValue -> value
        }
}

object NumberOrStringSerializer : KSerializer<NumberOrString> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NumberOrString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: NumberOrString) {
        when (value) {
            is NumberOrString.Number -> encoder.encodeLong(value.value)
            is NumberOrString.StringValue -> encoder.encodeString(value.value)
        }
    }

    override fun deserialize(decoder: Decoder): NumberOrString {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("NumberOrString can only be decoded as JSON")
        val value = jsonDecoder.decodeJsonElement()
        val primitive =
            value as? JsonPrimitive
                ?: throw SerializationException("Expect number or string")
        if (primitive.isString) {
            return NumberOrString.StringValue(primitive.content)
        }
        val long =
            primitive.longOrNull
                ?: throw SerializationException("Expected an integer")
        return NumberOrString.Number(long)
    }
}

/**
 * Type alias for request identifiers used in JSON-RPC communication.
 */
typealias RequestId = NumberOrString

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
 * Represents a JSON-RPC request with method, parameters, and extensions.
 *
 * This is the core structure for all MCP requests, containing the method being
 * called, the parameters for the method, and additional context data similar to
 * HTTP extensions.
 */
@Serializable
data class Request<P>(
    val method: String,
    val params: P,
    @Transient
    val extensions: Extensions = Extensions(),
) : GetExtensions {
    override fun extensions(): Extensions =
        extensions

    override fun extensionsMut(): Extensions =
        extensions

    companion object {
        fun <P> new(method: ConstString, params: P): Request<P> =
            Request(method = method.value, params = params)
    }
}

@Serializable
data class RequestOptionalParam<P>(
    val method: String,
    val params: P? = null,
    @Transient
    val extensions: Extensions = Extensions(),
) {
    companion object {
        fun <P> withParam(method: ConstString, params: P): RequestOptionalParam<P> =
            RequestOptionalParam(method = method.value, params = params)
    }
}

@Serializable
data class RequestNoParam(
    val method: String,
    @Transient
    val extensions: Extensions = Extensions(),
) : GetExtensions {
    override fun extensions(): Extensions =
        extensions

    override fun extensionsMut(): Extensions =
        extensions
}

@Serializable
data class Notification<P>(
    val method: String,
    val params: P,
    @Transient
    val extensions: Extensions = Extensions(),
) {
    companion object {
        fun <P> new(method: ConstString, params: P): Notification<P> =
            Notification(method = method.value, params = params)
    }
}

@Serializable
data class NotificationNoParam(
    val method: String,
    @Transient
    val extensions: Extensions = Extensions(),
)

@Serializable
data class JsonRpcRequest<R>(
    val jsonrpc: String = JsonRpcVersion2Point0.value,
    val id: RequestId,
    val request: R,
)

typealias DefaultResponse = JsonObject

@Serializable
data class JsonRpcResponse<R>(
    val jsonrpc: String = JsonRpcVersion2Point0.value,
    val id: RequestId,
    val result: R,
)

@Serializable
data class JsonRpcError(
    val jsonrpc: String = JsonRpcVersion2Point0.value,
    val id: RequestId,
    val error: ErrorData,
)

@Serializable
data class JsonRpcNotification<N>(
    val jsonrpc: String = JsonRpcVersion2Point0.value,
    val notification: N,
)

/**
 * Standard JSON-RPC error codes used throughout the MCP protocol.
 *
 * These codes follow the JSON-RPC 2.0 specification and provide standardized
 * error reporting across all MCP implementations.
 */
@Serializable(with = ErrorCodeSerializer::class)
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

object ErrorCodeSerializer : KSerializer<ErrorCode> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ErrorCode", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: ErrorCode) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(decoder: Decoder): ErrorCode =
        ErrorCode(decoder.decodeInt())
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

/**
 * Represents any JSON-RPC message that can be sent or received.
 *
 * This covers all possible message types in the JSON-RPC protocol: individual
 * requests, responses, notifications, and errors.
 */
@Serializable
sealed class JsonRpcMessage<Req, Resp, Noti> {
    /**
     * A single request expecting a response.
     */
    @Serializable
    data class RequestMessage<Req, Resp, Noti>(
        val value: JsonRpcRequest<Req>,
    ) : JsonRpcMessage<Req, Resp, Noti>()

    /**
     * A response to a previous request.
     */
    @Serializable
    data class ResponseMessage<Req, Resp, Noti>(
        val value: JsonRpcResponse<Resp>,
    ) : JsonRpcMessage<Req, Resp, Noti>()

    /**
     * A one-way notification with no response expected.
     */
    @Serializable
    data class NotificationMessage<Req, Resp, Noti>(
        val value: JsonRpcNotification<Noti>,
    ) : JsonRpcMessage<Req, Resp, Noti>()

    /**
     * An error response.
     */
    @Serializable
    data class ErrorMessage<Req, Resp, Noti>(
        val value: JsonRpcError,
    ) : JsonRpcMessage<Req, Resp, Noti>()

    fun intoRequest(): Pair<Req, RequestId>? =
        when (this) {
            is RequestMessage -> value.request to value.id
            else -> null
        }

    fun intoResponse(): Pair<Resp, RequestId>? =
        when (this) {
            is ResponseMessage -> value.result to value.id
            else -> null
        }

    fun intoNotification(): Noti? =
        when (this) {
            is NotificationMessage -> value.notification
            else -> null
        }

    fun intoError(): Pair<ErrorData, RequestId>? =
        when (this) {
            is ErrorMessage -> value.error to value.id
            else -> null
        }

    fun intoResult(): Pair<Result<Resp>, RequestId>? =
        when (this) {
            is ResponseMessage -> Result.success(value.result) to value.id
            is ErrorMessage -> Result.failure<Resp>(JsonRpcErrorException(value.error)) to value.id
            else -> null
        }

    companion object {
        fun <Req, Resp, Noti> request(request: Req, id: RequestId): JsonRpcMessage<Req, Resp, Noti> =
            RequestMessage(JsonRpcRequest(id = id, request = request))

        fun <Req, Resp, Noti> response(response: Resp, id: RequestId): JsonRpcMessage<Req, Resp, Noti> =
            ResponseMessage(JsonRpcResponse(id = id, result = response))

        fun <Req, Resp, Noti> error(error: ErrorData, id: RequestId): JsonRpcMessage<Req, Resp, Noti> =
            ErrorMessage(JsonRpcError(id = id, error = error))

        fun <Req, Resp, Noti> notification(notification: Noti): JsonRpcMessage<Req, Resp, Noti> =
            NotificationMessage(JsonRpcNotification(notification = notification))
    }
}

class JsonRpcErrorException(
    val errorData: ErrorData,
) : IllegalStateException(errorData.message)

/**
 * A response that indicates success but carries no data.
 */
typealias EmptyResult = EmptyObject

/**
 * A catch-all response either side can use for custom requests.
 */
@Serializable
data class CustomResult(
    val value: JsonElement,
) {
    /**
     * Deserialize the result into a strongly-typed structure.
     */
    inline fun <reified T> resultAs(json: Json = modelJson): Result<T> =
        runCatching { json.decodeFromJsonElement<T>(value) }

    companion object {
        fun new(result: JsonElement): CustomResult =
            CustomResult(result)
    }
}

@Serializable
data class CancelledNotificationParam(
    @SerialName("requestId")
    val requestId: RequestId,
    val reason: String? = null,
)

data object CancelledNotificationMethod : ConstString {
    override val value: String = "notifications/cancelled"
}

/**
 * Cancellation notification sent by either side for a previously issued request.
 */
typealias CancelledNotification = Notification<CancelledNotificationParam>

/**
 * A catch-all notification either side can use to send custom messages to its peer.
 *
 * This preserves the raw method name and parameters payload so handlers can
 * deserialize them into domain-specific types.
 */
@Serializable
data class CustomNotification(
    val method: String,
    val params: JsonElement? = null,
    @Transient
    val extensions: Extensions = Extensions(),
) {
    /**
     * Deserialize parameters into a strongly-typed structure.
     */
    inline fun <reified T> paramsAs(json: Json = modelJson): Result<T?> =
        runCatching { params?.let { json.decodeFromJsonElement<T>(it) } }

    companion object {
        fun new(method: String, params: JsonElement?): CustomNotification =
            CustomNotification(method, params)
    }
}

/**
 * A catch-all request either side can use to send custom messages to its peer.
 *
 * This preserves the raw method name and parameters payload so handlers can
 * deserialize them into domain-specific types.
 */
@Serializable
data class CustomRequest(
    val method: String,
    val params: JsonElement? = null,
    @Transient
    val extensions: Extensions = Extensions(),
) {
    /**
     * Deserialize parameters into a strongly-typed structure.
     */
    inline fun <reified T> paramsAs(json: Json = modelJson): Result<T?> =
        runCatching { params?.let { json.decodeFromJsonElement<T>(it) } }

    companion object {
        fun new(method: String, params: JsonElement?): CustomRequest =
            CustomRequest(method, params)
    }
}

data object InitializeResultMethod : ConstString {
    override val value: String = "initialize"
}

/**
 * This request is sent from the client to the server when it first connects,
 * asking it to begin initialization.
 */
typealias InitializeRequest = Request<InitializeRequestParams>

data object InitializedNotificationMethod : ConstString {
    override val value: String = "notifications/initialized"
}

/**
 * This notification is sent from the client to the server after initialization has finished.
 */
typealias InitializedNotification = NotificationNoParam

/**
 * Parameters sent by a client when initializing a connection to an MCP server.
 */
@Serializable
data class InitializeRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The MCP protocol version this client supports.
     */
    @SerialName("protocolVersion")
    val protocolVersion: ProtocolVersion,
    /**
     * The capabilities this client supports.
     */
    val capabilities: ClientCapabilities,
    /**
     * Information about the client implementation.
     */
    @SerialName("clientInfo")
    val clientInfo: Implementation,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }

    companion object {
        fun default(): InitializeRequestParams =
            InitializeRequestParams(
                protocolVersion = ProtocolVersion(),
                capabilities = ClientCapabilities(),
                clientInfo = Implementation.fromBuildEnv(),
            )
    }
}

/**
 * Deprecated: use [InitializeRequestParams] instead.
 */
@Deprecated("Use InitializeRequestParams instead")
typealias InitializeRequestParam = InitializeRequestParams

/**
 * The server's response to an initialization request.
 */
@Serializable
data class InitializeResult(
    /**
     * The MCP protocol version this server supports.
     */
    @SerialName("protocolVersion")
    val protocolVersion: ProtocolVersion,
    /**
     * The capabilities this server provides.
     */
    val capabilities: ServerCapabilities,
    /**
     * Information about the server implementation.
     */
    @SerialName("serverInfo")
    val serverInfo: Implementation,
    /**
     * Optional human-readable instructions about using this server.
     */
    val instructions: String? = null,
) {
    companion object {
        fun default(): InitializeResult =
            InitializeResult(
                protocolVersion = ProtocolVersion(),
                capabilities = ServerCapabilities(),
                serverInfo = Implementation.fromBuildEnv(),
                instructions = null,
            )
    }
}

typealias ServerInfo = InitializeResult
typealias ClientInfo = InitializeRequestParams

/**
 * A URL pointing to an icon resource or a base64-encoded data URI.
 */
@Serializable
data class Icon(
    /**
     * A standard URI pointing to an icon resource.
     */
    val src: String,
    /**
     * Optional override if the server's MIME type is missing or generic.
     */
    @SerialName("mimeType")
    val mimeType: String? = null,
    /**
     * Size specification, each string should be in width-by-height format or
     * any for scalable formats.
     */
    val sizes: List<String>? = null,
)

@Serializable
data class Implementation(
    val name: String,
    val title: String? = null,
    val version: String,
    val description: String? = null,
    val icons: List<Icon>? = null,
    @SerialName("websiteUrl")
    val websiteUrl: String? = null,
) {
    companion object {
        fun fromBuildEnv(): Implementation =
            Implementation(
                name = "rmcp-kotlin",
                title = null,
                version = "0.1.0",
                description = null,
                icons = null,
                websiteUrl = null,
            )

        fun default(): Implementation =
            fromBuildEnv()
    }
}

@Serializable
data class PaginatedRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    val cursor: String? = null,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

/**
 * Deprecated: use [PaginatedRequestParams] instead.
 */
@Deprecated("Use PaginatedRequestParams instead")
typealias PaginatedRequestParam = PaginatedRequestParams

data object PingRequestMethod : ConstString {
    override val value: String = "ping"
}

typealias PingRequest = RequestNoParam

data object ProgressNotificationMethod : ConstString {
    override val value: String = "notifications/progress"
}

@Serializable
data class ProgressNotificationParam(
    @SerialName("progressToken")
    val progressToken: ProgressToken,
    /**
     * The progress thus far. This should increase every time progress is made,
     * even if the total is unknown.
     */
    val progress: Double,
    /**
     * Total number of items to process or total progress required, if known.
     */
    val total: Double? = null,
    /**
     * An optional message describing the current progress.
     */
    val message: String? = null,
)

typealias ProgressNotification = Notification<ProgressNotificationParam>

typealias Cursor = String

@Serializable
data class ListResourcesResult(
    @SerialName("_meta")
    val meta: Meta? = null,
    @SerialName("nextCursor")
    val nextCursor: Cursor? = null,
    val resources: List<Resource> = emptyList(),
) {
    companion object {
        fun withAllItems(items: List<Resource>): ListResourcesResult =
            ListResourcesResult(resources = items)
    }
}

@Serializable
data class ListResourceTemplatesResult(
    @SerialName("_meta")
    val meta: Meta? = null,
    @SerialName("nextCursor")
    val nextCursor: Cursor? = null,
    @SerialName("resourceTemplates")
    val resourceTemplates: List<ResourceTemplate> = emptyList(),
) {
    companion object {
        fun withAllItems(items: List<ResourceTemplate>): ListResourceTemplatesResult =
            ListResourceTemplatesResult(resourceTemplates = items)
    }
}

data object ListResourcesRequestMethod : ConstString {
    override val value: String = "resources/list"
}

/**
 * Request to list all available resources from a server.
 */
typealias ListResourcesRequest = RequestOptionalParam<PaginatedRequestParams>

data object ListResourceTemplatesRequestMethod : ConstString {
    override val value: String = "resources/templates/list"
}

/**
 * Request to list all available resource templates from a server.
 */
typealias ListResourceTemplatesRequest = RequestOptionalParam<PaginatedRequestParams>

data object ReadResourceRequestMethod : ConstString {
    override val value: String = "resources/read"
}

/**
 * Parameters for reading a specific resource.
 */
@Serializable
data class ReadResourceRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The URI of the resource to read.
     */
    val uri: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

/**
 * Deprecated: use [ReadResourceRequestParams] instead.
 */
@Deprecated("Use ReadResourceRequestParams instead")
typealias ReadResourceRequestParam = ReadResourceRequestParams

/**
 * Result containing the contents of a read resource.
 */
@Serializable
data class ReadResourceResult(
    /**
     * The actual content of the resource.
     */
    val contents: List<ResourceContents>,
)

/**
 * Request to read a specific resource.
 */
typealias ReadResourceRequest = Request<ReadResourceRequestParams>

data object ResourceListChangedNotificationMethod : ConstString {
    override val value: String = "notifications/resources/list_changed"
}

/**
 * Notification sent when the list of available resources changes.
 */
typealias ResourceListChangedNotification = NotificationNoParam

data object SubscribeRequestMethod : ConstString {
    override val value: String = "resources/subscribe"
}

/**
 * Parameters for subscribing to resource updates.
 */
@Serializable
data class SubscribeRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The URI of the resource to subscribe to.
     */
    val uri: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use SubscribeRequestParams instead")
typealias SubscribeRequestParam = SubscribeRequestParams

/**
 * Request to subscribe to resource updates.
 */
typealias SubscribeRequest = Request<SubscribeRequestParams>

data object UnsubscribeRequestMethod : ConstString {
    override val value: String = "resources/unsubscribe"
}

/**
 * Parameters for unsubscribing from resource updates.
 */
@Serializable
data class UnsubscribeRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The URI of the resource to unsubscribe from.
     */
    val uri: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use UnsubscribeRequestParams instead")
typealias UnsubscribeRequestParam = UnsubscribeRequestParams

/**
 * Request to unsubscribe from resource updates.
 */
typealias UnsubscribeRequest = Request<UnsubscribeRequestParams>

data object ResourceUpdatedNotificationMethod : ConstString {
    override val value: String = "notifications/resources/updated"
}

/**
 * Parameters for a resource update notification.
 */
@Serializable
data class ResourceUpdatedNotificationParam(
    /**
     * The URI of the resource that was updated.
     */
    val uri: String,
)

/**
 * Notification sent when a subscribed resource is updated.
 */
typealias ResourceUpdatedNotification = Notification<ResourceUpdatedNotificationParam>

data object ListPromptsRequestMethod : ConstString {
    override val value: String = "prompts/list"
}

/**
 * Request to list all available prompts from a server.
 */
typealias ListPromptsRequest = RequestOptionalParam<PaginatedRequestParams>

@Serializable
data class ListPromptsResult(
    @SerialName("_meta")
    val meta: Meta? = null,
    @SerialName("nextCursor")
    val nextCursor: Cursor? = null,
    val prompts: List<Prompt> = emptyList(),
) {
    companion object {
        fun withAllItems(items: List<Prompt>): ListPromptsResult =
            ListPromptsResult(prompts = items)
    }
}

data object GetPromptRequestMethod : ConstString {
    override val value: String = "prompts/get"
}

/**
 * Parameters for retrieving a specific prompt.
 */
@Serializable
data class GetPromptRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    val name: String,
    val arguments: JsonObject? = null,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use GetPromptRequestParams instead")
typealias GetPromptRequestParam = GetPromptRequestParams

/**
 * Request to get a specific prompt.
 */
typealias GetPromptRequest = Request<GetPromptRequestParams>

data object PromptListChangedNotificationMethod : ConstString {
    override val value: String = "notifications/prompts/list_changed"
}

/**
 * Notification sent when the list of available prompts changes.
 */
typealias PromptListChangedNotification = NotificationNoParam

data object ToolListChangedNotificationMethod : ConstString {
    override val value: String = "notifications/tools/list_changed"
}

/**
 * Notification sent when the list of available tools changes.
 */
typealias ToolListChangedNotification = NotificationNoParam

/**
 * Logging levels supported by the MCP protocol.
 */
@Serializable
enum class LoggingLevel {
    @SerialName("debug")
    Debug,

    @SerialName("info")
    Info,

    @SerialName("notice")
    Notice,

    @SerialName("warning")
    Warning,

    @SerialName("error")
    Error,

    @SerialName("critical")
    Critical,

    @SerialName("alert")
    Alert,

    @SerialName("emergency")
    Emergency,
}

data object SetLevelRequestMethod : ConstString {
    override val value: String = "logging/setLevel"
}

/**
 * Parameters for setting the logging level.
 */
@Serializable
data class SetLevelRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The desired logging level.
     */
    val level: LoggingLevel,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use SetLevelRequestParams instead")
typealias SetLevelRequestParam = SetLevelRequestParams

/**
 * Request to set the logging level.
 */
typealias SetLevelRequest = Request<SetLevelRequestParams>

data object LoggingMessageNotificationMethod : ConstString {
    override val value: String = "notifications/message"
}

/**
 * Parameters for a logging message notification.
 */
@Serializable
data class LoggingMessageNotificationParam(
    /**
     * The severity level of this log message.
     */
    val level: LoggingLevel,
    /**
     * Optional logger name that generated this message.
     */
    val logger: String? = null,
    /**
     * The actual log data.
     */
    val data: JsonElement,
)

/**
 * Notification containing a log message.
 */
typealias LoggingMessageNotification = Notification<LoggingMessageNotificationParam>

data object CreateMessageRequestMethod : ConstString {
    override val value: String = "sampling/createMessage"
}

typealias CreateMessageRequest = Request<CreateMessageRequestParams>

/**
 * Represents the role of a participant in a conversation or message exchange.
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

/**
 * Tool selection mode.
 */
@Serializable
enum class ToolChoiceMode {
    /**
     * Model decides whether to use tools.
     */
    @SerialName("auto")
    Auto,

    /**
     * Model must use at least one tool.
     */
    @SerialName("required")
    Required,

    /**
     * Model must not use tools.
     */
    @SerialName("none")
    None,
}

/**
 * Tool choice configuration.
 */
@Serializable
data class ToolChoice(
    val mode: ToolChoiceMode? = null,
) {
    companion object {
        fun auto(): ToolChoice =
            ToolChoice(ToolChoiceMode.Auto)

        fun required(): ToolChoice =
            ToolChoice(ToolChoiceMode.Required)

        fun none(): ToolChoice =
            ToolChoice(ToolChoiceMode.None)
    }
}

/**
 * Single or array content wrapper.
 */
@Serializable
sealed class SamplingContent<T> {
    @Serializable
    data class Single<T>(
        val item: T,
    ) : SamplingContent<T>()

    @Serializable
    data class Multiple<T>(
        val items: List<T>,
    ) : SamplingContent<T>()

    /**
     * Convert to a list regardless of whether it is single or multiple.
     */
    fun intoVec(): List<T> =
        when (this) {
            is Single -> listOf(item)
            is Multiple -> items
        }

    /**
     * Check if the content is empty.
     */
    fun isEmpty(): Boolean =
        when (this) {
            is Single -> false
            is Multiple -> items.isEmpty()
        }

    /**
     * Get the number of content items.
     */
    fun len(): Int =
        when (this) {
            is Single -> 1
            is Multiple -> items.size
        }

    /**
     * Get the first item if present.
     */
    fun first(): T? =
        when (this) {
            is Single -> item
            is Multiple -> items.firstOrNull()
        }

    /**
     * Iterate over all content items.
     */
    fun iter(): List<T> =
        intoVec()

    companion object {
        fun <T> default(): SamplingContent<T> =
            Multiple(emptyList())

        fun <T> from(item: T): SamplingContent<T> =
            Single(item)

        fun <T> from(items: List<T>): SamplingContent<T> =
            Multiple(items)
    }
}

/**
 * A message in a sampling conversation, containing a role and content.
 */
@Serializable
data class SamplingMessage(
    /**
     * The role of the message sender.
     */
    val role: Role,
    /**
     * The actual content of the message.
     */
    val content: SamplingContent<SamplingMessageContent>,
    @SerialName("_meta")
    val meta: Meta? = null,
) {
    companion object {
        fun new(role: Role, content: SamplingMessageContent): SamplingMessage =
            SamplingMessage(role, SamplingContent.Single(content), null)

        fun newMultiple(role: Role, contents: List<SamplingMessageContent>): SamplingMessage =
            SamplingMessage(role, SamplingContent.Multiple(contents), null)

        fun userText(text: String): SamplingMessage =
            new(Role.User, SamplingMessageContent.text(text))

        fun assistantText(text: String): SamplingMessage =
            new(Role.Assistant, SamplingMessageContent.text(text))

        fun userToolResult(toolUseId: String, content: List<Content>): SamplingMessage =
            new(Role.User, SamplingMessageContent.toolResult(toolUseId, content))

        fun assistantToolUse(id: String, name: String, input: JsonObject): SamplingMessage =
            new(Role.Assistant, SamplingMessageContent.toolUse(id, name, input))
    }
}

/**
 * Content types for sampling messages.
 */
@Serializable
sealed class SamplingMessageContent {
    @Serializable
    data class Text(
        val text: RawTextContent,
    ) : SamplingMessageContent()

    @Serializable
    data class Image(
        val image: RawImageContent,
    ) : SamplingMessageContent()

    @Serializable
    data class Audio(
        val audio: RawAudioContent,
    ) : SamplingMessageContent()

    /**
     * Assistant only.
     */
    @Serializable
    data class ToolUse(
        val toolUse: ToolUseContent,
    ) : SamplingMessageContent()

    /**
     * User only.
     */
    @Serializable
    data class ToolResult(
        val toolResult: ToolResultContent,
    ) : SamplingMessageContent()

    fun asText(): RawTextContent? =
        when (this) {
            is Text -> text
            else -> null
        }

    fun asToolUse(): ToolUseContent? =
        when (this) {
            is ToolUse -> toolUse
            else -> null
        }

    fun asToolResult(): ToolResultContent? =
        when (this) {
            is ToolResult -> toolResult
            else -> null
        }

    companion object {
        fun text(text: String): SamplingMessageContent =
            Text(RawTextContent(text = text, meta = null))

        fun toolUse(id: String, name: String, input: JsonObject): SamplingMessageContent =
            ToolUse(ToolUseContent.new(id, name, input))

        fun toolResult(toolUseId: String, content: List<Content>): SamplingMessageContent =
            ToolResult(ToolResultContent.new(toolUseId, content))

        fun from(text: RawTextContent): SamplingMessageContent =
            Text(text)

        fun from(text: String): SamplingMessageContent =
            text(text)

        fun fromContent(content: Content): Result<SamplingMessageContent> =
            when (val raw = content.raw) {
                is RawContent.Text -> Result.success(Text(raw.value))
                is RawContent.Image -> Result.success(Image(raw.value))
                is RawContent.Audio -> Result.success(Audio(raw.value))
                is RawContent.Resource ->
                    Result.failure(
                        IllegalArgumentException("Resource content is not supported in sampling messages"),
                    )
                is RawContent.ResourceLink ->
                    Result.failure(
                        IllegalArgumentException("ResourceLink content is not supported in sampling messages"),
                    )
            }
    }
}

/**
 * Specifies how much context should be included in sampling requests.
 */
@Serializable
enum class ContextInclusion {
    /**
     * Include context from all connected MCP servers.
     */
    @SerialName("allServers")
    AllServers,

    /**
     * Include no additional context.
     */
    @SerialName("none")
    None,

    /**
     * Include context only from the requesting server.
     */
    @SerialName("thisServer")
    ThisServer,
}

/**
 * Parameters for creating a message through LLM sampling.
 */
@Serializable
data class CreateMessageRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * Task metadata for async task management.
     */
    @SerialName("task")
    var taskValue: JsonObject? = null,
    /**
     * The conversation history and current messages.
     */
    val messages: List<SamplingMessage>,
    /**
     * Preferences for model selection and behavior.
     */
    @SerialName("modelPreferences")
    val modelPreferences: ModelPreferences? = null,
    /**
     * System prompt to guide the model's behavior.
     */
    @SerialName("systemPrompt")
    val systemPrompt: String? = null,
    /**
     * How much context to include from MCP servers.
     */
    @SerialName("includeContext")
    val includeContext: ContextInclusion? = null,
    /**
     * Temperature for controlling randomness.
     */
    val temperature: Float? = null,
    /**
     * Maximum number of tokens to generate.
     */
    @SerialName("maxTokens")
    val maxTokens: UInt,
    /**
     * Sequences that should stop generation.
     */
    @SerialName("stopSequences")
    val stopSequences: List<String>? = null,
    /**
     * Additional metadata for the request.
     */
    val metadata: JsonElement? = null,
    /**
     * Tools available for the model to call.
     */
    val tools: List<Tool>? = null,
    /**
     * Tool selection behavior.
     */
    @SerialName("toolChoice")
    val toolChoice: ToolChoice? = null,
) : TaskAugmentedRequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }

    override fun task(): JsonObject? =
        taskValue

    override fun replaceTask(task: JsonObject?) {
        this.taskValue = task
    }

    /**
     * Validate the sampling request parameters per protocol requirements.
     */
    fun validate(): Result<Unit> {
        for (message in messages) {
            for (content in message.content.iter()) {
                when {
                    content is SamplingMessageContent.ToolUse && message.role != Role.Assistant ->
                        return Result.failure(IllegalArgumentException("ToolUse content is only allowed in assistant messages"))
                    content is SamplingMessageContent.ToolResult && message.role != Role.User ->
                        return Result.failure(IllegalArgumentException("ToolResult content is only allowed in user messages"))
                }
            }

            val contents = message.content.iter()
            val hasToolResult = contents.any { it is SamplingMessageContent.ToolResult }
            if (hasToolResult && contents.any { it !is SamplingMessageContent.ToolResult }) {
                return Result.failure(
                    IllegalArgumentException(
                        "SamplingMessage with tool result content MUST NOT contain other content types",
                    ),
                )
            }
        }
        return validateToolUseResultBalance()
    }

    private fun validateToolUseResultBalance(): Result<Unit> {
        val pendingToolUseIds = mutableListOf<String>()
        for (message in messages) {
            if (message.role == Role.Assistant) {
                for (content in message.content.iter()) {
                    if (content is SamplingMessageContent.ToolUse) {
                        pendingToolUseIds += content.toolUse.id
                    }
                }
            } else if (message.role == Role.User) {
                for (content in message.content.iter()) {
                    if (content is SamplingMessageContent.ToolResult) {
                        if (content.toolResult.toolUseId !in pendingToolUseIds) {
                            return Result.failure(
                                IllegalArgumentException(
                                    "ToolResult with toolUseId '${content.toolResult.toolUseId}' has no matching ToolUse",
                                ),
                            )
                        }
                        pendingToolUseIds.removeAll { it == content.toolResult.toolUseId }
                    }
                }
            }
        }
        if (pendingToolUseIds.isNotEmpty()) {
            return Result.failure(
                IllegalArgumentException("ToolUse with id(s) $pendingToolUseIds not balanced with ToolResult"),
            )
        }
        return Result.success(Unit)
    }
}

@Deprecated("Use CreateMessageRequestParams instead")
typealias CreateMessageRequestParam = CreateMessageRequestParams

/**
 * Preferences for model selection and behavior in sampling requests.
 */
@Serializable
data class ModelPreferences(
    /**
     * Specific model names or families to prefer.
     */
    val hints: List<ModelHint>? = null,
    /**
     * Priority for cost optimization.
     */
    @SerialName("costPriority")
    val costPriority: Float? = null,
    /**
     * Priority for speed or latency.
     */
    @SerialName("speedPriority")
    val speedPriority: Float? = null,
    /**
     * Priority for intelligence or capability.
     */
    @SerialName("intelligencePriority")
    val intelligencePriority: Float? = null,
)

/**
 * A hint suggesting a preferred model name or family.
 */
@Serializable
data class ModelHint(
    /**
     * The suggested model name or family identifier.
     */
    val name: String? = null,
)

/**
 * Context for completion requests providing previously resolved arguments.
 */
@Serializable
data class CompletionContext(
    /**
     * Previously resolved argument values that can inform completion suggestions.
     */
    val arguments: Map<String, String>? = null,
) {
    /**
     * Get a specific argument value by name.
     */
    fun getArgument(name: String): String? =
        arguments?.get(name)

    /**
     * Check if the context has any arguments.
     */
    fun hasArguments(): Boolean =
        arguments?.isNotEmpty() ?: false

    /**
     * Get all argument names.
     */
    fun argumentNames(): Set<String> =
        arguments?.keys ?: emptySet()

    companion object {
        /**
         * Create a new empty completion context.
         */
        fun new(): CompletionContext =
            CompletionContext()

        /**
         * Create a completion context with the given arguments.
         */
        fun withArguments(arguments: Map<String, String>): CompletionContext =
            CompletionContext(arguments)
    }
}

@Serializable
data class CompleteRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    @SerialName("ref")
    val reference: Reference,
    val argument: ArgumentInfo,
    /**
     * Optional context containing previously resolved argument values.
     */
    val context: CompletionContext? = null,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use CompleteRequestParams instead")
typealias CompleteRequestParam = CompleteRequestParams

typealias CompleteRequest = Request<CompleteRequestParams>

@Serializable
data class CompletionInfo(
    val values: List<String> = emptyList(),
    val total: UInt? = null,
    @SerialName("hasMore")
    val hasMore: Boolean? = null,
) {
    /**
     * Check if this completion response indicates more results are available.
     */
    fun hasMoreResults(): Boolean =
        hasMore ?: false

    /**
     * Get the total number of available completions, if known.
     */
    fun totalAvailable(): UInt? =
        total

    /**
     * Validate that the completion info complies with the MCP specification.
     */
    fun validate(): Result<Unit> =
        if (values.size > MAX_VALUES) {
            Result.failure(IllegalArgumentException("Too many completion values: ${values.size} (max: $MAX_VALUES)"))
        } else {
            Result.success(Unit)
        }

    companion object {
        /**
         * Maximum number of completion values allowed per response.
         */
        const val MAX_VALUES: Int = 100

        /**
         * Create a new completion info with validation for maximum values.
         */
        fun new(values: List<String>): Result<CompletionInfo> =
            if (values.size > MAX_VALUES) {
                Result.failure(IllegalArgumentException("Too many completion values: ${values.size} (max: $MAX_VALUES)"))
            } else {
                Result.success(CompletionInfo(values = values))
            }

        /**
         * Create completion info with all values and no pagination.
         */
        fun withAllValues(values: List<String>): Result<CompletionInfo> =
            new(values).map { completion ->
                completion.copy(total = completion.values.size.toUInt(), hasMore = false)
            }

        /**
         * Create completion info with pagination information.
         */
        fun withPagination(values: List<String>, total: UInt?, hasMore: Boolean): Result<CompletionInfo> =
            new(values).map { completion ->
                completion.copy(total = total, hasMore = hasMore)
            }
    }
}

@Serializable
data class CompleteResult(
    val completion: CompletionInfo = CompletionInfo(),
)

@Serializable
sealed class Reference {
    @Serializable
    data class Resource(
        val value: ResourceReference,
    ) : Reference()

    @Serializable
    data class Prompt(
        val value: PromptReference,
    ) : Reference()

    /**
     * Get the reference type as a string.
     */
    fun referenceType(): String =
        when (this) {
            is Prompt -> "ref/prompt"
            is Resource -> "ref/resource"
        }

    /**
     * Extract prompt name if this is a prompt reference.
     */
    fun asPromptName(): String? =
        when (this) {
            is Prompt -> value.name
            else -> null
        }

    /**
     * Extract resource URI if this is a resource reference.
     */
    fun asResourceUri(): String? =
        when (this) {
            is Resource -> value.uri
            else -> null
        }

    companion object {
        /**
         * Create a prompt reference.
         */
        fun forPrompt(name: String): Reference =
            Prompt(PromptReference(name = name, title = null))

        /**
         * Create a resource reference.
         */
        fun forResource(uri: String): Reference =
            Resource(ResourceReference(uri))
    }
}

@Serializable
data class ResourceReference(
    val uri: String,
)

@Serializable
data class PromptReference(
    val name: String,
    val title: String? = null,
)

data object CompleteRequestMethod : ConstString {
    override val value: String = "completion/complete"
}

@Serializable
data class ArgumentInfo(
    val name: String,
    val value: String,
)

@Serializable
data class Root(
    val uri: String,
    val name: String? = null,
)

data object ListRootsRequestMethod : ConstString {
    override val value: String = "roots/list"
}

typealias ListRootsRequest = RequestNoParam

@Serializable
data class ListRootsResult(
    val roots: List<Root> = emptyList(),
)

data object RootsListChangedNotificationMethod : ConstString {
    override val value: String = "notifications/roots/list_changed"
}

typealias RootsListChangedNotification = NotificationNoParam

data object ElicitationCreateRequestMethod : ConstString {
    override val value: String = "elicitation/create"
}

data object ElicitationResponseNotificationMethod : ConstString {
    override val value: String = "notifications/elicitation/response"
}

data object ElicitationCompletionNotificationMethod : ConstString {
    override val value: String = "notifications/elicitation/complete"
}

/**
 * Represents the possible actions a user can take in response to an elicitation request.
 */
@Serializable
enum class ElicitationAction {
    /**
     * User accepts the request and provides the requested information.
     */
    @SerialName("accept")
    Accept,

    /**
     * User declines to provide the information but allows the operation to continue.
     */
    @SerialName("decline")
    Decline,

    /**
     * User cancels the entire operation.
     */
    @SerialName("cancel")
    Cancel,
}

/**
 * Parameters for creating an elicitation request to gather user input.
 */
@Serializable
sealed class CreateElicitationRequestParams : RequestParamsMeta {
    @Serializable
    @SerialName("form")
    data class FormElicitationParams(
        /**
         * Protocol-level metadata for this request.
         */
        @SerialName("_meta")
        var metaValue: Meta? = null,
        /**
         * Human-readable message explaining what input is needed from the user.
         */
        val message: String,
        /**
         * Type-safe schema defining the expected structure and validation rules.
         */
        @SerialName("requestedSchema")
        val requestedSchema: ElicitationSchema,
    ) : CreateElicitationRequestParams() {
        override fun meta(): Meta? =
            metaValue

        override fun replaceMeta(meta: Meta?) {
            metaValue = meta
        }
    }

    @Serializable
    @SerialName("url")
    data class UrlElicitationParams(
        /**
         * Protocol-level metadata for this request.
         */
        @SerialName("_meta")
        var metaValue: Meta? = null,
        /**
         * Human-readable message explaining what input is needed from the user.
         */
        val message: String,
        /**
         * The URL where the user can provide the requested information.
         */
        val url: String,
        /**
         * The unique identifier for this elicitation request.
         */
        @SerialName("elicitationId")
        val elicitationId: String,
    ) : CreateElicitationRequestParams() {
        override fun meta(): Meta? =
            metaValue

        override fun replaceMeta(meta: Meta?) {
            metaValue = meta
        }
    }
}

@Deprecated("Use CreateElicitationRequestParams instead")
typealias CreateElicitationRequestParam = CreateElicitationRequestParams

/**
 * The result returned by a client in response to an elicitation request.
 */
@Serializable
data class CreateElicitationResult(
    /**
     * The user's decision on how to handle the elicitation request.
     */
    val action: ElicitationAction,
    /**
     * The actual data provided by the user, if they accepted the request.
     */
    val content: JsonElement? = null,
)

/**
 * Request type for creating an elicitation to gather user input.
 */
typealias CreateElicitationRequest = Request<CreateElicitationRequestParams>

/**
 * Notification parameters for a URL elicitation completion notification.
 */
@Serializable
data class ElicitationResponseNotificationParam(
    @SerialName("elicitationId")
    val elicitationId: String,
)

/**
 * Notification sent when a URL elicitation process is completed.
 */
typealias ElicitationCompletionNotification = Notification<ElicitationResponseNotificationParam>

/**
 * The result of a tool call operation.
 */
@Serializable
data class CallToolResult(
    /**
     * The content returned by the tool.
     */
    val content: List<Content> = emptyList(),
    /**
     * An optional JSON value that represents the structured result of the tool call.
     */
    @SerialName("structuredContent")
    val structuredContent: JsonElement? = null,
    /**
     * Whether this result represents an error condition.
     */
    @SerialName("isError")
    val isError: Boolean? = null,
    /**
     * Optional protocol-level metadata for this result.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) {
    /**
     * Convert the structured content part of response into a certain type.
     */
    inline fun <reified T> intoTyped(json: Json = modelJson): Result<T> {
        structuredContent?.let { value ->
            return runCatching { json.decodeFromJsonElement<T>(value) }
        }
        val rawText =
            content
                .firstOrNull()
                ?.raw
                ?.asText()
                ?.text
        if (rawText != null) {
            return runCatching { json.decodeFromString<T>(rawText) }
        }
        return runCatching { json.decodeFromJsonElement<T>(JsonNull) }
    }

    companion object {
        /**
         * Create a successful tool result with unstructured content.
         */
        fun success(content: List<Content>): CallToolResult =
            CallToolResult(content = content, structuredContent = null, isError = false, meta = null)

        /**
         * Create an error tool result with unstructured content.
         */
        fun error(content: List<Content>): CallToolResult =
            CallToolResult(content = content, structuredContent = null, isError = true, meta = null)

        /**
         * Create a successful tool result with structured content.
         */
        fun structured(value: JsonElement): CallToolResult =
            CallToolResult(
                content = listOf(Content.text(value.toString())),
                structuredContent = value,
                isError = false,
                meta = null,
            )

        /**
         * Create an error tool result with structured content.
         */
        fun structuredError(value: JsonElement): CallToolResult =
            CallToolResult(
                content = listOf(Content.text(value.toString())),
                structuredContent = value,
                isError = true,
                meta = null,
            )
    }
}

data object ListToolsRequestMethod : ConstString {
    override val value: String = "tools/list"
}

/**
 * Request to list all available tools from a server.
 */
typealias ListToolsRequest = RequestOptionalParam<PaginatedRequestParams>

@Serializable
data class ListToolsResult(
    @SerialName("_meta")
    val meta: Meta? = null,
    @SerialName("nextCursor")
    val nextCursor: Cursor? = null,
    val tools: List<Tool> = emptyList(),
) {
    companion object {
        fun withAllItems(items: List<Tool>): ListToolsResult =
            ListToolsResult(tools = items)
    }
}

data object CallToolRequestMethod : ConstString {
    override val value: String = "tools/call"
}

/**
 * Parameters for calling a tool provided by an MCP server.
 */
@Serializable
data class CallToolRequestParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    /**
     * The name of the tool to call.
     */
    val name: String,
    /**
     * Arguments to pass to the tool.
     */
    val arguments: JsonObject? = null,
    /**
     * Task metadata for async task management.
     */
    @SerialName("task")
    var taskValue: JsonObject? = null,
) : TaskAugmentedRequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }

    override fun task(): JsonObject? =
        taskValue

    override fun replaceTask(task: JsonObject?) {
        this.taskValue = task
    }
}

@Deprecated("Use CallToolRequestParams instead")
typealias CallToolRequestParam = CallToolRequestParams

/**
 * Request to call a specific tool.
 */
typealias CallToolRequest = Request<CallToolRequestParams>

/**
 * Result of sampling create-message.
 */
@Serializable
data class CreateMessageResult(
    /**
     * The identifier of the model that generated the response.
     */
    val model: String,
    /**
     * The reason why generation stopped.
     */
    @SerialName("stopReason")
    val stopReason: String? = null,
    /**
     * The generated message with role and content.
     */
    val message: SamplingMessage,
) {
    /**
     * Validate the result: role must be assistant.
     */
    fun validate(): Result<Unit> =
        if (message.role != Role.Assistant) {
            Result.failure(IllegalArgumentException("CreateMessageResult role must be 'assistant'"))
        } else {
            Result.success(Unit)
        }

    companion object {
        const val STOP_REASON_END_TURN: String = "endTurn"
        const val STOP_REASON_END_SEQUENCE: String = "stopSequence"
        const val STOP_REASON_END_MAX_TOKEN: String = "maxTokens"
        const val STOP_REASON_TOOL_USE: String = "toolUse"
    }
}

@Serializable
data class GetPromptResult(
    val description: String? = null,
    val messages: List<PromptMessage>,
)

data object GetTaskInfoMethod : ConstString {
    override val value: String = "tasks/get"
}

typealias GetTaskInfoRequest = Request<GetTaskInfoParams>

@Serializable
data class GetTaskInfoParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    @SerialName("taskId")
    val taskId: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use GetTaskInfoParams instead")
typealias GetTaskInfoParam = GetTaskInfoParams

data object ListTasksMethod : ConstString {
    override val value: String = "tasks/list"
}

typealias ListTasksRequest = RequestOptionalParam<PaginatedRequestParams>

data object GetTaskResultMethod : ConstString {
    override val value: String = "tasks/result"
}

typealias GetTaskResultRequest = Request<GetTaskResultParams>

@Serializable
data class GetTaskResultParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    @SerialName("taskId")
    val taskId: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use GetTaskResultParams instead")
typealias GetTaskResultParam = GetTaskResultParams

data object CancelTaskMethod : ConstString {
    override val value: String = "tasks/cancel"
}

typealias CancelTaskRequest = Request<CancelTaskParams>

@Serializable
data class CancelTaskParams(
    /**
     * Protocol-level metadata for this request.
     */
    @SerialName("_meta")
    var metaValue: Meta? = null,
    @SerialName("taskId")
    val taskId: String,
) : RequestParamsMeta {
    override fun meta(): Meta? =
        metaValue

    override fun replaceMeta(meta: Meta?) {
        this.metaValue = meta
    }
}

@Deprecated("Use CancelTaskParams instead")
typealias CancelTaskParam = CancelTaskParams

@Serializable
data class GetTaskInfoResult(
    val task: Task? = null,
)

@Serializable
data class ListTasksResult(
    val tasks: List<Task> = emptyList(),
    @SerialName("nextCursor")
    val nextCursor: String? = null,
    val total: ULong? = null,
)

@Serializable
sealed class ClientRequest {
    @Serializable data class PingRequest(
        val value: io.github.kotlinmania.rmcp.model.PingRequest,
    ) : ClientRequest()

    @Serializable data class InitializeRequest(
        val value: io.github.kotlinmania.rmcp.model.InitializeRequest,
    ) : ClientRequest()

    @Serializable data class CompleteRequest(
        val value: io.github.kotlinmania.rmcp.model.CompleteRequest,
    ) : ClientRequest()

    @Serializable data class SetLevelRequest(
        val value: io.github.kotlinmania.rmcp.model.SetLevelRequest,
    ) : ClientRequest()

    @Serializable data class GetPromptRequest(
        val value: io.github.kotlinmania.rmcp.model.GetPromptRequest,
    ) : ClientRequest()

    @Serializable data class ListPromptsRequest(
        val value: io.github.kotlinmania.rmcp.model.ListPromptsRequest,
    ) : ClientRequest()

    @Serializable data class ListResourcesRequest(
        val value: io.github.kotlinmania.rmcp.model.ListResourcesRequest,
    ) : ClientRequest()

    @Serializable data class ListResourceTemplatesRequest(
        val value: io.github.kotlinmania.rmcp.model.ListResourceTemplatesRequest,
    ) : ClientRequest()

    @Serializable data class ReadResourceRequest(
        val value: io.github.kotlinmania.rmcp.model.ReadResourceRequest,
    ) : ClientRequest()

    @Serializable data class SubscribeRequest(
        val value: io.github.kotlinmania.rmcp.model.SubscribeRequest,
    ) : ClientRequest()

    @Serializable data class UnsubscribeRequest(
        val value: io.github.kotlinmania.rmcp.model.UnsubscribeRequest,
    ) : ClientRequest()

    @Serializable data class CallToolRequest(
        val value: io.github.kotlinmania.rmcp.model.CallToolRequest,
    ) : ClientRequest()

    @Serializable data class ListToolsRequest(
        val value: io.github.kotlinmania.rmcp.model.ListToolsRequest,
    ) : ClientRequest()

    @Serializable data class GetTaskInfoRequest(
        val value: io.github.kotlinmania.rmcp.model.GetTaskInfoRequest,
    ) : ClientRequest()

    @Serializable data class ListTasksRequest(
        val value: io.github.kotlinmania.rmcp.model.ListTasksRequest,
    ) : ClientRequest()

    @Serializable data class GetTaskResultRequest(
        val value: io.github.kotlinmania.rmcp.model.GetTaskResultRequest,
    ) : ClientRequest()

    @Serializable data class CancelTaskRequest(
        val value: io.github.kotlinmania.rmcp.model.CancelTaskRequest,
    ) : ClientRequest()

    @Serializable data class CustomRequest(
        val value: io.github.kotlinmania.rmcp.model.CustomRequest,
    ) : ClientRequest()

    fun method(): String =
        when (this) {
            is PingRequest -> value.method
            is InitializeRequest -> value.method
            is CompleteRequest -> value.method
            is SetLevelRequest -> value.method
            is GetPromptRequest -> value.method
            is ListPromptsRequest -> value.method
            is ListResourcesRequest -> value.method
            is ListResourceTemplatesRequest -> value.method
            is ReadResourceRequest -> value.method
            is SubscribeRequest -> value.method
            is UnsubscribeRequest -> value.method
            is CallToolRequest -> value.method
            is ListToolsRequest -> value.method
            is GetTaskInfoRequest -> value.method
            is ListTasksRequest -> value.method
            is GetTaskResultRequest -> value.method
            is CancelTaskRequest -> value.method
            is CustomRequest -> value.method
        }
}

@Serializable
sealed class ClientNotification {
    @Serializable data class CancelledNotification(
        val value: io.github.kotlinmania.rmcp.model.CancelledNotification,
    ) : ClientNotification()

    @Serializable data class ProgressNotification(
        val value: io.github.kotlinmania.rmcp.model.ProgressNotification,
    ) : ClientNotification()

    @Serializable data class InitializedNotification(
        val value: io.github.kotlinmania.rmcp.model.InitializedNotification,
    ) : ClientNotification()

    @Serializable data class RootsListChangedNotification(
        val value: io.github.kotlinmania.rmcp.model.RootsListChangedNotification,
    ) : ClientNotification()

    @Serializable data class CustomNotification(
        val value: io.github.kotlinmania.rmcp.model.CustomNotification,
    ) : ClientNotification()
}

@Serializable
sealed class ClientResult {
    @Serializable data class CreateMessageResult(
        val value: io.github.kotlinmania.rmcp.model.CreateMessageResult,
    ) : ClientResult()

    @Serializable data class ListRootsResult(
        val value: io.github.kotlinmania.rmcp.model.ListRootsResult,
    ) : ClientResult()

    @Serializable data class CreateElicitationResult(
        val value: io.github.kotlinmania.rmcp.model.CreateElicitationResult,
    ) : ClientResult()

    @Serializable data class EmptyResult(
        val value: io.github.kotlinmania.rmcp.model.EmptyResult,
    ) : ClientResult()

    @Serializable data class CustomResult(
        val value: io.github.kotlinmania.rmcp.model.CustomResult,
    ) : ClientResult()

    companion object {
        fun empty(unit: Unit): ClientResult =
            EmptyResult(EmptyObject())
    }
}

typealias ClientJsonRpcMessage = JsonRpcMessage<ClientRequest, ClientResult, ClientNotification>

@Serializable
sealed class ServerRequest {
    @Serializable data class PingRequest(
        val value: io.github.kotlinmania.rmcp.model.PingRequest,
    ) : ServerRequest()

    @Serializable data class CreateMessageRequest(
        val value: io.github.kotlinmania.rmcp.model.CreateMessageRequest,
    ) : ServerRequest()

    @Serializable data class ListRootsRequest(
        val value: io.github.kotlinmania.rmcp.model.ListRootsRequest,
    ) : ServerRequest()

    @Serializable data class CreateElicitationRequest(
        val value: io.github.kotlinmania.rmcp.model.CreateElicitationRequest,
    ) : ServerRequest()

    @Serializable data class CustomRequest(
        val value: io.github.kotlinmania.rmcp.model.CustomRequest,
    ) : ServerRequest()
}

@Serializable
sealed class ServerNotification {
    @Serializable data class CancelledNotification(
        val value: io.github.kotlinmania.rmcp.model.CancelledNotification,
    ) : ServerNotification()

    @Serializable data class ProgressNotification(
        val value: io.github.kotlinmania.rmcp.model.ProgressNotification,
    ) : ServerNotification()

    @Serializable data class LoggingMessageNotification(
        val value: io.github.kotlinmania.rmcp.model.LoggingMessageNotification,
    ) : ServerNotification()

    @Serializable data class ResourceUpdatedNotification(
        val value: io.github.kotlinmania.rmcp.model.ResourceUpdatedNotification,
    ) : ServerNotification()

    @Serializable data class ResourceListChangedNotification(
        val value: io.github.kotlinmania.rmcp.model.ResourceListChangedNotification,
    ) : ServerNotification()

    @Serializable data class ToolListChangedNotification(
        val value: io.github.kotlinmania.rmcp.model.ToolListChangedNotification,
    ) : ServerNotification()

    @Serializable data class PromptListChangedNotification(
        val value: io.github.kotlinmania.rmcp.model.PromptListChangedNotification,
    ) : ServerNotification()

    @Serializable data class ElicitationCompletionNotification(
        val value: io.github.kotlinmania.rmcp.model.ElicitationCompletionNotification,
    ) : ServerNotification()

    @Serializable data class CustomNotification(
        val value: io.github.kotlinmania.rmcp.model.CustomNotification,
    ) : ServerNotification()
}

@Serializable
sealed class ServerResult {
    @Serializable data class InitializeResult(
        val value: io.github.kotlinmania.rmcp.model.InitializeResult,
    ) : ServerResult()

    @Serializable data class CompleteResult(
        val value: io.github.kotlinmania.rmcp.model.CompleteResult,
    ) : ServerResult()

    @Serializable data class GetPromptResult(
        val value: io.github.kotlinmania.rmcp.model.GetPromptResult,
    ) : ServerResult()

    @Serializable data class ListPromptsResult(
        val value: io.github.kotlinmania.rmcp.model.ListPromptsResult,
    ) : ServerResult()

    @Serializable data class ListResourcesResult(
        val value: io.github.kotlinmania.rmcp.model.ListResourcesResult,
    ) : ServerResult()

    @Serializable data class ListResourceTemplatesResult(
        val value: io.github.kotlinmania.rmcp.model.ListResourceTemplatesResult,
    ) : ServerResult()

    @Serializable data class ReadResourceResult(
        val value: io.github.kotlinmania.rmcp.model.ReadResourceResult,
    ) : ServerResult()

    @Serializable data class CallToolResult(
        val value: io.github.kotlinmania.rmcp.model.CallToolResult,
    ) : ServerResult()

    @Serializable data class ListToolsResult(
        val value: io.github.kotlinmania.rmcp.model.ListToolsResult,
    ) : ServerResult()

    @Serializable data class CreateElicitationResult(
        val value: io.github.kotlinmania.rmcp.model.CreateElicitationResult,
    ) : ServerResult()

    @Serializable data class EmptyResult(
        val value: io.github.kotlinmania.rmcp.model.EmptyResult,
    ) : ServerResult()

    @Serializable data class CreateTaskResult(
        val value: io.github.kotlinmania.rmcp.model.CreateTaskResult,
    ) : ServerResult()

    @Serializable data class ListTasksResult(
        val value: io.github.kotlinmania.rmcp.model.ListTasksResult,
    ) : ServerResult()

    @Serializable data class GetTaskInfoResult(
        val value: io.github.kotlinmania.rmcp.model.GetTaskInfoResult,
    ) : ServerResult()

    @Serializable data class TaskResult(
        val value: io.github.kotlinmania.rmcp.model.TaskResult,
    ) : ServerResult()

    @Serializable data class CustomResult(
        val value: io.github.kotlinmania.rmcp.model.CustomResult,
    ) : ServerResult()

    companion object {
        fun empty(unit: Unit): ServerResult =
            EmptyResult(EmptyObject())
    }
}

typealias ServerJsonRpcMessage = JsonRpcMessage<ServerRequest, ServerResult, ServerNotification>

fun ServerNotification.tryIntoCancelledNotification(): Result<CancelledNotification> =
    when (this) {
        is ServerNotification.CancelledNotification -> Result.success(value)
        else -> Result.failure(IllegalArgumentException("not a cancelled notification"))
    }

fun ClientNotification.tryIntoCancelledNotification(): Result<CancelledNotification> =
    when (this) {
        is ClientNotification.CancelledNotification -> Result.success(value)
        else -> Result.failure(IllegalArgumentException("not a cancelled notification"))
    }

@PublishedApi
internal val modelJson =
    Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
