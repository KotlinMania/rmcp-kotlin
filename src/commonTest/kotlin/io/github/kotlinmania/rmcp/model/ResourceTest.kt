// port-lint: source model/resource.rs
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val resourceTestJson = Json {
    explicitNulls = false
}

class ResourceTest {
    @Test
    fun testResourceSerialization() {
        val resource = RawResource(
            uri = "file:///test.txt",
            title = null,
            name = "test",
            description = "Test resource",
            mimeType = "text/plain",
            size = 100u,
            icons = null,
            meta = null,
        )

        val json = resourceTestJson.encodeToString(resource)
        println("Serialized JSON: $json")

        // Verify it contains the Kotlin-facing camelCase spelling, not the Rust-style field spelling.
        assertTrue(json.contains("mimeType"))
        assertFalse(json.contains("mime" + "_" + "type"))
    }

    @Test
    fun testResourceContentsSerialization() {
        val textContents = ResourceContents.TextResourceContents(
            uri = "file:///test.txt",
            mimeType = "text/plain",
            text = "Hello world",
            meta = null,
        )

        val json = resourceTestJson.encodeToString(textContents)
        println("ResourceContents JSON: $json")

        // Verify it contains the Kotlin-facing camelCase spelling, not the Rust-style field spelling.
        assertTrue(json.contains("mimeType"))
        assertFalse(json.contains("mime" + "_" + "type"))
    }

    @Test
    fun testResourceTemplateWithIcons() {
        val resourceTemplate = RawResourceTemplate(
            uriTemplate = "file:///{path}",
            name = "template",
            title = "Test Template",
            description = "A test resource template",
            mimeType = "text/plain",
            icons = listOf(
                Icon(
                    src = "https://example.com/icon.png",
                    mimeType = "image/png",
                    sizes = listOf("48x48"),
                ),
            ),
        )

        val json = resourceTestJson.encodeToJsonElement(RawResourceTemplate.serializer(), resourceTemplate).jsonObject
        assertTrue(json["icons"]?.jsonArray != null)
        assertEquals("https://example.com/icon.png", json["icons"]!!.jsonArray[0].jsonObject["src"]!!.jsonPrimitive.content)
        assertEquals("48x48", json["icons"]!!.jsonArray[0].jsonObject["sizes"]!!.jsonArray[0].jsonPrimitive.content)
    }

    @Test
    fun testResourceTemplateWithoutIcons() {
        val resourceTemplate = RawResourceTemplate(
            uriTemplate = "file:///{path}",
            name = "template",
            title = null,
            description = null,
            mimeType = null,
            icons = null,
        )

        val json = resourceTestJson.encodeToJsonElement(RawResourceTemplate.serializer(), resourceTemplate).jsonObject
        assertTrue(json["icons"] == null)
    }
}
