// port-lint: source model/tool.rs
package io.github.kotlinmania.rmcp.model

import io.github.kotlinmania.rmcp.handler.server.schemaForOutput
import io.github.kotlinmania.rmcp.handler.server.schemaForType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Tools represent a routine that a server can execute.
 *
 * Tool calls represent requests from the client to execute one.
 */

/**
 * A tool that can be used by a model.
 */
@Serializable
data class Tool(
    /**
     * The name of the tool.
     */
    val name: String,
    /**
     * A human-readable title for the tool.
     */
    val title: String? = null,
    /**
     * A description of what the tool does.
     */
    val description: String? = null,
    /**
     * A JSON Schema object defining the expected parameters for the tool.
     */
    @SerialName("inputSchema")
    val inputSchema: JsonObject,
    /**
     * An optional JSON Schema object defining the structure of the tool's
     * output.
     */
    @SerialName("outputSchema")
    val outputSchema: JsonObject? = null,
    /**
     * Optional additional tool information.
     */
    val annotations: ToolAnnotations? = null,
    /**
     * Execution-related configuration including task support mode.
     */
    val execution: ToolExecution? = null,
    /**
     * Optional list of icons for the tool.
     */
    val icons: List<Icon>? = null,
    /**
     * Optional additional metadata for this tool.
     */
    @SerialName("_meta")
    val meta: Meta? = null,
) {
    companion object {
        /**
         * Create a new tool with the given name and description.
         */
        fun new(name: String, description: String, inputSchema: JsonObject): Tool =
            Tool(
                name = name,
                title = null,
                description = description,
                inputSchema = inputSchema,
                outputSchema = null,
                annotations = null,
                execution = null,
                icons = null,
                meta = null,
            )
    }

    fun annotate(annotations: ToolAnnotations): Tool =
        copy(annotations = annotations)

    /**
     * Set the execution configuration for this tool.
     */
    fun withExecution(execution: ToolExecution): Tool =
        copy(execution = execution)

    /**
     * Returns the task support mode for this tool.
     *
     * Returns [TaskSupport.Forbidden] if not explicitly set.
     */
    fun taskSupport(): TaskSupport =
        execution?.taskSupport ?: TaskSupport.Forbidden

    /**
     * Set the output schema using a type that has a Kotlin serializer.
     *
     * Throws if the generated schema does not have root type `object` as
     * required by MCP specification.
     */
    inline fun <reified T> withOutputSchema(): Tool {
        val schema =
            schemaForOutput<T>().getOrElse { error ->
                throw IllegalArgumentException("Invalid output schema for tool '$name': ${error.message}", error)
            }
        return copy(outputSchema = schema)
    }

    /**
     * Set the input schema using a type that has a Kotlin serializer.
     */
    inline fun <reified T> withInputSchema(): Tool =
        copy(inputSchema = schemaForType<T>())

    /**
     * Get the schema as JSON value.
     */
    fun schemaAsJsonValue(): JsonObject =
        inputSchema
}

/**
 * Per-tool task support mode as defined in the MCP specification.
 *
 * This enum indicates whether a tool supports task-based invocation, allowing
 * clients to know how to properly call the tool.
 *
 * See Tool-Level Negotiation in the MCP tasks specification.
 */
@Serializable
enum class TaskSupport {
    /**
     * Clients MUST NOT invoke this tool as a task, the default behavior.
     */
    @SerialName("forbidden")
    Forbidden,

    /**
     * Clients MAY invoke this tool as either a task or a normal call.
     */
    @SerialName("optional")
    Optional,

    /**
     * Clients MUST invoke this tool as a task.
     */
    @SerialName("required")
    Required,
}

/**
 * Execution-related configuration for a tool.
 *
 * This struct contains settings that control how a tool should be executed,
 * including task support configuration.
 */
@Serializable
data class ToolExecution(
    /**
     * Indicates whether this tool supports task-based invocation.
     *
     * When not present or set to [TaskSupport.Forbidden], clients MUST NOT
     * invoke this tool as a task. When set to [TaskSupport.Optional], clients
     * MAY invoke this tool as a task or normal call. When set to
     * [TaskSupport.Required], clients MUST invoke this tool as a task.
     */
    @SerialName("taskSupport")
    val taskSupport: TaskSupport? = null,
) {
    companion object {
        /**
         * Create a new empty ToolExecution configuration.
         */
        fun new(): ToolExecution =
            ToolExecution()
    }

    /**
     * Set the task support mode.
     */
    fun withTaskSupport(taskSupport: TaskSupport): ToolExecution =
        copy(taskSupport = taskSupport)
}

/**
 * Additional properties describing a Tool to clients.
 *
 * NOTE: all properties in ToolAnnotations are **hints**. They are not
 * guaranteed to provide a faithful description of tool behavior, including
 * descriptive properties like `title`.
 *
 * Clients should never make tool use decisions based on ToolAnnotations
 * received from untrusted servers.
 */
@Serializable
data class ToolAnnotations(
    /**
     * A human-readable title for the tool.
     */
    val title: String? = null,
    /**
     * If true, the tool does not modify its environment.
     *
     * Default: false
     */
    @SerialName("readOnlyHint")
    val readOnlyHint: Boolean? = null,
    /**
     * If true, the tool may perform destructive updates to its environment. If
     * false, the tool performs only additive updates.
     *
     * This property is meaningful only when `readOnlyHint == false`.
     *
     * Default: true
     */
    @SerialName("destructiveHint")
    val destructiveHint: Boolean? = null,
    /**
     * If true, calling the tool repeatedly with the same arguments will have no
     * additional effect on its environment.
     *
     * This property is meaningful only when `readOnlyHint == false`.
     *
     * Default: false.
     */
    @SerialName("idempotentHint")
    val idempotentHint: Boolean? = null,
    /**
     * If true, this tool may interact with an "open world" of external
     * entities. If false, the tool's domain of interaction is closed. For
     * example, the world of a web search tool is open, whereas that of a memory
     * tool is not.
     *
     * Default: true
     */
    @SerialName("openWorldHint")
    val openWorldHint: Boolean? = null,
) {
    companion object {
        fun new(): ToolAnnotations =
            ToolAnnotations()

        fun withTitle(title: String): ToolAnnotations =
            ToolAnnotations(
                title = title,
                readOnlyHint = null,
                destructiveHint = null,
                idempotentHint = null,
                openWorldHint = null,
            )
    }

    fun readOnly(readOnly: Boolean): ToolAnnotations =
        copy(readOnlyHint = readOnly)

    fun destructive(destructive: Boolean): ToolAnnotations =
        copy(destructiveHint = destructive)

    fun idempotent(idempotent: Boolean): ToolAnnotations =
        copy(idempotentHint = idempotent)

    fun openWorld(openWorld: Boolean): ToolAnnotations =
        copy(openWorldHint = openWorld)

    /**
     * If not set, defaults to true.
     */
    fun isDestructive(): Boolean =
        destructiveHint ?: true

    /**
     * If not set, defaults to false.
     */
    fun isIdempotent(): Boolean =
        idempotentHint ?: false
}
