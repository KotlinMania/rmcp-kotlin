// port-lint: tests rmcp/src/model.rs
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val modelTestJson =
    Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

class ModelTest {
    @Test
    fun testProtocolVersionOrder() {
        val v1 = ProtocolVersion.V_2024_11_05
        val v2 = ProtocolVersion.V_2025_03_26
        assertTrue(v1 < v2)
    }

    @Test
    fun testIconSerialization() {
        val icon =
            Icon(
                src = "https://example.com/icon.png",
                mimeType = "image/png",
                sizes = listOf("48x48"),
            )

        val jsonStr = modelTestJson.encodeToString(Icon.serializer(), icon)
        val json = modelTestJson.parseToJsonElement(jsonStr).jsonObject
        assertEquals("https://example.com/icon.png", json["src"]?.jsonPrimitive?.content)
        assertEquals("image/png", json["mimeType"]?.jsonPrimitive?.content)

        val deserialized = modelTestJson.decodeFromString(Icon.serializer(), jsonStr)
        assertEquals(icon, deserialized)
    }

    @Test
    fun testIconMinimal() {
        val icon =
            Icon(
                src = "data:image/svg+xml;base64,PHN2Zy8+",
            )

        val jsonStr = modelTestJson.encodeToString(Icon.serializer(), icon)
        val json = modelTestJson.parseToJsonElement(jsonStr).jsonObject
        assertEquals("data:image/svg+xml;base64,PHN2Zy8+", json["src"]?.jsonPrimitive?.content)
        assertNull(json["mimeType"])
        assertNull(json["sizes"])
    }

    @Test
    fun testImplementationWithIcons() {
        val implementation =
            Implementation(
                name = "test-server",
                title = "Test Server",
                version = "1.0.0",
                descriptionText = "A test server for unit testing",
                icons =
                    listOf(
                        Icon(
                            src = "https://example.com/icon.png",
                            mimeType = "image/png",
                            sizes = listOf("48x48"),
                        ),
                        Icon(
                            src = "https://example.com/icon.svg",
                            mimeType = "image/svg+xml",
                            sizes = listOf("any"),
                        ),
                    ),
                websiteUrl = "https://example.com",
            )

        val jsonStr = modelTestJson.encodeToString(Implementation.serializer(), implementation)
        val json = modelTestJson.parseToJsonElement(jsonStr).jsonObject
        assertEquals("test-server", json["name"]?.jsonPrimitive?.content)
        assertEquals("A test server for unit testing", json["description"]?.jsonPrimitive?.content)
        assertEquals("https://example.com", json["websiteUrl"]?.jsonPrimitive?.content)

        val deserialized = modelTestJson.decodeFromString(Implementation.serializer(), jsonStr)
        assertEquals(implementation, deserialized)
    }

    @Test
    fun testBackwardCompatibility() {
        val oldJson =
            """
            {
                "name": "legacy-server",
                "version": "0.9.0"
            }
            """.trimIndent()

        val implementation = modelTestJson.decodeFromString(Implementation.serializer(), oldJson)
        assertEquals("legacy-server", implementation.name)
        assertEquals("0.9.0", implementation.version)
        assertNull(implementation.descriptionText)
        assertNull(implementation.icons)
        assertNull(implementation.websiteUrl)
    }

    @Test
    fun testInitializeWithIcons() {
        val initResult =
            InitializeResult(
                protocolVersion = ProtocolVersion(),
                capabilities = ServerCapabilities(),
                serverInfo =
                    Implementation(
                        name = "icon-server",
                        version = "2.0.0",
                        icons =
                            listOf(
                                Icon(
                                    src = "https://example.com/server.png",
                                    mimeType = "image/png",
                                    sizes = listOf("48x48"),
                                ),
                            ),
                        websiteUrl = "https://docs.example.com",
                    ),
            )

        val jsonStr = modelTestJson.encodeToString(InitializeResult.serializer(), initResult)
        val deserialized = modelTestJson.decodeFromString(InitializeResult.serializer(), jsonStr)
        assertEquals(initResult, deserialized)
    }

    @Test
    fun testNegativeAndLargeRequestIds() {
        val id1 = NumberOrString.Number(-1L)
        val json1 = modelTestJson.encodeToString(RequestId.serializer(), id1)
        assertEquals("-1", json1)
        val deser1 = modelTestJson.decodeFromString(RequestId.serializer(), json1)
        assertEquals(id1, deser1)

        val id2 = NumberOrString.Number(9007199254740991L)
        val json2 = modelTestJson.encodeToString(RequestId.serializer(), id2)
        assertEquals("9007199254740991", json2)
        val deser2 = modelTestJson.decodeFromString(RequestId.serializer(), json2)
        assertEquals(id2, deser2)
    }

    @Test
    fun testNotificationSerde() {
        val raw = """{"jsonrpc":"2.0","method":"notifications/initialized"}"""
        val message = modelTestJson.decodeFromString<ClientJsonRpcMessage>(raw)
        assertTrue(message is JsonRpcMessage.NotificationMessage)
        val json = modelTestJson.encodeToString<ClientJsonRpcMessage>(message)
        assertEquals(raw, json)
    }

    @Test
    fun testCustomClientNotificationRoundtrip() {
        val raw = """{"jsonrpc":"2.0","method":"notifications/custom","params":{"foo":"bar"}}"""
        val message = modelTestJson.decodeFromString<ClientJsonRpcMessage>(raw)
        assertTrue(message is JsonRpcMessage.NotificationMessage)
        val notification = (message.value.notification as? ClientNotification.CustomNotification)?.value
        assertEquals("notifications/custom", notification?.method)
        assertEquals(
            "bar",
            notification
                ?.params
                ?.jsonObject
                ?.get("foo")
                ?.jsonPrimitive
                ?.content,
        )

        val json = modelTestJson.encodeToString<ClientJsonRpcMessage>(message)
        assertEquals(raw, json)
    }

    @Test
    fun testCustomServerNotificationRoundtrip() {
        val raw = """{"jsonrpc":"2.0","method":"notifications/custom-server","params":{"hello":"world"}}"""
        val message = modelTestJson.decodeFromString<ServerJsonRpcMessage>(raw)
        assertTrue(message is JsonRpcMessage.NotificationMessage)
        val notification = (message.value.notification as? ServerNotification.CustomNotification)?.value
        assertEquals("notifications/custom-server", notification?.method)
        assertEquals(
            "world",
            notification
                ?.params
                ?.jsonObject
                ?.get("hello")
                ?.jsonPrimitive
                ?.content,
        )

        val json = modelTestJson.encodeToString<ServerJsonRpcMessage>(message)
        assertEquals(raw, json)
    }

    @Test
    fun testCustomRequestRoundtrip() {
        val raw = """{"jsonrpc":"2.0","id":42,"method":"requests/custom","params":{"foo":"bar"}}"""
        val message = modelTestJson.decodeFromString<ClientJsonRpcMessage>(raw)
        assertTrue(message is JsonRpcMessage.RequestMessage)
        assertEquals(NumberOrString.Number(42L), message.value.id)
        val request = (message.value.request as? ClientRequest.CustomRequest)?.value
        assertEquals("requests/custom", request?.method)

        val json = modelTestJson.encodeToString<ClientJsonRpcMessage>(message)
        assertEquals(raw, json)
    }

    @Test
    fun testInitialRequestResponseSerde() {
        val requestJson =
            """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "initialize",
              "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {
                  "roots": {
                    "listChanged": true
                  },
                  "sampling": {}
                },
                "clientInfo": {
                  "name": "ExampleClient",
                  "version": "1.0.0"
                }
              }
            }
            """.trimIndent()
        val message = modelTestJson.decodeFromString<ClientJsonRpcMessage>(requestJson)
        assertTrue(message is JsonRpcMessage.RequestMessage)
        assertEquals(NumberOrString.Number(1L), message.value.id)
    }

    @Test
    fun testElicitationDeserializationUntagged() {
        val jsonStr =
            """
            {
                "message": "Please provide more details.",
                "requestedSchema": {
                    "title": "User Details",
                    "type": "object",
                    "properties": {
                        "name": { "type": "string" },
                        "age": { "type": "integer" }
                    },
                    "required": ["name", "age"]
                }
            }
            """.trimIndent()
        val elicitation = modelTestJson.decodeFromString(CreateElicitationRequestParams.serializer(), jsonStr)
        assertTrue(elicitation is CreateElicitationRequestParams.FormElicitationParams)
        assertEquals("Please provide more details.", elicitation.message)
        assertEquals("User Details", elicitation.requestedSchema.title)
    }

    @Test
    fun testElicitationDeserialization() {
        val jsonForm =
            """
            {
                "_meta": { "meta_form_key_1": "meta form value 1" },
                "mode": "form",
                "message": "Please provide more details.",
                "requestedSchema": {
                    "title": "User Details",
                    "type": "object",
                    "properties": {
                        "name": { "type": "string" },
                        "age": { "type": "integer" }
                    },
                    "required": ["name", "age"]
                }
            }
            """.trimIndent()
        val elicitationForm = modelTestJson.decodeFromString(CreateElicitationRequestParams.serializer(), jsonForm)
        assertTrue(elicitationForm is CreateElicitationRequestParams.FormElicitationParams)
        assertEquals("Please provide more details.", elicitationForm.message)

        val jsonUrl =
            """
            {
                "_meta": { "meta_url_key_1": "meta url value 1" },
                "mode": "url",
                "message": "Please fill out the form at the following URL.",
                "url": "https://example.com/form",
                "elicitationId": "elicitation-123"
            }
            """.trimIndent()
        val elicitationUrl = modelTestJson.decodeFromString(CreateElicitationRequestParams.serializer(), jsonUrl)
        assertTrue(elicitationUrl is CreateElicitationRequestParams.UrlElicitationParams)
        assertEquals("https://example.com/form", elicitationUrl.url)
        assertEquals("elicitation-123", elicitationUrl.elicitationId)
    }

    @Test
    fun testElicitationSerialization() {
        val urlElicitation =
            CreateElicitationRequestParams.UrlElicitationParams(
                metaValue =
                    Meta(
                        kotlinx.serialization.json.buildJsonObject {
                            put("meta_url_key_1", kotlinx.serialization.json.JsonPrimitive("meta url value 1"))
                        },
                    ),
                message = "Please fill out the form at the following URL.",
                url = "https://example.com/form",
                elicitationId = "elicitation-123",
            )
        val jsonUrl = modelTestJson.encodeToString(CreateElicitationRequestParams.serializer(), urlElicitation)
        val parsed = modelTestJson.parseToJsonElement(jsonUrl).jsonObject
        assertEquals("url", parsed["mode"]?.jsonPrimitive?.content)
        assertEquals("https://example.com/form", parsed["url"]?.jsonPrimitive?.content)
        assertEquals("elicitation-123", parsed["elicitationId"]?.jsonPrimitive?.content)
    }
}
