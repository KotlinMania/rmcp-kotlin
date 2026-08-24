// port-lint: source model/resource.rs
package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a resource in the extension with metadata.
 */
@Serializable
data class RawResource(
    /**
     * URI representing the resource location, for example
     * `file:///path/to/file` or `str:///content`.
     */
    val uri: String,
    /**
     * Name of the resource.
     */
    val name: String,
    /**
     * Human-readable title of the resource.
     */
    val title: String? = null,
    /**
     * Optional description of the resource.
     */
    @SerialName("description")
    val descriptionText: String? = null,
    /**
     * MIME type of the resource content, `text` or `blob`.
     */
    @SerialName("mimeType")
    val mimeType: String? = null,
    /**
     * The size of the raw resource content, in bytes, before base64 encoding or
     * any tokenization, if known.
     *
     * This can be used by Hosts to display file sizes and estimate context
     * window use.
     */
    val size: UInt? = null,
    /**
     * Optional list of icons for the resource.
     */
    val icons: List<Icon>? = null,
    /**
     * Optional additional metadata for this resource.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) : AnnotateAble {
    companion object {
        /**
         * Creates a new Resource from a URI with explicit MIME type.
         */
        fun new(uri: String, name: String): RawResource =
            RawResource(
                uri = uri,
                name = name,
                title = null,
                descriptionText = null,
                mimeType = null,
                size = null,
                icons = null,
                meta = null,
            )
    }
}

@Serializable
data class Resource(
    val raw: RawResource,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawResource, annotations: Annotations? = null): Resource =
            Resource(raw, annotations)
    }
}

@Serializable
data class RawResourceTemplate(
    @SerialName("uriTemplate")
    val uriTemplate: String,
    val name: String,
    val title: String? = null,
    @SerialName("description")
    val descriptionText: String? = null,
    @SerialName("mimeType")
    val mimeType: String? = null,
    /**
     * Optional list of icons for the resource template.
     */
    val icons: List<Icon>? = null,
) : AnnotateAble

@Serializable
data class ResourceTemplate(
    val raw: RawResourceTemplate,
    val annotations: Annotations? = null,
) {
    companion object {
        fun new(raw: RawResourceTemplate, annotations: Annotations? = null): ResourceTemplate =
            ResourceTemplate(raw, annotations)
    }
}

@Serializable
sealed class ResourceContents {
    abstract val uri: String
    abstract val mimeType: String?
    abstract val meta: Meta?

    @Serializable
    data class TextResourceContents(
        override val uri: String,
        @SerialName("mimeType")
        override val mimeType: String? = null,
        val text: String,
        @SerialName("_meta")
        override val meta: Meta? = null,
    ) : ResourceContents()

    @Serializable
    data class BlobResourceContents(
        override val uri: String,
        @SerialName("mimeType")
        override val mimeType: String? = null,
        val blob: String,
        @SerialName("_meta")
        override val meta: Meta? = null,
    ) : ResourceContents()

    companion object {
        fun text(text: String, uri: String): ResourceContents =
            TextResourceContents(
                uri = uri,
                mimeType = "text",
                text = text,
                meta = null,
            )
    }
}
