// port-lint: source rmcp/src/handler/server/tool_name_validation.rs
package io.github.kotlinmania.rmcp.handler.server

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolNameValidationTest {
    @Test
    fun testValidToolNames() {
        val maxLengthName = "a".repeat(128)
        val validNames =
            listOf(
                "my_tool",
                "MyTool",
                "my-tool",
                "my.tool",
                "tool123",
                "a",
                maxLengthName,
            )

        for (name in validNames) {
            val result = validateToolName(name)
            assertTrue(result.isValid, "Tool name '$name' should be valid")
        }
    }

    @Test
    fun testEmptyToolName() {
        val result = validateToolName("")
        assertFalse(result.isValid)
        assertTrue("Tool name cannot be empty" in result.warnings)
    }

    @Test
    fun testTooLongToolName() {
        val name = "a".repeat(129)
        val result = validateToolName(name)
        assertFalse(result.isValid)
        assertTrue(result.warnings[0].contains("exceeds maximum length"))
    }

    @Test
    fun testToolNameWithSpaces() {
        val result = validateToolName("my tool")
        assertFalse(result.isValid)
        assertTrue(result.warnings.any { it.contains("contains spaces") })
    }

    @Test
    fun testToolNameWithCommas() {
        val result = validateToolName("my,tool")
        assertFalse(result.isValid)
        assertTrue(result.warnings.any { it.contains("contains commas") })
    }

    @Test
    fun testToolNameStartingWithDash() {
        val result = validateToolName("-tool")
        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("starts or ends with a dash") })
    }

    @Test
    fun testToolNameEndingWithDot() {
        val result = validateToolName("tool.")
        assertTrue(result.isValid)
        assertTrue(result.warnings.any { it.contains("starts or ends with a dot") })
    }

    @Test
    fun testToolNameWithInvalidCharacters() {
        val result = validateToolName("my@tool")
        assertFalse(result.isValid)
        assertTrue(result.warnings.any { it.contains("contains invalid characters") })
    }

    @Test
    fun testToolNameAllSpecialCharactersAllowed() {
        val validChars = listOf('_', '-', '.')
        for (ch in validChars) {
            val name = "tool$ch"
            val result = validateToolName(name)
            assertTrue(result.isValid, "Tool name with character '$ch' should be valid")
        }
    }

    @Test
    fun testMinimumLength() {
        val result = validateToolName("a")
        assertTrue(result.isValid)
    }

    @Test
    fun testMaximumLength() {
        val name = "a".repeat(128)
        val result = validateToolName(name)
        assertTrue(result.isValid)
    }
}
