// port-lint: source rmcp/src/handler/server/common.rs
@file:OptIn(ExperimentalStdlibApi::class)

package io.github.kotlinmania.rmcp.handler.server

import io.github.kotlinmania.rmcp.model.ErrorData
import io.github.kotlinmania.rmcp.model.JsonObject
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal val schemaForTypeCache: MutableMap<KType, JsonObject> = mutableMapOf()

@PublishedApi
internal val schemaForOutputCache: MutableMap<KType, Result<JsonObject>> = mutableMapOf()

/**
 * Generates a JSON schema for a type.
 */
inline fun <reified T> schemaForType(): JsonObject =
    schemaForTypeCache.getOrPut(typeOf<T>()) {
        @OptIn(ExperimentalSerializationApi::class)
        schemaForDescriptor(serializer<T>().descriptor)
    }

/**
 * Generate and validate a JSON schema for outputSchema, which must have root
 * type "object".
 */
inline fun <reified T> schemaForOutput(): Result<JsonObject> =
    schemaForOutputCache.getOrPut(typeOf<T>()) {
        val schema = schemaForType<T>()
        when (val type = schema["type"]) {
            JsonPrimitive("object") -> Result.success(schema)
            is JsonPrimitive ->
                Result.failure(
                    IllegalArgumentException(
                        "MCP specification requires tool outputSchema to have root type 'object', but found '${type.content}'.",
                    ),
                )
            null ->
                Result.failure(
                    IllegalArgumentException(
                        "Schema is missing 'type' field. MCP specification requires outputSchema to have root type 'object'.",
                    ),
                )
            else ->
                Result.failure(
                    IllegalArgumentException(
                        "Schema 'type' field has unexpected format: $type. Expected \"object\".",
                    ),
                )
        }
    }

/**
 * Trait for extracting parts from a context, unifying tool and prompt
 * extraction.
 */
interface FromContextPart<C, T> {
    fun fromContextPart(context: C): Result<T>
}

class Extension<T>(
    val value: T,
)

data class RequestId(
    val value: io.github.kotlinmania.rmcp.model.RequestId,
)

/**
 * Trait for types that can provide access to RequestContext.
 */
interface AsRequestContext

@PublishedApi
internal fun schemaForDescriptor(descriptor: SerialDescriptor): JsonObject =
    when (descriptor.kind) {
        PrimitiveKind.INT,
        PrimitiveKind.LONG,
        PrimitiveKind.SHORT,
        PrimitiveKind.BYTE,
        -> buildJsonObject { put("type", JsonPrimitive("integer")) }

        PrimitiveKind.FLOAT,
        PrimitiveKind.DOUBLE,
        -> buildJsonObject { put("type", JsonPrimitive("number")) }

        PrimitiveKind.BOOLEAN -> buildJsonObject { put("type", JsonPrimitive("boolean")) }
        PrimitiveKind.STRING,
        PrimitiveKind.CHAR,
        -> buildJsonObject { put("type", JsonPrimitive("string")) }

        StructureKind.LIST ->
            buildJsonObject {
                put("type", JsonPrimitive("array"))
                put("items", schemaForDescriptor(descriptor.getElementDescriptor(0)))
            }

        StructureKind.CLASS,
        StructureKind.OBJECT,
        ->
            buildJsonObject {
                put("type", JsonPrimitive("object"))
                put(
                    "properties",
                    buildJsonObject {
                        for (index in 0 until descriptor.elementsCount) {
                            put(descriptor.getElementName(index), schemaForDescriptor(descriptor.getElementDescriptor(index)))
                        }
                    },
                )
            }

        else -> buildJsonObject { put("type", JsonPrimitive("object")) }
    }

fun missingContextPart(name: String): ErrorData =
    ErrorData.invalidParams("missing extension $name", null)
