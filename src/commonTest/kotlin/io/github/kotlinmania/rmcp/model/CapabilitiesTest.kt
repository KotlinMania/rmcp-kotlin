// port-lint: tests model/capabilities.rs
@file:OptIn(ExperimentalSerializationApi::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val capabilitiesTestJson =
    Json {
        explicitNulls = false
    }

class CapabilitiesTest {
    @Test
    fun testBuilder() {
        val builder =
            ServerCapabilitiesBuilder()
                .enableLogging()
                .enableExperimental()
                .enablePrompts()
                .enableResources()
                .enableTools()
                .enableToolListChanged()
        assertEquals(emptyJsonObject(), builder.logging)
        assertEquals(PromptsCapability(), builder.prompts)
        assertEquals(ResourcesCapability(), builder.resources)
        assertEquals(ToolsCapability(listChanged = true), builder.tools)
        assertEquals(mutableMapOf(), builder.experimental)
        val clientBuilder =
            ClientCapabilitiesBuilder()
                .enableExperimental()
                .enableRoots()
                .enableRootsListChanged()
                .enableSampling()
        assertEquals(mutableMapOf(), clientBuilder.experimental)
        assertEquals(RootsCapabilities(listChanged = true), clientBuilder.roots)
    }

    @Test
    fun testTaskCapabilitiesDeserialization() {
        val json =
            """
            {
                "list": {},
                "cancel": {},
                "requests": {
                    "tools": { "call": {} }
                }
            }
            """.trimIndent()

        val tasks = capabilitiesTestJson.decodeFromString<TasksCapability>(json)
        assertNotNull(tasks.list)
        assertNotNull(tasks.cancel)
        val requests = assertNotNull(tasks.requests)
        val tools = assertNotNull(requests.tools)
        assertNotNull(tools.call)
    }

    @Test
    fun testTasksCapabilityClientDefault() {
        val tasks = TasksCapability.clientDefault()

        // Verify structure
        assertTrue(tasks.supportsList())
        assertTrue(tasks.supportsCancel())
        assertTrue(tasks.supportsSamplingCreateMessage())
        assertTrue(tasks.supportsElicitationCreate())
        assertFalse(tasks.supportsToolsCall())

        // Verify serialization matches expected format
        val json = capabilitiesTestJson.parseToJsonElement(capabilitiesTestJson.encodeToString(tasks)).jsonObject
        assertEquals(emptyJsonObject(), json["list"])
        assertEquals(emptyJsonObject(), json["cancel"])
        assertEquals(emptyJsonObject(), json["requests"]!!.jsonObject["sampling"]!!.jsonObject["createMessage"])
        assertEquals(emptyJsonObject(), json["requests"]!!.jsonObject["elicitation"]!!.jsonObject["create"])
    }

    @Test
    fun testTasksCapabilityServerDefault() {
        val tasks = TasksCapability.serverDefault()

        // Verify structure
        assertTrue(tasks.supportsList())
        assertTrue(tasks.supportsCancel())
        assertTrue(tasks.supportsToolsCall())
        assertFalse(tasks.supportsSamplingCreateMessage())
        assertFalse(tasks.supportsElicitationCreate())

        // Verify serialization matches expected format
        val json = capabilitiesTestJson.parseToJsonElement(capabilitiesTestJson.encodeToString(tasks)).jsonObject
        assertEquals(emptyJsonObject(), json["list"])
        assertEquals(emptyJsonObject(), json["cancel"])
        assertEquals(emptyJsonObject(), json["requests"]!!.jsonObject["tools"]!!.jsonObject["call"])
    }

    @Test
    fun testClientExtensionsCapability() {
        val extensions = mutableMapOf<String, JsonObject>()
        extensions["io.modelcontextprotocol/ui"] =
            buildJsonObject {
                put("mimeTypes", JsonArray(listOf(JsonPrimitive("text/html;profile=mcp-app"))))
            }

        val capabilities =
            ClientCapabilities
                .builder()
                .enableExtensionsWith(extensions)
                .enableSampling()
                .build()

        // Verify serialization matches MCP Apps spec format
        val json = capabilitiesTestJson.parseToJsonElement(capabilitiesTestJson.encodeToString(capabilities)).jsonObject
        assertEquals(
            JsonArray(listOf(JsonPrimitive("text/html;profile=mcp-app"))),
            json["extensions"]!!.jsonObject["io.modelcontextprotocol/ui"]!!.jsonObject["mimeTypes"],
        )
        assertTrue(json["sampling"]!!.jsonObject.isEmpty())
    }

    @Test
    fun testServerExtensionsCapability() {
        val extensions = mutableMapOf<String, JsonObject>()
        extensions["io.modelcontextprotocol/apps"] = emptyJsonObject()

        val capabilities =
            ServerCapabilities
                .builder()
                .enableExtensionsWith(extensions)
                .enableTools()
                .build()

        // Verify serialization
        val json = capabilitiesTestJson.parseToJsonElement(capabilitiesTestJson.encodeToString(capabilities)).jsonObject
        assertTrue(json["extensions"]!!.jsonObject["io.modelcontextprotocol/apps"]!!.jsonObject.isEmpty())
        assertTrue(json["tools"]!!.jsonObject.isEmpty())
    }

    @Test
    fun testExtensionsDeserialization() {
        val json =
            """
            {
                "extensions": {
                    "io.modelcontextprotocol/ui": {
                        "mimeTypes": ["text/html;profile=mcp-app"]
                    }
                },
                "sampling": {}
            }
            """.trimIndent()

        val capabilities = capabilitiesTestJson.decodeFromString<ClientCapabilities>(json)
        val extensions = assertNotNull(capabilities.extensions)
        assertTrue(extensions.containsKey("io.modelcontextprotocol/ui"))
        val uiExt = assertNotNull(extensions["io.modelcontextprotocol/ui"])
        assertTrue(uiExt.containsKey("mimeTypes"))
    }

    @Test
    fun testExtensionsEmptySettings() {
        val extensions = mutableMapOf<String, JsonObject>()
        extensions["io.modelcontextprotocol/oauth-client-credentials"] = emptyJsonObject()

        val capabilities =
            ClientCapabilities
                .builder()
                .enableExtensionsWith(extensions)
                .build()

        val json = capabilitiesTestJson.parseToJsonElement(capabilitiesTestJson.encodeToString(capabilities)).jsonObject
        assertEquals(
            emptyJsonObject(),
            json["extensions"]!!.jsonObject["io.modelcontextprotocol/oauth-client-credentials"],
        )
    }
}
