// port-lint: source handler/server/common.rs
@file:OptIn(ExperimentalStdlibApi::class, kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.rmcp.handler.server

import io.github.kotlinmania.rmcp.model.ErrorData
import io.github.kotlinmania.schemars.JsonSchema
import io.github.kotlinmania.schemars.Schema
import io.github.kotlinmania.schemars.Value
import io.github.kotlinmania.schemars.generate.SchemaGenerator
import io.github.kotlinmania.schemars.generate.SchemaSettings
import io.github.kotlinmania.schemars.jsonSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.BoolSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.CharSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.F32Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.F64Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.I16Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.I32Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.I64Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.I8Schema
import io.github.kotlinmania.schemars.jsonschemaimpls.OptionSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.SeqSchema
import io.github.kotlinmania.schemars.jsonschemaimpls.StringSchema
import io.github.kotlinmania.schemars.transform.AddNullable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import kotlin.native.HiddenFromObjC
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@PublishedApi
internal val schemaForTypeCache: MutableMap<KType, JsonObject> = mutableMapOf()

@PublishedApi
internal val schemaForOutputCache: MutableMap<KType, Result<JsonObject>> = mutableMapOf()

private val schemaForIdCache: MutableMap<String, JsonObject> = mutableMapOf()

private val schemaForOutputIdCache: MutableMap<String, Result<JsonObject>> = mutableMapOf()

/**
 * Generates a JSON schema for a [JsonSchema] descriptor using MCP Draft 2020-12 settings
 * with [AddNullable].
 */
@HiddenFromObjC
fun schemaForType(target: JsonSchema): JsonObject =
    schemaForIdCache.getOrPut(target.schemaId()) {
        generateMcpRootSchema(target)
    }

/**
 * Generates a JSON schema for a type.
 */
inline fun <reified T> schemaForType(): JsonObject =
    schemaForTypeCache.getOrPut(typeOf<T>()) {
        @OptIn(ExperimentalSerializationApi::class)
        schemaForDescriptor(serializer<T>().descriptor)
    }

/**
 * Generate and validate a JSON schema for `outputSchema` from a [JsonSchema], which must have
 * root type `"object"`.
 */
@HiddenFromObjC
fun schemaForOutput(target: JsonSchema): Result<JsonObject> =
    schemaForOutputIdCache.getOrPut(target.schemaId()) {
        validateOutputSchema(schemaForType(target))
    }

/**
 * Generate and validate a JSON schema for outputSchema, which must have root
 * type "object".
 */
inline fun <reified T> schemaForOutput(): Result<JsonObject> =
    schemaForOutputCache.getOrPut(typeOf<T>()) {
        validateOutputSchema(schemaForType<T>())
    }

@PublishedApi
internal fun validateOutputSchema(schema: JsonObject): Result<JsonObject> =
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
    generateMcpRootSchema(descriptorToJsonSchema(descriptor))

private fun generateMcpRootSchema(target: JsonSchema): JsonObject {
    val settings =
        SchemaSettings
            .draft202012()
            .withTransform(AddNullable())
    val generator = settings.intoGenerator()
    val rootSchema = generator.intoRootSchemaFor(target)
    val serialized = rootSchema.serialize()
    val obj =
        serialized as? Value.Object
            ?: error("Schema serialization produced non-object value: expected JSON object but got $serialized")
    return obj.toJsonObject()
}

@OptIn(ExperimentalSerializationApi::class)
private fun descriptorToJsonSchema(descriptor: SerialDescriptor): JsonSchema {
    val baseSchema: JsonSchema =
        when (descriptor.kind) {
            PrimitiveKind.BYTE -> I8Schema
            PrimitiveKind.SHORT -> I16Schema
            PrimitiveKind.INT -> I32Schema
            PrimitiveKind.LONG -> I64Schema
            PrimitiveKind.FLOAT -> F32Schema
            PrimitiveKind.DOUBLE -> F64Schema
            PrimitiveKind.BOOLEAN -> BoolSchema
            PrimitiveKind.CHAR -> CharSchema
            PrimitiveKind.STRING -> StringSchema
            StructureKind.LIST -> SeqSchema(descriptorToJsonSchema(descriptor.getElementDescriptor(0)))
            StructureKind.CLASS,
            StructureKind.OBJECT,
            ->
                object : JsonSchema {
                    override fun schemaName(): String = descriptor.serialName.substringAfterLast('.')

                    override fun schemaId(): String = descriptor.serialName

                    override fun jsonSchema(generator: SchemaGenerator): Schema =
                        jsonSchema {
                            this["type"] = "object"
                            val props = mutableMapOf<String, Value>()
                            val requiredFields = mutableListOf<Value>()
                            for (index in 0 until descriptor.elementsCount) {
                                val propName = descriptor.getElementName(index)
                                val elementDesc = descriptor.getElementDescriptor(index)
                                val propSchema = descriptorToJsonSchema(elementDesc)
                                props[propName] = generator.subschemaFor(propSchema).toValue()
                                if (!descriptor.isElementOptional(index) && !elementDesc.isNullable) {
                                    requiredFields.add(Value.Str(propName))
                                }
                            }
                            this["properties"] = Value.Object(props)
                            if (requiredFields.isNotEmpty()) {
                                this["required"] = Value.Array(requiredFields)
                            }
                        }
                }
            else ->
                object : JsonSchema {
                    override fun inlineSchema(): Boolean = true

                    override fun schemaName(): String = descriptor.serialName.substringAfterLast('.')

                    override fun schemaId(): String = descriptor.serialName

                    override fun jsonSchema(generator: SchemaGenerator): Schema =
                        jsonSchema {
                            this["type"] = "object"
                        }
                }
        }
    return if (descriptor.isNullable) OptionSchema(baseSchema) else baseSchema
}

private fun Value.Object.toJsonObject(): JsonObject =
    JsonObject(entries.mapValues { (_, v) -> v.toJsonElement() })

private fun Value.toJsonElement(): JsonElement =
    when (this) {
        is Value.Null -> JsonNull
        is Value.Bool -> JsonPrimitive(value)
        is Value.Number -> JsonPrimitive(value)
        is Value.Str -> JsonPrimitive(value)
        is Value.Array -> JsonArray(items.map { it.toJsonElement() })
        is Value.Object -> toJsonObject()
    }

fun missingContextPart(name: String): ErrorData =
    ErrorData.invalidParams("missing extension $name", null)
