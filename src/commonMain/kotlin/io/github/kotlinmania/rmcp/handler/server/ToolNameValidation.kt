// port-lint: source rmcp/src/handler/server/tool_name_validation.rs
package io.github.kotlinmania.rmcp.handler.server

/**
 * Tool name validation utilities according to SEP: Specify Format for Tool Names.
 *
 * Tool names should be between 1 and 128 characters in length, inclusive.
 * Tool names are case-sensitive.
 * Allowed characters: uppercase and lowercase ASCII letters (A-Z, a-z), digits
 * (0-9), underscore (_), dash (-), and dot (.).
 * Tool names should not contain spaces, commas, or other special characters.
 */

/**
 * Result of tool name validation containing validation status and warnings.
 */
internal data class ToolNameValidationResult(
    /**
     * Whether the tool name is valid according to the specification.
     */
    val isValid: Boolean,
    /**
     * Array of warning messages about non-conforming aspects of the tool name.
     */
    val warnings: List<String>,
) {
    companion object {
        /**
         * Create a new validation result.
         */
        fun new(isValid: Boolean, warnings: List<String>): ToolNameValidationResult =
            ToolNameValidationResult(isValid, warnings)
    }
}

/**
 * Validates a tool name according to the SEP specification.
 */
internal fun validateToolName(name: String): ToolNameValidationResult {
    val warnings = mutableListOf<String>()

    if (name.isEmpty()) {
        return ToolNameValidationResult.new(false, listOf("Tool name cannot be empty"))
    }

    if (name.length > 128) {
        return ToolNameValidationResult.new(
            false,
            listOf("Tool name exceeds maximum length of 128 characters (current: ${name.length})"),
        )
    }

    if (' ' in name) {
        warnings += "Tool name contains spaces, which may cause parsing issues"
    }

    if (',' in name) {
        warnings += "Tool name contains commas, which may cause parsing issues"
    }

    if (name.startsWith('-') || name.endsWith('-')) {
        warnings += "Tool name starts or ends with a dash, which may cause parsing issues in some contexts"
    }

    if (name.startsWith('.') || name.endsWith('.')) {
        warnings += "Tool name starts or ends with a dot, which may cause parsing issues in some contexts"
    }

    val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-".toSet()
    val invalidChars =
        name
            .asSequence()
            .filter { it !in validChars }
            .distinct()
            .sorted()
            .toList()

    if (invalidChars.isNotEmpty()) {
        val invalidCharsList = invalidChars.joinToString(", ") { "\"$it\"" }
        warnings += "Tool name contains invalid characters: $invalidCharsList"
        warnings += "Allowed characters are: A-Z, a-z, 0-9, underscore (_), dash (-), and dot (.)"

        return ToolNameValidationResult.new(false, warnings)
    }

    if (name.isEmpty() || name.length > 128) {
        return ToolNameValidationResult.new(
            false,
            listOf("Tool name length must be between 1 and 128 characters"),
        )
    }

    return ToolNameValidationResult.new(true, warnings)
}

/**
 * Issues warnings for non-conforming tool names.
 */
internal fun issueToolNameWarning(name: String, warnings: List<String>) {
    println("Tool name validation warning for \"$name\":")
    for (warning in warnings) {
        println("  - $warning")
    }
    println("Tool registration will proceed, but this may cause compatibility issues.")
    println("Consider updating the tool name to conform to the MCP tool naming standard.")
    println(
        "See SEP: Specify Format for Tool Names (https://github.com/modelcontextprotocol/modelcontextprotocol/issues/986) for more details.",
    )
}

/**
 * Validates a tool name and issues warnings for non-conforming names.
 */
fun validateAndWarnToolName(name: String): Boolean {
    val result = validateToolName(name)

    if (result.warnings.isNotEmpty()) {
        issueToolNameWarning(name, result.warnings)
    }

    return result.isValid
}
