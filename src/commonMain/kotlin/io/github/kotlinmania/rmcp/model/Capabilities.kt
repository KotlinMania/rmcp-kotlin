// port-lint: source model/capabilities.rs
package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

typealias ExperimentalCapabilities = MutableMap<String, JsonObject>

/**
 * MCP extension capabilities map.
 *
 * Keys are extension identifiers in the format `{vendor-prefix}/{extension-name}`
 * (for example, `io.modelcontextprotocol/ui` or
 * `io.modelcontextprotocol/oauth-client-credentials`). Values are
 * per-extension settings objects. An empty object indicates support with no
 * settings.
 */
typealias ExtensionCapabilities = MutableMap<String, JsonObject>

@Serializable
data class PromptsCapability(
    @SerialName("listChanged")
    var listChanged: Boolean? = null,
)

@Serializable
data class ResourcesCapability(
    val subscribe: Boolean? = null,
    @SerialName("listChanged")
    var listChanged: Boolean? = null,
)

@Serializable
data class ToolsCapability(
    @SerialName("listChanged")
    var listChanged: Boolean? = null,
)

@Serializable
data class RootsCapabilities(
    @SerialName("listChanged")
    var listChanged: Boolean? = null,
)

/**
 * Task capabilities shared by client and server.
 */
@Serializable
data class TasksCapability(
    val requests: TaskRequestsCapability? = null,
    val list: JsonObject? = null,
    val cancel: JsonObject? = null,
) {
    /**
     * Default client tasks capability with sampling and elicitation support.
     */
    fun clientDefault(): TasksCapability =
        clientDefault()

    /**
     * Default server tasks capability with tools/call support.
     */
    fun serverDefault(): TasksCapability =
        serverDefault()

    fun supportsList(): Boolean =
        list != null

    fun supportsCancel(): Boolean =
        cancel != null

    fun supportsToolsCall(): Boolean =
        requests?.tools?.call != null

    fun supportsSamplingCreateMessage(): Boolean =
        requests?.sampling?.createMessage != null

    fun supportsElicitationCreate(): Boolean =
        requests?.elicitation?.create != null

    companion object {
        fun clientDefault(): TasksCapability =
            TasksCapability(
                list = emptyJsonObject(),
                cancel = emptyJsonObject(),
                requests =
                    TaskRequestsCapability(
                        sampling =
                            SamplingTaskCapability(
                                createMessage = emptyJsonObject(),
                            ),
                        elicitation =
                            ElicitationTaskCapability(
                                create = emptyJsonObject(),
                            ),
                        tools = null,
                    ),
            )

        fun serverDefault(): TasksCapability =
            TasksCapability(
                list = emptyJsonObject(),
                cancel = emptyJsonObject(),
                requests =
                    TaskRequestsCapability(
                        sampling = null,
                        elicitation = null,
                        tools =
                            ToolsTaskCapability(
                                call = emptyJsonObject(),
                            ),
                    ),
            )
    }
}

/**
 * Request types that support task-augmented execution.
 */
@Serializable
data class TaskRequestsCapability(
    val sampling: SamplingTaskCapability? = null,
    val elicitation: ElicitationTaskCapability? = null,
    val tools: ToolsTaskCapability? = null,
)

@Serializable
data class SamplingTaskCapability(
    @SerialName("createMessage")
    val createMessage: JsonObject? = null,
)

@Serializable
data class ElicitationTaskCapability(
    val create: JsonObject? = null,
)

@Serializable
data class ToolsTaskCapability(
    val call: JsonObject? = null,
)

/**
 * Capability for handling elicitation requests from servers.
 *
 * Elicitation allows servers to request interactive input from users during
 * tool execution. This capability indicates that a client can handle
 * elicitation requests and present appropriate UI to users for collecting the
 * requested information.
 *
 * Capability for form mode elicitation.
 */
@Serializable
data class FormElicitationCapability(
    /**
     * Whether the client supports JSON Schema validation for elicitation
     * responses. When true, the client will validate user input against the
     * requested schema before sending the response back to the server.
     */
    @SerialName("schemaValidation")
    val schemaValidation: Boolean? = null,
)

/**
 * Capability for URL mode elicitation.
 */
@Serializable
class UrlElicitationCapability

/**
 * Elicitation allows servers to request interactive input from users during
 * tool execution. This capability indicates that a client can handle
 * elicitation requests and present appropriate UI to users for collecting the
 * requested information.
 */
@Serializable
data class ElicitationCapability(
    /**
     * Whether client supports form-based elicitation.
     */
    val form: FormElicitationCapability? = null,
    /**
     * Whether client supports URL-based elicitation.
     */
    val url: UrlElicitationCapability? = null,
)

/**
 * Sampling capability with optional sub-capabilities (SEP-1577).
 */
@Serializable
data class SamplingCapability(
    /**
     * Support for `tools` and `toolChoice` parameters.
     */
    val tools: JsonObject? = null,
    /**
     * Support for `includeContext`, soft-deprecated.
     */
    val context: JsonObject? = null,
)

/**
 * # Builder
 */
@Serializable
data class ClientCapabilities(
    val experimental: ExperimentalCapabilities? = null,
    /**
     * Optional MCP extensions that the client supports (SEP-1724).
     * Keys are extension identifiers, values are per-extension settings
     * objects. An empty object indicates support with no settings.
     */
    val extensions: ExtensionCapabilities? = null,
    val roots: RootsCapabilities? = null,
    /**
     * Capability for LLM sampling requests (SEP-1577).
     */
    val sampling: SamplingCapability? = null,
    /**
     * Capability to handle elicitation requests from servers for interactive
     * user input.
     */
    val elicitation: ElicitationCapability? = null,
    val tasks: TasksCapability? = null,
) {
    companion object {
        fun builder(): ClientCapabilitiesBuilder =
            ClientCapabilitiesBuilder()
    }
}

/**
 * ## Builder
 */
@Serializable
data class ServerCapabilities(
    val experimental: ExperimentalCapabilities? = null,
    /**
     * Optional MCP extensions that the server supports (SEP-1724).
     * Keys are extension identifiers, values are per-extension settings
     * objects. An empty object indicates support with no settings.
     */
    val extensions: ExtensionCapabilities? = null,
    val logging: JsonObject? = null,
    val completions: JsonObject? = null,
    val prompts: PromptsCapability? = null,
    val resources: ResourcesCapability? = null,
    val tools: ToolsCapability? = null,
    val tasks: TasksCapability? = null,
) {
    companion object {
        fun builder(): ServerCapabilitiesBuilder =
            ServerCapabilitiesBuilder()
    }
}

class ServerCapabilitiesBuilder(
    var experimental: ExperimentalCapabilities? = null,
    var extensions: ExtensionCapabilities? = null,
    var logging: JsonObject? = null,
    var completions: JsonObject? = null,
    var prompts: PromptsCapability? = null,
    var resources: ResourcesCapability? = null,
    var tools: ToolsCapability? = null,
    var tasks: TasksCapability? = null,
) {
    fun build(): ServerCapabilities =
        ServerCapabilities(
            experimental = experimental,
            extensions = extensions,
            logging = logging,
            completions = completions,
            prompts = prompts,
            resources = resources,
            tools = tools,
            tasks = tasks,
        )

    fun enableExperimental(): ServerCapabilitiesBuilder =
        apply { experimental = mutableMapOf() }

    fun enableExperimentalWith(experimental: ExperimentalCapabilities): ServerCapabilitiesBuilder =
        apply { this.experimental = experimental }

    fun enableExtensions(): ServerCapabilitiesBuilder =
        apply { extensions = mutableMapOf() }

    fun enableExtensionsWith(extensions: ExtensionCapabilities): ServerCapabilitiesBuilder =
        apply { this.extensions = extensions }

    fun enableLogging(): ServerCapabilitiesBuilder =
        apply { logging = emptyJsonObject() }

    fun enableLoggingWith(logging: JsonObject): ServerCapabilitiesBuilder =
        apply { this.logging = logging }

    fun enableCompletions(): ServerCapabilitiesBuilder =
        apply { completions = emptyJsonObject() }

    fun enableCompletionsWith(completions: JsonObject): ServerCapabilitiesBuilder =
        apply { this.completions = completions }

    fun enablePrompts(): ServerCapabilitiesBuilder =
        apply { prompts = PromptsCapability() }

    fun enablePromptsWith(prompts: PromptsCapability): ServerCapabilitiesBuilder =
        apply { this.prompts = prompts }

    fun enableResources(): ServerCapabilitiesBuilder =
        apply { resources = ResourcesCapability() }

    fun enableResourcesWith(resources: ResourcesCapability): ServerCapabilitiesBuilder =
        apply { this.resources = resources }

    fun enableTools(): ServerCapabilitiesBuilder =
        apply { tools = ToolsCapability() }

    fun enableToolsWith(tools: ToolsCapability): ServerCapabilitiesBuilder =
        apply { this.tools = tools }

    fun enableTasks(): ServerCapabilitiesBuilder =
        apply { tasks = TasksCapability() }

    fun enableTasksWith(tasks: TasksCapability): ServerCapabilitiesBuilder =
        apply { this.tasks = tasks }

    fun enableToolListChanged(): ServerCapabilitiesBuilder =
        apply { tools?.listChanged = true }

    fun enablePromptsListChanged(): ServerCapabilitiesBuilder =
        apply { prompts?.listChanged = true }

    fun enableResourcesListChanged(): ServerCapabilitiesBuilder =
        apply { resources = resources?.copy(listChanged = true) }

    fun enableResourcesSubscribe(): ServerCapabilitiesBuilder =
        apply { resources = resources?.copy(subscribe = true) }
}

class ClientCapabilitiesBuilder(
    var experimental: ExperimentalCapabilities? = null,
    var extensions: ExtensionCapabilities? = null,
    var roots: RootsCapabilities? = null,
    var sampling: SamplingCapability? = null,
    var elicitation: ElicitationCapability? = null,
    var tasks: TasksCapability? = null,
) {
    fun build(): ClientCapabilities =
        ClientCapabilities(
            experimental = experimental,
            extensions = extensions,
            roots = roots,
            sampling = sampling,
            elicitation = elicitation,
            tasks = tasks,
        )

    fun enableExperimental(): ClientCapabilitiesBuilder =
        apply { experimental = mutableMapOf() }

    fun enableExperimentalWith(experimental: ExperimentalCapabilities): ClientCapabilitiesBuilder =
        apply { this.experimental = experimental }

    fun enableExtensions(): ClientCapabilitiesBuilder =
        apply { extensions = mutableMapOf() }

    fun enableExtensionsWith(extensions: ExtensionCapabilities): ClientCapabilitiesBuilder =
        apply { this.extensions = extensions }

    fun enableRoots(): ClientCapabilitiesBuilder =
        apply { roots = RootsCapabilities() }

    fun enableRootsWith(roots: RootsCapabilities): ClientCapabilitiesBuilder =
        apply { this.roots = roots }

    fun enableSampling(): ClientCapabilitiesBuilder =
        apply { sampling = SamplingCapability() }

    fun enableSamplingWith(sampling: SamplingCapability): ClientCapabilitiesBuilder =
        apply { this.sampling = sampling }

    fun enableElicitation(): ClientCapabilitiesBuilder =
        apply { elicitation = ElicitationCapability() }

    fun enableElicitationWith(elicitation: ElicitationCapability): ClientCapabilitiesBuilder =
        apply { this.elicitation = elicitation }

    fun enableTasks(): ClientCapabilitiesBuilder =
        apply { tasks = TasksCapability() }

    fun enableTasksWith(tasks: TasksCapability): ClientCapabilitiesBuilder =
        apply { this.tasks = tasks }

    fun enableRootsListChanged(): ClientCapabilitiesBuilder =
        apply { roots?.listChanged = true }

    /**
     * Enable tool calling in sampling requests.
     */
    fun enableSamplingTools(): ClientCapabilitiesBuilder =
        apply { sampling = sampling?.copy(tools = emptyJsonObject()) }

    /**
     * Enable context inclusion in sampling, soft-deprecated.
     */
    fun enableSamplingContext(): ClientCapabilitiesBuilder =
        apply { sampling = sampling?.copy(context = emptyJsonObject()) }

    /**
     * Enable JSON Schema validation for elicitation responses in form mode.
     * When enabled, the client will validate user input against the requested
     * schema before sending responses back to the server.
     */
    fun enableElicitationSchemaValidation(): ClientCapabilitiesBuilder =
        apply {
            elicitation =
                elicitation?.copy(
                    form =
                        FormElicitationCapability(
                            schemaValidation = true,
                        ),
                )
        }
}

fun emptyJsonObject(): JsonObject =
    buildJsonObject {}
