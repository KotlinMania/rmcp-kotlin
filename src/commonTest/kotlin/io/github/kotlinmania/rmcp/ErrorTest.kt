// port-lint: tests error.rs
package io.github.kotlinmania.rmcp

import io.github.kotlinmania.rmcp.model.ErrorCode
import io.github.kotlinmania.rmcp.model.ErrorData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorTest {
    @Test
    fun testErrorDataFormatting() {
        val err = ErrorData.new(ErrorCode.RESOURCE_NOT_FOUND, "resource not found", null)
        assertEquals("-32002: resource not found", err.fmt())
    }

    @Test
    fun testRmcpErrorHierarchy() {
        val runtimeError = RmcpError.Runtime(IllegalStateException("runtime failure"))
        assertTrue(runtimeError.message!!.contains("runtime failure"))

        val taskError = RmcpError.TaskError("task cancelled")
        assertEquals("Task error: task cancelled", taskError.message)
    }
}
