// port-lint: tests model/prompt.rs
@file:OptIn(ExperimentalSerializationApi::class)

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

private val promptTestJson =
    Json {
        explicitNulls = false
    }

class PromptTest {
    @Test
    fun testPromptMessageImageSerialization() {
        val imageContent =
            RawImageContent(
                data = "base64data",
                mimeType = "image/png",
                meta = null,
            )

        val json = promptTestJson.encodeToString(imageContent)
        println("PromptMessage ImageContent JSON: $json")

        // Verify it contains the Kotlin-facing camelCase spelling, not the Rust-style field spelling.
        assertTrue(json.contains("mimeType"))
        assertFalse(json.contains("mime" + "_" + "type"))
    }

    @Test
    fun testPromptMessageResourceLinkSerialization() {
        val resource = RawResource.new("file:///test.txt", "test.txt")
        val message = PromptMessage.newResourceLink(PromptMessageRole.User, Resource(raw = resource))

        val json = promptTestJson.encodeToString(message)
        println("PromptMessage with ResourceLink JSON: $json")

        // Verify it contains the correct type tag
        assertTrue(json.contains("\"type\":\"resource_link\""))
        assertTrue(json.contains("\"uri\":\"file:///test.txt\""))
        assertTrue(json.contains("\"name\":\"test.txt\""))
    }

    @Test
    fun testPromptMessageContentResourceLinkDeserialization() {
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

        val content = promptTestJson.decodeFromString<PromptMessageContent>(json)

        val link = assertIs<PromptMessageContent.ResourceLink>(content).link.raw
        assertEquals("file:///example.txt", link.uri)
        assertEquals("example.txt", link.name)
        assertEquals("Example file", link.descriptionText)
        assertEquals("text/plain", link.mimeType)
    }
}
