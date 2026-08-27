// port-lint: source rmcp/src/model/prompt.rs
@file:OptIn(ExperimentalEncodingApi::class)

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
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A prompt that can be used to generate text from a model.
 */
@Serializable
data class Prompt(
    /**
     * The name of the prompt.
     */
    val name: String,
    val title: String? = null,
    /**
     * Optional description of what the prompt does.
     */
    @SerialName("description")
    val descriptionText: String? = null,
    /**
     * Optional arguments that can be passed to customize the prompt.
     */
    val arguments: List<PromptArgument>? = null,
    /**
     * Optional list of icons for the prompt.
     */
    val icons: List<Icon>? = null,
    /**
     * Optional additional metadata for this prompt.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) {
    companion object {
        /**
         * Create a new prompt with the given name, description and arguments.
         */
        fun new(
            name: String,
            description: String?,
            arguments: List<PromptArgument>?,
        ): Prompt =
            Prompt(
                name = name,
                title = null,
                descriptionText = description,
                arguments = arguments,
                icons = null,
                meta = null,
            )
    }
}

/**
 * Represents a prompt argument that can be passed to customize the prompt.
 */
@Serializable
data class PromptArgument(
    /**
     * The name of the argument.
     */
    val name: String,
    /**
     * A human-readable title for the argument.
     */
    val title: String? = null,
    /**
     * A description of what the argument is used for.
     */
    @SerialName("description")
    val descriptionText: String? = null,
    /**
     * Whether this argument is required.
     */
    val required: Boolean? = null,
)

/**
 * Represents the role of a message sender in a prompt conversation.
 */
@Serializable
enum class PromptMessageRole {
    @SerialName("user")
    User,

    @SerialName("assistant")
    Assistant,
}

/**
 * Content types that can be included in prompt messages.
 */
@Serializable(with = PromptMessageContentSerializer::class)
sealed class PromptMessageContent {
    /**
     * Plain text content.
     */
    data class Text(
        val text: String,
    ) : PromptMessageContent()

    /**
     * Image content with base64-encoded data.
     */
    data class Image(
        val image: ImageContent,
    ) : PromptMessageContent()

    /**
     * Embedded server-side resource.
     */
    data class Resource(
        val resource: EmbeddedResource,
    ) : PromptMessageContent()

    /**
     * A link to a resource that can be fetched separately.
     */
    data class ResourceLink(
        val link: io.github.kotlinmania.rmcp.model.Resource,
    ) : PromptMessageContent()

    companion object {
        fun text(text: String): PromptMessageContent =
            Text(text)

        /**
         * Create a resource link content.
         */
        fun resourceLink(resource: io.github.kotlinmania.rmcp.model.Resource): PromptMessageContent =
            ResourceLink(resource)
    }
}

/**
 * A message in a prompt conversation.
 */
@Serializable
data class PromptMessage(
    /**
     * The role of the message sender.
     */
    val role: PromptMessageRole,
    /**
     * The content of the message.
     */
    val content: PromptMessageContent,
) {
    companion object {
        /**
         * Create a new text message with the given role and text content.
         */
        fun newText(role: PromptMessageRole, text: String): PromptMessage =
            PromptMessage(
                role = role,
                content = PromptMessageContent.Text(text),
            )

        /**
         * Create a new image message. [meta] and [annotations] are optional.
         */
        fun newImage(
            role: PromptMessageRole,
            data: ByteArray,
            mimeType: String,
            meta: Meta?,
            annotations: Annotations?,
        ): PromptMessage {
            val base64 = Base64.Default.encode(data)
            return PromptMessage(
                role = role,
                content =
                    PromptMessageContent.Image(
                        ImageContent(
                            raw =
                                RawImageContent(
                                    data = base64,
                                    mimeType = mimeType,
                                    meta = meta,
                                ),
                            annotations = annotations,
                        ),
                    ),
            )
        }

        /**
         * Create a new resource message. [resourceMeta], [resourceContentMeta],
         * and [annotations] are optional.
         */
        fun newResource(
            role: PromptMessageRole,
            uri: String,
            mimeType: String?,
            text: String?,
            resourceMeta: Meta?,
            resourceContentMeta: Meta?,
            annotations: Annotations?,
        ): PromptMessage {
            val resourceContents =
                if (text != null) {
                    ResourceContents.TextResourceContents(
                        uri = uri,
                        mimeType = mimeType,
                        text = text,
                        meta = resourceContentMeta,
                    )
                } else {
                    ResourceContents.BlobResourceContents(
                        uri = uri,
                        mimeType = mimeType,
                        blob = "",
                        meta = resourceContentMeta,
                    )
                }
            return PromptMessage(
                role = role,
                content =
                    PromptMessageContent.Resource(
                        EmbeddedResource(
                            raw =
                                RawEmbeddedResource(
                                    meta = resourceMeta,
                                    resource = resourceContents,
                                ),
                            annotations = annotations,
                        ),
                    ),
            )
        }

        /**
         * Note: PromptMessage text content does not carry protocol-level
         * `_meta` per current schema. This function exists for API symmetry
         * but ignores the meta parameter.
         */
        fun newTextWithMeta(role: PromptMessageRole, text: String, meta: Meta?): PromptMessage =
            newText(role, text)

        /**
         * Create a new resource link message.
         */
        fun newResourceLink(role: PromptMessageRole, resource: io.github.kotlinmania.rmcp.model.Resource): PromptMessage =
            PromptMessage(
                role = role,
                content = PromptMessageContent.ResourceLink(resource),
            )
    }
}

object PromptMessageContentSerializer : KSerializer<PromptMessageContent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("PromptMessageContent")

    override fun serialize(encoder: Encoder, value: PromptMessageContent) {
        val jsonEncoder =
            encoder as? JsonEncoder
                ?: throw SerializationException("PromptMessageContent can only be encoded as JSON")
        val encoded =
            when (value) {
                is PromptMessageContent.Text ->
                    buildJsonObject {
                        put("type", JsonPrimitive("text"))
                        put("text", JsonPrimitive(value.text))
                    }

                is PromptMessageContent.Image ->
                    buildJsonObject {
                        put("type", JsonPrimitive("image"))
                        put("data", JsonPrimitive(value.image.raw.data))
                        put("mimeType", JsonPrimitive(value.image.raw.mimeType))
                        value.image.raw.meta
                            ?.let { put("_meta", jsonEncoder.json.encodeToJsonElement(Meta.serializer(), it)) }
                    }

                is PromptMessageContent.Resource ->
                    buildJsonObject {
                        put("type", JsonPrimitive("resource"))
                        put("resource", jsonEncoder.json.encodeToJsonElement(ResourceContents.serializer(), value.resource.raw.resource))
                        value.resource.raw.meta
                            ?.let { put("_meta", jsonEncoder.json.encodeToJsonElement(Meta.serializer(), it)) }
                    }

                is PromptMessageContent.ResourceLink -> encodeResourceLink(jsonEncoder, value.link.raw)
            }
        jsonEncoder.encodeJsonElement(encoded)
    }

    override fun deserialize(decoder: Decoder): PromptMessageContent {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("PromptMessageContent can only be decoded as JSON")
        val obj = jsonDecoder.decodeJsonElement().jsonObject
        val type =
            obj["type"]?.jsonPrimitive?.content
                ?: throw SerializationException("missing prompt message content type")
        return when (type) {
            "text" -> PromptMessageContent.Text(obj["text"]?.jsonPrimitive?.content.orEmpty())
            "resource_link" ->
                PromptMessageContent.ResourceLink(
                    io.github.kotlinmania.rmcp.model
                        .Resource(raw = decodeResourceLink(obj)),
                )
            else -> throw SerializationException("unknown prompt message content type $type")
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
