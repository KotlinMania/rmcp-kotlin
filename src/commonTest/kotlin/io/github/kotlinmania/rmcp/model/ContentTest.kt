// port-lint: source model/content.rs
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

private val contentTestJson =
    Json {
        explicitNulls = false
    }

class ContentTest {
    @Test
    fun testImageContentSerialization() {
        val imageContent =
            RawImageContent(
                data = "base64data",
                mimeType = "image/png",
                meta = null,
            )

        val json = contentTestJson.encodeToString(imageContent)
        println("ImageContent JSON: $json")

        // Verify it contains the Kotlin-facing camelCase spelling, not the Rust-style field spelling.
        assertTrue(json.contains("mimeType"))
        assertFalse(json.contains("mime" + "_" + "type"))
    }

    @Test
    fun testAudioContentSerialization() {
        val audioContent =
            RawAudioContent(
                data = "base64audiodata",
                mimeType = "audio/wav",
            )

        val json = contentTestJson.encodeToString(audioContent)
        println("AudioContent JSON: $json")

        // Verify it contains the Kotlin-facing camelCase spelling, not the Rust-style field spelling.
        assertTrue(json.contains("mimeType"))
        assertFalse(json.contains("mime" + "_" + "type"))
    }

    @Test
    fun testResourceLinkSerialization() {
        val resourceLink =
            RawContent.ResourceLink(
                RawResource(
                    uri = "file:///test.txt",
                    name = "test.txt",
                    title = null,
                    descriptionText = "A test file",
                    mimeType = "text/plain",
                    size = 100u,
                    icons = null,
                    meta = null,
                ),
            )

        val json = contentTestJson.encodeToString<RawContent>(resourceLink)
        println("ResourceLink JSON: $json")

        // Verify it contains the correct type tag
        assertTrue(json.contains("\"type\":\"resource_link\""))
        assertTrue(json.contains("\"uri\":\"file:///test.txt\""))
        assertTrue(json.contains("\"name\":\"test.txt\""))
    }

    @Test
    fun testResourceLinkDeserialization() {
        val json =
            """
            {
                "type": "resource_link",
                "uri": "file:///example.txt",
                "name": "example.txt",
                "description": "Example file",
                "mimeType": "text/plain"
            }
            """.trimIndent()

        val content = contentTestJson.decodeFromString<RawContent>(json)

        val resourceLink = assertIs<RawContent.ResourceLink>(content)
        assertEquals("file:///example.txt", resourceLink.value.uri)
        assertEquals("example.txt", resourceLink.value.name)
        assertEquals("Example file", resourceLink.value.descriptionText)
        assertEquals("text/plain", resourceLink.value.mimeType)
    }
}
