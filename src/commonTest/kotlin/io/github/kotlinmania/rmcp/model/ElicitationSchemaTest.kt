// port-lint: tests rmcp/src/model/elicitation_schema.rs
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val elicitationTestJson = Json {
    explicitNulls = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class ElicitationSchemaTest {
    @Test
    fun testStringSchemaSerialization() {
        val schema = StringSchema.email().copy(descriptionText = "Email address")
        val jsonStr = elicitationTestJson.encodeToString(StringSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals("email", json["format"]?.jsonPrimitive?.content)
        assertEquals("Email address", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testNumberSchemaSerialization() {
        val schema = NumberSchema.new().range(0.0, 100.0).copy(descriptionText = "Percentage")
        val jsonStr = elicitationTestJson.encodeToString(NumberSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("number", json["type"]?.jsonPrimitive?.content)
        assertEquals(0.0, json["minimum"]?.jsonPrimitive?.double)
        assertEquals(100.0, json["maximum"]?.jsonPrimitive?.double)
    }

    @Test
    fun testIntegerSchemaSerialization() {
        val schema = IntegerSchema.new().range(0L, 150L)
        val jsonStr = elicitationTestJson.encodeToString(IntegerSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("integer", json["type"]?.jsonPrimitive?.content)
        assertEquals("0", json["minimum"]?.jsonPrimitive?.content)
        assertEquals("150", json["maximum"]?.jsonPrimitive?.content)
    }

    @Test
    fun testBooleanSchemaSerialization() {
        val schema = BooleanSchema.new().withDefault(true)
        val jsonStr = elicitationTestJson.encodeToString(BooleanSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("boolean", json["type"]?.jsonPrimitive?.content)
        assertEquals("true", json["default"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaUntitledSingleSelectSerialization() {
        val schema = EnumSchema.builder(listOf("US", "UK"))
            .description("Country code")
            .build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals(listOf("US", "UK"), json["enum"]?.jsonArray?.map { it.jsonPrimitive.content })
        assertEquals("Country code", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaUntitledMultiSelectSerialization() {
        val schema = EnumSchema.builder(listOf("US", "UK"))
            .multiselect()
            .minItems(1uL).getOrThrow()
            .maxItems(4uL).getOrThrow()
            .description("Country code")
            .build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("array", json["type"]?.jsonPrimitive?.content)
        assertEquals("1", json["minItems"]?.jsonPrimitive?.content)
        assertEquals("4", json["maxItems"]?.jsonPrimitive?.content)
        assertEquals("Country code", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaTitledSingleSelectSerialization() {
        val schema = EnumSchema.builder(listOf("US", "UK"))
            .enumTitles(listOf("United States", "United Kingdom")).getOrThrow()
            .description("Country code")
            .build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals("Country code", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaLegacySerialization() {
        val schema = EnumSchema.Legacy(
            LegacyEnumSchema(
                type = StringTypeConst.value,
                title = "Legacy Enum",
                descriptionText = "A legacy enum schema",
                values = listOf("A", "B"),
                enumNames = listOf("Option A", "Option B"),
            )
        )
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals("Legacy Enum", json["title"]?.jsonPrimitive?.content)
        assertEquals("A legacy enum schema", json["description"]?.jsonPrimitive?.content)
        assertEquals(listOf("A", "B"), json["enum"]?.jsonArray?.map { it.jsonPrimitive.content })
        assertEquals(listOf("Option A", "Option B"), json["enumNames"]?.jsonArray?.map { it.jsonPrimitive.content })
    }

    @Test
    fun testEnumSchemaTitledMultiSelectSerialization() {
        val schema = EnumSchema.builder(listOf("US", "UK"))
            .enumTitles(listOf("United States", "United Kingdom")).getOrThrow()
            .multiselect()
            .minItems(1uL).getOrThrow()
            .maxItems(4uL).getOrThrow()
            .description("Country code")
            .build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("array", json["type"]?.jsonPrimitive?.content)
        assertEquals("1", json["minItems"]?.jsonPrimitive?.content)
        assertEquals("4", json["maxItems"]?.jsonPrimitive?.content)
        assertEquals("Country code", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testPrimitiveSchemaEnumDeserialization() {
        val jsonEnum = """{"type":"string","enum":["a","b"]}"""
        val schemaEnum = elicitationTestJson.decodeFromString(PrimitiveSchema.serializer(), jsonEnum)
        assertTrue(schemaEnum is PrimitiveSchema.EnumVariant)

        val jsonString = """{"type":"string"}"""
        val schemaString = elicitationTestJson.decodeFromString(PrimitiveSchema.serializer(), jsonString)
        assertTrue(schemaString is PrimitiveSchema.StringVariant)
    }

    @Test
    fun testEnumSchemaSingleSelectWithDefault() {
        val schema = EnumSchema.builder(listOf("red", "green", "blue"))
            .withDefault("green").getOrThrow()
            .description("Favorite color")
            .build()

        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals("green", json["default"]?.jsonPrimitive?.content)
        assertEquals("Favorite color", json["description"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaMultiSelectWithDefault() {
        val schema = EnumSchema.builder(listOf("red", "green", "blue"))
            .multiselect()
            .withDefault(listOf("red", "blue")).getOrThrow()
            .minItems(1uL).getOrThrow()
            .maxItems(3uL).getOrThrow()
            .build()

        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("array", json["type"]?.jsonPrimitive?.content)
        assertEquals(listOf("red", "blue"), json["default"]?.jsonArray?.map { it.jsonPrimitive.content })
        assertEquals("1", json["minItems"]?.jsonPrimitive?.content)
        assertEquals("3", json["maxItems"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaTransitionClearsDefaults() {
        val builder = EnumSchema.builder(listOf("A", "B"))
            .withDefault("A").getOrThrow()

        val schema = builder.multiselect().build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("array", json["type"]?.jsonPrimitive?.content)
        assertNull(json["default"])
    }

    @Test
    fun testEnumSchemaMultiToSingleTransition() {
        val builder = EnumSchema.builder(listOf("A", "B", "C"))
            .multiselect()
            .withDefault(listOf("A", "B")).getOrThrow()
            .minItems(1uL).getOrThrow()

        val schema = builder.singleSelect().build()
        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertNull(json["default"])
        assertNull(json["minItems"])
        assertNull(json["maxItems"])
    }

    @Test
    fun testEnumSchemaInvalidSingleDefault() {
        val result = EnumSchema.builder(listOf("A", "B")).withDefault("C")
        assertTrue(result.isFailure)
        assertEquals("Provided default value is not in enum values", result.exceptionOrNull()?.message)
    }

    @Test
    fun testEnumSchemaInvalidMultiDefault() {
        val result = EnumSchema.builder(listOf("A", "B"))
            .multiselect()
            .withDefault(listOf("A", "C"))
        assertTrue(result.isFailure)
        assertEquals("One of the provided default values is not in enum values", result.exceptionOrNull()?.message)
    }

    @Test
    fun testEnumSchemaTitledWithDefault() {
        val schema = EnumSchema.builder(listOf("US", "UK"))
            .enumTitles(listOf("United States", "United Kingdom")).getOrThrow()
            .withDefault("UK").getOrThrow()
            .build()

        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals("UK", json["default"]?.jsonPrimitive?.content)
    }

    @Test
    fun testEnumSchemaUntitledAfterTitled() {
        val schema = EnumSchema.builder(listOf("A", "B"))
            .enumTitles(listOf("Option A", "Option B")).getOrThrow()
            .untitled()
            .build()

        val jsonStr = elicitationTestJson.encodeToString(EnumSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("string", json["type"]?.jsonPrimitive?.content)
        assertEquals(listOf("A", "B"), json["enum"]?.jsonArray?.map { it.jsonPrimitive.content })
        assertNull(json["oneOf"])
    }

    @Test
    fun testElicitationSchemaBuilderSimple() {
        val schema = ElicitationSchema.builder()
            .requiredEmail("email")
            .optionalBool("newsletter", false)
            .build()
            .getOrThrow()

        assertEquals(2, schema.properties.size)
        assertTrue(schema.properties.containsKey("email"))
        assertTrue(schema.properties.containsKey("newsletter"))
        assertEquals(listOf("email"), schema.required)
    }

    @Test
    fun testElicitationSchemaBuilderComplex() {
        val enumSchema = EnumSchema.builder(listOf("US", "UK", "CA")).build()
        val schema = ElicitationSchema.builder()
            .requiredStringWith("name") { s -> s.length(1u, 100u) }
            .requiredInteger("age", 0L, 150L)
            .optionalBool("newsletter", false)
            .requiredEnumSchema("country", enumSchema)
            .description("User registration")
            .build()
            .getOrThrow()

        assertEquals(4, schema.properties.size)
        assertEquals(listOf("name", "age", "country"), schema.required)
        assertEquals("User registration", schema.descriptionText)
    }

    @Test
    fun testElicitationSchemaSerialization() {
        val schema = ElicitationSchema.builder()
            .requiredStringWith("name") { s -> s.length(1u, 100u) }
            .build()
            .getOrThrow()

        val jsonStr = elicitationTestJson.encodeToString(ElicitationSchema.serializer(), schema)
        val json = elicitationTestJson.parseToJsonElement(jsonStr).jsonObject

        assertEquals("object", json["type"]?.jsonPrimitive?.content)
        assertTrue(json["properties"]?.jsonObject?.containsKey("name") == true)
        assertEquals(listOf("name"), json["required"]?.jsonArray?.map { it.jsonPrimitive.content })
    }

    @Test
    fun testIntegerRangeValidation() {
        assertFailsWith<IllegalArgumentException> {
            IntegerSchema.new().range(10L, 5L)
        }
    }

    @Test
    fun testStringLengthValidation() {
        assertFailsWith<IllegalArgumentException> {
            StringSchema.new().length(10u, 5u)
        }
    }

    @Test
    fun testIntegerRangeValidationWithResult() {
        val result = IntegerSchema.new().withRange(10L, 5L)
        assertTrue(result.isFailure)
        assertEquals("minimum must be <= maximum", result.exceptionOrNull()?.message)
    }
}
