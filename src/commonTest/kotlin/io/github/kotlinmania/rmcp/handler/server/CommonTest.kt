// port-lint: tests rmcp/src/handler/server/common.rs
package io.github.kotlinmania.rmcp.handler.server

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Serializable
private data class TestObject(
    val value: Int,
)

@Serializable
private data class AnotherTestObject(
    val value: Int,
)

class CommonTest {
    @Test
    fun testSchemaForTypeHandlesPrimitive() {
        val schema = schemaForType<Int>()

        assertEquals(JsonPrimitive("integer"), schema["type"])
    }

    @Test
    fun testSchemaForTypeHandlesArray() {
        val schema = schemaForType<List<Int>>()

        assertEquals(JsonPrimitive("array"), schema["type"])
        val items = schema["items"]?.jsonObject
        assertEquals(JsonPrimitive("integer"), assertNotNull(items)["type"])
    }

    @Test
    fun testSchemaForTypeHandlesStruct() {
        val schema = schemaForType<TestObject>()

        assertEquals(JsonPrimitive("object"), schema["type"])
        val properties = schema["properties"]?.jsonObject
        assertTrue(assertNotNull(properties).containsKey("value"))
    }

    @Test
    fun testSchemaForTypeCachesPrimitiveTypes() {
        val schema1 = schemaForType<Int>()
        val schema2 = schemaForType<Int>()

        assertSame(schema1, schema2)
    }

    @Test
    fun testSchemaForTypeCachesStructTypes() {
        val schema1 = schemaForType<TestObject>()
        val schema2 = schemaForType<TestObject>()

        assertSame(schema1, schema2)
    }

    @Test
    fun testSchemaForTypeDifferentTypesDifferentSchemas() {
        val schema1 = schemaForType<TestObject>()
        val schema2 = schemaForType<AnotherTestObject>()

        assertFalse(schema1 === schema2)
    }

    @Test
    fun testSchemaForTypeArcCanBeShared() {
        val schema = schemaForType<TestObject>()
        val cloned = schema

        assertSame(schema, cloned)
    }

    @Test
    fun testSchemaForOutputRejectsPrimitive() {
        val result = schemaForOutput<Int>()
        assertTrue(result.isFailure)
    }

    @Test
    fun testSchemaForOutputAcceptsObject() {
        val result = schemaForOutput<TestObject>()
        assertTrue(result.isSuccess)
    }
}
