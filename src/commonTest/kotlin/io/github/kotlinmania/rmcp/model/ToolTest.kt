// port-lint: tests model/tool.rs
@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val toolTestJson =
    Json {
        explicitNulls = false
    }

@Serializable
data class InputData(
    val name: String,
    val age: UInt,
)

@Serializable
data class OutputData(
    val greeting: String,
    val isAdult: Boolean,
)

class ToolTest {
    @Test
    fun testWithOutputSchema() {
        val tool = Tool.new("test", "Test tool", buildJsonObject {}).withOutputSchema<OutputData>()

        assertNotNull(tool.outputSchema)

        val schemaStr = toolTestJson.encodeToString(tool.outputSchema)
        assertTrue(schemaStr.contains("greeting"))
        assertTrue(schemaStr.contains("isAdult"))
    }

    @Test
    fun testWithInputSchema() {
        val tool = Tool.new("test", "Test tool", buildJsonObject {}).withInputSchema<InputData>()

        val schemaStr = toolTestJson.encodeToString(tool.inputSchema)
        assertTrue(schemaStr.contains("name"))
        assertTrue(schemaStr.contains("age"))
    }

    @Test
    fun testChainedBuilderMethods() {
        val tool =
            Tool
                .new("test", "Test tool", buildJsonObject {})
                .withInputSchema<InputData>()
                .withOutputSchema<OutputData>()
                .annotate(ToolAnnotations.new().readOnly(true))

        assertNotNull(tool.outputSchema)
        assertNotNull(tool.annotations)
        assertEquals(true, tool.annotations.readOnlyHint)

        val inputSchemaStr = toolTestJson.encodeToString(tool.inputSchema)
        assertTrue(inputSchemaStr.contains("name"))
        assertTrue(inputSchemaStr.contains("age"))

        val outputSchemaStr = toolTestJson.encodeToString(tool.outputSchema)
        assertTrue(outputSchemaStr.contains("greeting"))
        assertTrue(outputSchemaStr.contains("isAdult"))
    }

    @Test
    fun testSerializeToolResultWithMeta() {
        val content = listOf(Content.text("ok"))
        val meta =
            Meta(
                buildJsonObject {
                    put("foo", JsonPrimitive("bar"))
                },
            )
        val result =
            CallToolResult(
                content = content,
                structuredContent = null,
                isError = false,
                meta = meta,
            )
        val jsonStr = toolTestJson.encodeToString(result)
        val expected = """{"content":[{"raw":{"type":"text","text":"ok"}}],"isError":false,"_meta":{"values":{"foo":"bar"}}}"""
        assertEquals(toolTestJson.parseToJsonElement(expected), toolTestJson.parseToJsonElement(jsonStr))
    }

    @Test
    fun testDeserializeToolResultWithMeta() {
        val raw = """{"content":[{"raw":{"type":"text","text":"hello"}}],"isError":true,"_meta":{"values":{"a":1,"b":"two"}}}"""
        val result = toolTestJson.decodeFromString<CallToolResult>(raw)
        assertEquals(true, result.isError)
        assertEquals(1, result.content.size)
        val meta = assertNotNull(result.meta)
        assertEquals(JsonPrimitive(1), meta["a"])
        assertEquals(JsonPrimitive("two"), meta["b"])
    }

    @Test
    fun testSerializeToolResultWithoutMetaOmitsField() {
        val result = CallToolResult.success(listOf(Content.text("no meta")))
        val jsonStr = toolTestJson.encodeToString(result)
        assertFalse(jsonStr.contains("_meta"))
    }

    @Test
    fun testToolExecutionAndTaskSupport() {
        val tool =
            Tool
                .new("task-tool", "Task tool", buildJsonObject {})
                .withExecution(ToolExecution.new().withTaskSupport(TaskSupport.Required))

        assertEquals(TaskSupport.Required, tool.taskSupport())

        val defaultTool = Tool.new("default-tool", "Default tool", buildJsonObject {})
        assertEquals(TaskSupport.Forbidden, defaultTool.taskSupport())
    }

    @Test
    fun testToolAnnotationsHints() {
        val annotations =
            ToolAnnotations
                .withTitle("My Tool")
                .readOnly(false)
                .destructive(true)
                .idempotent(false)
                .openWorld(true)

        assertEquals("My Tool", annotations.title)
        assertEquals(false, annotations.readOnlyHint)
        assertEquals(true, annotations.destructiveHint)
        assertEquals(false, annotations.idempotentHint)
        assertEquals(true, annotations.openWorldHint)
        assertTrue(annotations.isDestructive())
        assertFalse(annotations.isIdempotent())
    }
}
