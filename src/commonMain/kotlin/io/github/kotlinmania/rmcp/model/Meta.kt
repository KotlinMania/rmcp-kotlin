// port-lint: source model/meta.rs
package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

interface GetMeta {
    fun getMetaMut(): Meta

    fun getMeta(): Meta
}

interface GetExtensions {
    fun extensions(): Extensions

    fun extensionsMut(): Extensions
}

/**
 * Trait for request params that contain the `_meta` field.
 *
 * Per the MCP 2025-11-25 spec, all request params should have an optional
 * `_meta` field that can contain a `progressToken` for tracking long-running
 * operations.
 */
interface RequestParamsMeta {
    /**
     * Get a reference to the meta field.
     */
    fun meta(): Meta?

    /**
     * Replace the meta field.
     */
    fun replaceMeta(meta: Meta?)

    /**
     * Set the meta field.
     */
    fun setMeta(meta: Meta) {
        replaceMeta(meta)
    }

    /**
     * Get the progress token from meta, if present.
     */
    fun progressToken(): ProgressToken? =
        meta()?.getProgressToken()

    /**
     * Set a progress token in meta.
     */
    fun setProgressToken(token: ProgressToken) {
        val current = meta() ?: Meta.new()
        current.setProgressToken(token)
        replaceMeta(current)
    }
}

/**
 * Trait for task-augmented request params that contain both `_meta` and `task`
 * fields.
 *
 * Per the MCP 2025-11-25 spec, certain requests, like `tools/call` and
 * `sampling/createMessage`, can include a `task` field to signal that the
 * caller wants task-augmented execution.
 */
interface TaskAugmentedRequestParamsMeta : RequestParamsMeta {
    /**
     * Get a reference to the task field.
     */
    fun task(): JsonObject?

    /**
     * Replace the task field.
     */
    fun replaceTask(task: JsonObject?)

    /**
     * Set the task field.
     */
    fun setTask(task: JsonObject) {
        replaceTask(task)
    }
}

@Serializable
data class Meta(
    var values: JsonObject = buildJsonObject {},
) {
    fun getProgressToken(): ProgressToken? {
        val value = values[PROGRESS_TOKEN_FIELD] ?: return null
        val primitive = value as? JsonPrimitive ?: return null
        return if (primitive.isString) {
            ProgressToken(NumberOrString.StringValue(primitive.content))
        } else {
            primitive.longOrNull?.let { ProgressToken(NumberOrString.Number(it)) }
        }
    }

    fun setProgressToken(token: ProgressToken) {
        values = values.with(PROGRESS_TOKEN_FIELD, token.toJsonElement())
    }

    fun extend(other: Meta) {
        values = values.extendedBy(other.values)
    }

    operator fun get(key: String): JsonElement? =
        values[key]

    companion object {
        fun new(): Meta =
            Meta()

        fun withProgressToken(token: ProgressToken): Meta =
            new().also { it.setProgressToken(token) }

        fun staticEmpty(): Meta =
            EMPTY

        private val EMPTY: Meta = Meta()
    }
}

private const val PROGRESS_TOKEN_FIELD: String = "progressToken"

private fun ProgressToken.toJsonElement(): JsonPrimitive =
    when (val token = value) {
        is NumberOrString.Number -> JsonPrimitive(token.value)
        is NumberOrString.StringValue -> JsonPrimitive(token.value)
    }

private fun JsonObject.with(key: String, value: JsonElement): JsonObject =
    buildJsonObject {
        for ((existingKey, existingValue) in this@with) {
            put(existingKey, existingValue)
        }
        put(key, value)
    }

private fun JsonObject.extendedBy(other: JsonObject): JsonObject =
    buildJsonObject {
        for ((key, value) in this@extendedBy) {
            put(key, value)
        }
        for ((key, value) in other) {
            put(key, value)
        }
    }
