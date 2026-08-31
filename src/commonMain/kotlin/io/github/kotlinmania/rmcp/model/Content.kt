// port-lint: source rmcp/src/model/content.rs
package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Content sent around agents, extensions, and LLMs.
 *
 * The various content types can be displayed to humans but also understood by
 * models. They include optional annotations used to help inform agent usage.
 */

@Serializable
data class RawTextContent(
    val text: String,
    /**
     * Optional protocol-level metadata for this content block.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) : AnnotateAble

@Serializable
data class TextContent(
    val raw: RawTextContent,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawTextContent, annotations: Annotations? = null): TextContent =
            TextContent(raw, annotations)
    }
}

@Serializable
data class RawImageContent(
    /**
     * The base64-encoded image.
     */
    val data: String,
    @SerialName("mimeType")
    val mimeType: String,
    /**
     * Optional protocol-level metadata for this content block.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) : AnnotateAble

@Serializable
data class ImageContent(
    val raw: RawImageContent,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawImageContent, annotations: Annotations? = null): ImageContent =
            ImageContent(raw, annotations)
    }
}

@Serializable
data class RawEmbeddedResource(
    /**
     * Optional protocol-level metadata for this content block.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
    val resource: ResourceContents,
) : AnnotateAble

@Serializable
data class EmbeddedResource(
    val raw: RawEmbeddedResource,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawEmbeddedResource, annotations: Annotations? = null): EmbeddedResource =
            EmbeddedResource(raw, annotations)
    }
}

fun EmbeddedResource.getText(): String =
    when (val resource = raw.resource) {
        is ResourceContents.TextResourceContents -> resource.text
        is ResourceContents.BlobResourceContents -> ""
    }

@Serializable
data class RawAudioContent(
    val data: String,
    @SerialName("mimeType")
    val mimeType: String,
) : AnnotateAble

@Serializable
data class AudioContent(
    val raw: RawAudioContent,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawAudioContent, annotations: Annotations? = null): AudioContent =
            AudioContent(raw, annotations)
    }
}

/**
 * Tool call request from assistant (SEP-1577).
 */
@Serializable
data class ToolUseContent(
    /**
     * Unique identifier for this tool call.
     */
    val id: String,
    /**
     * Name of the tool to call.
     */
    val name: String,
    /**
     * Input arguments for the tool.
     */
    val input: JsonObject,
    /**
     * Optional metadata, preserved for caching.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) {
    companion object {
        fun new(id: String, name: String, input: JsonObject): ToolUseContent =
            ToolUseContent(id, name, input, null)
    }
}

/**
 * Tool execution result in user message (SEP-1577).
 */
@Serializable
data class ToolResultContent(
    /**
     * Optional metadata.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
    /**
     * ID of the corresponding tool use.
     */
    @SerialName("toolUseId")
    val toolUseId: String,
    /**
     * Content blocks returned by the tool.
     */
    val content: List<Content> = emptyList(),
    /**
     * Optional structured result.
     */
    @SerialName("structuredContent")
    val structuredContent: JsonObject? = null,
    /**
     * Whether tool execution failed.
     */
    @SerialName("isError")
    val isError: Boolean? = null,
) {
    companion object {
        fun new(toolUseId: String, content: List<Content>): ToolResultContent =
            ToolResultContent(
                meta = null,
                toolUseId = toolUseId,
                content = content,
                structuredContent = null,
                isError = null,
            )

        fun error(toolUseId: String, content: List<Content>): ToolResultContent =
            ToolResultContent(
                meta = null,
                toolUseId = toolUseId,
                content = content,
                structuredContent = null,
                isError = true,
            )
    }
}

@Serializable(with = RawContentSerializer::class)
sealed class RawContent : AnnotateAble {
    data class Text(
        val value: RawTextContent,
    ) : RawContent()

    data class Image(
        val value: RawImageContent,
    ) : RawContent()

    data class Resource(
        val value: RawEmbeddedResource,
    ) : RawContent()

    data class Audio(
        val value: RawAudioContent,
    ) : RawContent()

    data class ResourceLink(
        val value: RawResource,
    ) : RawContent()

    fun asText(): RawTextContent? =
        when (this) {
            is Text -> value
            else -> null
        }

    fun asImage(): RawImageContent? =
        when (this) {
            is Image -> value
            else -> null
        }

    fun asResource(): RawEmbeddedResource? =
        when (this) {
            is Resource -> value
            else -> null
        }

    fun asResourceLink(): RawResource? =
        when (this) {
            is ResourceLink -> value
            else -> null
        }

    companion object {
        fun json(json: JsonElement): Result<RawContent> =
            Result.success(text(json.toString()))

        fun text(text: String): RawContent =
            Text(RawTextContent(text = text, meta = null))

        fun image(data: String, mimeType: String): RawContent =
            Image(RawImageContent(data = data, mimeType = mimeType, meta = null))

        fun resource(resource: ResourceContents): RawContent =
            Resource(RawEmbeddedResource(meta = null, resource = resource))

        fun embeddedText(uri: String, content: String): RawContent =
            Resource(
                RawEmbeddedResource(
                    meta = null,
                    resource =
                        ResourceContents.TextResourceContents(
                            uri = uri,
                            mimeType = "text",
                            text = content,
                            meta = null,
                        ),
                ),
            )

        /**
         * Create a resource link content.
         */
        fun resourceLink(resource: RawResource): RawContent =
            ResourceLink(resource)
    }
}

@Serializable
data class Content(
    val raw: RawContent,
    val annotations: Annotations? = null,
) {
    companion object {
        fun text(text: String): Content =
            Content(RawContent.text(text), null)

        fun image(data: String, mimeType: String): Content =
            Content(RawContent.image(data, mimeType), null)

        fun resource(resource: ResourceContents): Content =
            Content(RawContent.resource(resource), null)

        fun embeddedText(uri: String, content: String): Content =
            Content(RawContent.embeddedText(uri, content), null)

        fun json(json: JsonElement): Result<Content> =
            RawContent.json(json).map { Content(it, null) }

        /**
         * Create a resource link content.
         */
        fun resourceLink(resource: RawResource): Content =
            Content(RawContent.resourceLink(resource), null)
    }
}

@Serializable
data class JsonContent<S>(
    val value: S,
)

/**
 * Types that can be converted into a list of contents.
 */
interface IntoContents {
    fun intoContents(): List<Content>
}

fun Content.intoContents(): List<Content> =
    listOf(this)

fun String.intoContents(): List<Content> =
    listOf(Content.text(this))

fun Unit.intoContents(): List<Content> =
    emptyList()

object RawContentSerializer : KSerializer<RawContent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("RawContent")

    override fun serialize(encoder: Encoder, value: RawContent) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("RawContent can only be encoded as JSON")
        val encoded =
            when (value) {
                is RawContent.Text ->
                    buildJsonObject {
                        put("type", JsonPrimitive("text"))
                        put("text", JsonPrimitive(value.value.text))
                        value.value.meta?.let { put("_meta", jsonEncoder.json.encodeToJsonElement(Meta.serializer(), it)) }
                    }

                is RawContent.Image ->
                    buildJsonObject {
                        put("type", JsonPrimitive("image"))
                        put("data", JsonPrimitive(value.value.data))
                        put("mimeType", JsonPrimitive(value.value.mimeType))
                        value.value.meta?.let { put("_meta", jsonEncoder.json.encodeToJsonElement(Meta.serializer(), it)) }
                    }

                is RawContent.Resource ->
                    buildJsonObject {
                        put("type", JsonPrimitive("resource"))
                        put("resource", jsonEncoder.json.encodeToJsonElement(ResourceContents.serializer(), value.value.resource))
                        value.value.meta?.let { put("_meta", jsonEncoder.json.encodeToJsonElement(Meta.serializer(), it)) }
                    }

                is RawContent.Audio ->
                    buildJsonObject {
                        put("type", JsonPrimitive("audio"))
                        put("data", JsonPrimitive(value.value.data))
                        put("mimeType", JsonPrimitive(value.value.mimeType))
                    }

                is RawContent.ResourceLink -> encodeResourceLink(jsonEncoder, value.value)
            }
        jsonEncoder.encodeJsonElement(encoded)
    }

    override fun deserialize(decoder: Decoder): RawContent {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("RawContent can only be decoded as JSON")
        val obj = jsonDecoder.decodeJsonElement().jsonObject
        val type =
            obj["type"]?.jsonPrimitive?.content
                ?: throw SerializationException("missing content type")
        return when (type) {
            "resource_link" -> RawContent.ResourceLink(decodeResourceLink(obj))
            "text" -> RawContent.Text(RawTextContent(text = obj["text"]?.jsonPrimitive?.content.orEmpty()))
            "image" ->
                RawContent.Image(
                    RawImageContent(
                        data = obj["data"]?.jsonPrimitive?.content.orEmpty(),
                        mimeType = obj["mimeType"]?.jsonPrimitive?.content.orEmpty(),
                    ),
                )
            "audio" ->
                RawContent.Audio(
                    RawAudioContent(
                        data = obj["data"]?.jsonPrimitive?.content.orEmpty(),
                        mimeType = obj["mimeType"]?.jsonPrimitive?.content.orEmpty(),
                    ),
                )
            else -> throw SerializationException("unknown content type $type")
        }
    }

    private fun encodeResourceLink(encoder: JsonEncoder, resource: RawResource): JsonObject {
        val encoded = encoder.json.encodeToJsonElement(RawResource.serializer(), resource).jsonObject
        return buildJsonObject {
            put("type", JsonPrimitive("resource_link"))
            for ((key, value) in encoded) {
                put(key, value)
            }
        }
    }

    private fun decodeResourceLink(obj: JsonObject): RawResource =
        RawResource(
            uri = obj["uri"]?.jsonPrimitive?.content.orEmpty(),
            name = obj["name"]?.jsonPrimitive?.content.orEmpty(),
            title = obj["title"]?.jsonPrimitive?.content,
            descriptionText = obj["description"]?.jsonPrimitive?.content,
            mimeType = obj["mimeType"]?.jsonPrimitive?.content,
            size = null,
            icons = null,
            meta = null,
        )
}
