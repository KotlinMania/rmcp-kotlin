// port-lint: source model/elicitation_schema.rs
@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Type-safe schema definitions for MCP elicitation requests.
 *
 * This module provides strongly-typed schema definitions for elicitation
 * requests that comply with the MCP 2025-06-18 specification. Elicitation
 * schemas must be objects with primitive-typed properties.
 */

data object ObjectTypeConst : ConstString {
    override val value: String = "object"
}

data object StringTypeConst : ConstString {
    override val value: String = "string"
}

data object NumberTypeConst : ConstString {
    override val value: String = "number"
}

data object IntegerTypeConst : ConstString {
    override val value: String = "integer"
}

data object BooleanTypeConst : ConstString {
    override val value: String = "boolean"
}

data object EnumTypeConst : ConstString {
    override val value: String = "string"
}

data object ArrayTypeConst : ConstString {
    override val value: String = "array"
}

/**
 * Primitive schema definition for elicitation properties.
 *
 * According to the MCP 2025-06-18 specification, elicitation schemas must have
 * properties of primitive types only: string, number, integer, boolean, and enum.
 *
 * Put enum as the first variant to avoid ambiguity during deserialization.
 */
@Serializable
sealed class PrimitiveSchema {
    /**
     * Enum property, explicit enum schema.
     */
    @Serializable
    data class Enum(val value: EnumSchema) : PrimitiveSchema()

    /**
     * String property, with optional enum constraint.
     */
    @Serializable
    data class String(val value: StringSchema) : PrimitiveSchema()

    /**
     * Number property, with optional enum constraint.
     */
    @Serializable
    data class Number(val value: NumberSchema) : PrimitiveSchema()

    /**
     * Integer property, with optional enum constraint.
     */
    @Serializable
    data class Integer(val value: IntegerSchema) : PrimitiveSchema()

    /**
     * Boolean property.
     */
    @Serializable
    data class Boolean(val value: BooleanSchema) : PrimitiveSchema()
}

/**
 * String format types allowed by the MCP specification.
 */
@Serializable
enum class StringFormat {
    /**
     * Email address format.
     */
    @SerialName("email")
    Email,

    /**
     * URI format.
     */
    @SerialName("uri")
    Uri,

    /**
     * Date format, with year, month, and day.
     */
    @SerialName("date")
    Date,

    /**
     * Date-time format.
     */
    @SerialName("date-time")
    DateTime,
}

/**
 * Schema definition for string properties.
 *
 * Compliant with the MCP 2025-06-18 specification for elicitation schemas.
 */
@Serializable
data class StringSchema(
    /**
     * Type discriminator.
     */
    @SerialName("type")
    val type: kotlin.String = StringTypeConst.value,

    /**
     * Optional title for the schema.
     */
    val title: kotlin.String? = null,

    /**
     * Human-readable description.
     */
    val description: kotlin.String? = null,

    /**
     * Minimum string length.
     */
    @SerialName("minLength")
    val minLength: UInt? = null,

    /**
     * Maximum string length.
     */
    @SerialName("maxLength")
    val maxLength: UInt? = null,

    /**
     * String format, limited to email, URI, date, and date-time.
     */
    val format: StringFormat? = null,
) {
    /**
     * Create a new string schema.
     */
    fun new(): StringSchema =
        StringSchema()

    /**
     * Set title.
     */
    fun title(title: kotlin.String): StringSchema =
        copy(title = title)

    /**
     * Set description.
     */
    fun description(description: kotlin.String): StringSchema =
        copy(description = description)

    /**
     * Set minimum and maximum length.
     */
    fun withLength(min: UInt, max: UInt): Result<StringSchema> =
        if (min > max) {
            Result.failure(IllegalArgumentException("minLength must be <= maxLength"))
        } else {
            Result.success(copy(minLength = min, maxLength = max))
        }

    /**
     * Set minimum and maximum length. Throws on invalid input.
     */
    fun length(min: UInt, max: UInt): StringSchema {
        require(min <= max) { "minLength must be <= maxLength" }
        return copy(minLength = min, maxLength = max)
    }

    /**
     * Set minimum length.
     */
    fun minLength(min: UInt): StringSchema =
        copy(minLength = min)

    /**
     * Set maximum length.
     */
    fun maxLength(max: UInt): StringSchema =
        copy(maxLength = max)

    /**
     * Set format.
     */
    fun format(format: StringFormat): StringSchema =
        copy(format = format)

    companion object {
        /**
         * Create a new string schema.
         */
        fun new(): StringSchema =
            StringSchema()

        /**
         * Create an email string schema.
         */
        fun email(): StringSchema =
            StringSchema(format = StringFormat.Email)

        /**
         * Create a URI string schema.
         */
        fun uri(): StringSchema =
            StringSchema(format = StringFormat.Uri)

        /**
         * Create a date string schema.
         */
        fun date(): StringSchema =
            StringSchema(format = StringFormat.Date)

        /**
         * Create a date-time string schema.
         */
        fun dateTime(): StringSchema =
            StringSchema(format = StringFormat.DateTime)
    }
}

/**
 * Schema definition for number properties, floating-point.
 *
 * Compliant with the MCP 2025-06-18 specification for elicitation schemas.
 */
@Serializable
data class NumberSchema(
    /**
     * Type discriminator.
     */
    @SerialName("type")
    val type: kotlin.String = NumberTypeConst.value,

    /**
     * Optional title for the schema.
     */
    val title: kotlin.String? = null,

    /**
     * Human-readable description.
     */
    val description: kotlin.String? = null,

    /**
     * Minimum value, inclusive.
     */
    val minimum: Double? = null,

    /**
     * Maximum value, inclusive.
     */
    val maximum: Double? = null,
) {
    /**
     * Set minimum and maximum, inclusive.
     */
    fun withRange(min: Double, max: Double): Result<NumberSchema> =
        if (min > max) {
            Result.failure(IllegalArgumentException("minimum must be <= maximum"))
        } else {
            Result.success(copy(minimum = min, maximum = max))
        }

    /**
     * Set minimum and maximum. Throws on invalid input.
     */
    fun range(min: Double, max: Double): NumberSchema {
        require(min <= max) { "minimum must be <= maximum" }
        return copy(minimum = min, maximum = max)
    }

    /**
     * Set minimum, inclusive.
     */
    fun minimum(min: Double): NumberSchema =
        copy(minimum = min)

    /**
     * Set maximum, inclusive.
     */
    fun maximum(max: Double): NumberSchema =
        copy(maximum = max)

    /**
     * Set title.
     */
    fun title(title: kotlin.String): NumberSchema =
        copy(title = title)

    /**
     * Set description.
     */
    fun description(description: kotlin.String): NumberSchema =
        copy(description = description)

    companion object {
        /**
         * Create a new number schema.
         */
        fun new(): NumberSchema =
            NumberSchema()
    }
}

/**
 * Schema definition for integer properties.
 *
 * Compliant with the MCP 2025-06-18 specification for elicitation schemas.
 */
@Serializable
data class IntegerSchema(
    /**
     * Type discriminator.
     */
    @SerialName("type")
    val type: kotlin.String = IntegerTypeConst.value,

    /**
     * Optional title for the schema.
     */
    val title: kotlin.String? = null,

    /**
     * Human-readable description.
     */
    val description: kotlin.String? = null,

    /**
     * Minimum value, inclusive.
     */
    val minimum: Long? = null,

    /**
     * Maximum value, inclusive.
     */
    val maximum: Long? = null,
) {
    /**
     * Set minimum and maximum, inclusive.
     */
    fun withRange(min: Long, max: Long): Result<IntegerSchema> =
        if (min > max) {
            Result.failure(IllegalArgumentException("minimum must be <= maximum"))
        } else {
            Result.success(copy(minimum = min, maximum = max))
        }

    /**
     * Set minimum and maximum. Throws on invalid input.
     */
    fun range(min: Long, max: Long): IntegerSchema {
        require(min <= max) { "minimum must be <= maximum" }
        return copy(minimum = min, maximum = max)
    }

    /**
     * Set minimum, inclusive.
     */
    fun minimum(min: Long): IntegerSchema =
        copy(minimum = min)

    /**
     * Set maximum, inclusive.
     */
    fun maximum(max: Long): IntegerSchema =
        copy(maximum = max)

    /**
     * Set title.
     */
    fun title(title: kotlin.String): IntegerSchema =
        copy(title = title)

    /**
     * Set description.
     */
    fun description(description: kotlin.String): IntegerSchema =
        copy(description = description)

    companion object {
        /**
         * Create a new integer schema.
         */
        fun new(): IntegerSchema =
            IntegerSchema()
    }
}

/**
 * Schema definition for boolean properties.
 */
@Serializable
data class BooleanSchema(
    /**
     * Type discriminator.
     */
    @SerialName("type")
    val type: kotlin.String = BooleanTypeConst.value,

    /**
     * Optional title for the schema.
     */
    val title: kotlin.String? = null,

    /**
     * Human-readable description.
     */
    val description: kotlin.String? = null,

    /**
     * Default value.
     */
    val default: Boolean? = null,
) {
    /**
     * Set title.
     */
    fun title(title: kotlin.String): BooleanSchema =
        copy(title = title)

    /**
     * Set description.
     */
    fun description(description: kotlin.String): BooleanSchema =
        copy(description = description)

    /**
     * Set default value.
     */
    fun withDefault(default: Boolean): BooleanSchema =
        copy(default = default)

    companion object {
        /**
         * Create a new boolean schema.
         */
        fun new(): BooleanSchema =
            BooleanSchema()
    }
}

/**
 * Represents a single titled enum item.
 */
@Serializable
data class ConstTitle(
    @SerialName("const")
    val constValue: kotlin.String,
    val title: kotlin.String,
)

/**
 * Legacy enum schema, kept for backward compatibility.
 */
@Serializable
data class LegacyEnumSchema(
    @SerialName("type")
    val type: kotlin.String = StringTypeConst.value,
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
    @SerialName("enum")
    val values: List<kotlin.String>,
    @SerialName("enumNames")
    val enumNames: List<kotlin.String>? = null,
)

/**
 * Untitled single-select enum schema.
 */
@Serializable
data class UntitledSingleSelectEnumSchema(
    @SerialName("type")
    val type: kotlin.String = StringTypeConst.value,
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
    @SerialName("enum")
    val values: List<kotlin.String>,
    val default: kotlin.String? = null,
)

/**
 * Titled single-select enum schema.
 */
@Serializable
data class TitledSingleSelectEnumSchema(
    @SerialName("type")
    val type: kotlin.String = StringTypeConst.value,
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
    @SerialName("oneOf")
    val oneOf: List<ConstTitle>,
    val default: kotlin.String? = null,
)

/**
 * Combined single-select enum schema.
 */
@Serializable
sealed class SingleSelectEnumSchema {
    @Serializable
    data class Untitled(val value: UntitledSingleSelectEnumSchema) : SingleSelectEnumSchema()

    @Serializable
    data class Titled(val value: TitledSingleSelectEnumSchema) : SingleSelectEnumSchema()
}

/**
 * Items for untitled multi-select options.
 */
@Serializable
data class UntitledItems(
    @SerialName("type")
    val type: kotlin.String = StringTypeConst.value,
    @SerialName("enum")
    val values: List<kotlin.String>,
)

/**
 * Items for titled multi-select options.
 */
@Serializable
data class TitledItems(
    @SerialName("anyOf")
    val anyOf: List<ConstTitle>,
)

/**
 * Multi-select untitled options.
 */
@Serializable
data class UntitledMultiSelectEnumSchema(
    @SerialName("type")
    val type: kotlin.String = ArrayTypeConst.value,
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
    @SerialName("minItems")
    val minItems: ULong? = null,
    @SerialName("maxItems")
    val maxItems: ULong? = null,
    val items: UntitledItems,
    val default: List<kotlin.String>? = null,
)

/**
 * Multi-select titled options.
 */
@Serializable
data class TitledMultiSelectEnumSchema(
    @SerialName("type")
    val type: kotlin.String = ArrayTypeConst.value,
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
    @SerialName("minItems")
    val minItems: ULong? = null,
    @SerialName("maxItems")
    val maxItems: ULong? = null,
    val items: TitledItems,
    val default: List<kotlin.String>? = null,
)

/**
 * Multi-select enum options.
 */
@Serializable
sealed class MultiSelectEnumSchema {
    @Serializable
    data class Untitled(val value: UntitledMultiSelectEnumSchema) : MultiSelectEnumSchema()

    @Serializable
    data class Titled(val value: TitledMultiSelectEnumSchema) : MultiSelectEnumSchema()
}

/**
 * Compliant with the MCP 2025-06-18 specification for elicitation schemas.
 * Enums must have string type for values and can optionally include
 * human-readable names.
 */
@Serializable
sealed class EnumSchema {
    @Serializable
    data class Single(val value: SingleSelectEnumSchema) : EnumSchema()

    @Serializable
    data class Multi(val value: MultiSelectEnumSchema) : EnumSchema()

    @Serializable
    data class Legacy(val value: LegacyEnumSchema) : EnumSchema()

    companion object {
        /**
         * Creates a new enum schema builder with the given enum values.
         */
        fun builder(values: List<kotlin.String>): EnumSchemaBuilder =
            EnumSchemaBuilder.new(values)
    }
}

/**
 * Builder for enum schema values.
 *
 * Allows creating various enum schema types, single or multi select and titled
 * or untitled, with validation of provided parameters.
 */
data class EnumSchemaBuilder(
    /**
     * Enum values.
     */
    val enumValues: List<kotlin.String> = emptyList(),
    /**
     * Whether to generate titled enum schema.
     */
    val titled: Boolean = false,
    /**
     * Title of the enum schema.
     */
    val title: kotlin.String? = null,
    /**
     * Description of the enum schema.
     */
    val description: kotlin.String? = null,
    /**
     * Titles of given enum values.
     */
    val enumTitles: List<kotlin.String> = emptyList(),
    /**
     * Minimum number of items to choose for multi-select.
     */
    val minItems: ULong? = null,
    /**
     * Maximum number of items to choose for multi-select.
     */
    val maxItems: ULong? = null,
    /**
     * Default values for enum.
     */
    val default: List<kotlin.String> = emptyList(),
    val isMultiSelect: Boolean = false,
) {
    /**
     * Set title of enum schema.
     */
    fun title(value: kotlin.String): EnumSchemaBuilder =
        copy(title = value)

    /**
     * Set description of enum schema.
     */
    fun description(value: kotlin.String): EnumSchemaBuilder =
        copy(description = value)

    /**
     * Set enum as untitled and clear any previously set titles.
     */
    fun untitled(): EnumSchemaBuilder =
        copy(enumTitles = emptyList(), titled = false)

    /**
     * Set titles to enum values, and implicitly set this enum schema as titled.
     */
    fun enumTitles(titles: List<kotlin.String>): Result<EnumSchemaBuilder> =
        if (titles.size != enumValues.size) {
            Result.failure(
                IllegalArgumentException(
                    "Provided number of titles do not match number of values: expected ${enumValues.size}, but got ${titles.size}",
                ),
            )
        } else {
            Result.success(copy(titled = true, enumTitles = titles))
        }

    /**
     * Transition to multi-select enum builder.
     *
     * Clears any previously set default values and resets item bounds.
     */
    fun multiselect(): EnumSchemaBuilder =
        copy(isMultiSelect = true, minItems = null, maxItems = null, default = emptyList())

    /**
     * Set enum as single-select. If it was multi-select, clear default values.
     */
    fun singleSelect(): EnumSchemaBuilder =
        copy(isMultiSelect = false, minItems = null, maxItems = null, default = emptyList())

    /**
     * Set default value for single-select enum.
     */
    fun withDefault(defaultValue: kotlin.String): Result<EnumSchemaBuilder> =
        if (defaultValue !in enumValues) {
            Result.failure(IllegalArgumentException("Provided default value is not in enum values"))
        } else {
            Result.success(copy(default = listOf(defaultValue)))
        }

    /**
     * Set default values for multi-select enum.
     */
    fun withDefault(defaultValues: List<kotlin.String>): Result<EnumSchemaBuilder> {
        for (value in defaultValues) {
            if (value !in enumValues) {
                return Result.failure(
                    IllegalArgumentException("One of the provided default values is not in enum values"),
                )
            }
        }
        val min = minItems
        if (min != null && defaultValues.size.toULong() < min) {
            return Result.failure(
                IllegalArgumentException("Number of provided default values is less than minItems"),
            )
        }
        val max = maxItems
        if (max != null && defaultValues.size.toULong() > max) {
            return Result.failure(
                IllegalArgumentException("Number of provided default values is greater than maxItems"),
            )
        }
        return Result.success(copy(default = defaultValues))
    }

    /**
     * Set minimal number of items for multi-select enum options.
     */
    fun minItems(value: ULong): Result<EnumSchemaBuilder> {
        val max = maxItems
        return if (max != null && value > max) {
            Result.failure(IllegalArgumentException("Provided value is greater than maxItems"))
        } else {
            Result.success(copy(minItems = value))
        }
    }

    /**
     * Set maximal number of items for multi-select enum options.
     */
    fun maxItems(value: ULong): Result<EnumSchemaBuilder> {
        val min = minItems
        return if (min != null && value < min) {
            Result.failure(IllegalArgumentException("Provided value is less than minItems"))
        } else {
            Result.success(copy(maxItems = value))
        }
    }

    /**
     * Build enum schema.
     */
    fun build(): EnumSchema =
        if (isMultiSelect) {
            buildMultiSelect()
        } else {
            buildSingleSelect()
        }

    private fun buildSingleSelect(): EnumSchema =
        if (titled) {
            EnumSchema.Single(
                SingleSelectEnumSchema.Titled(
                    TitledSingleSelectEnumSchema(
                        title = title,
                        description = description,
                        oneOf = enumTitles.zip(enumValues).map { (title, constValue) ->
                            ConstTitle(constValue = constValue, title = title)
                        },
                        default = default.firstOrNull(),
                    ),
                ),
            )
        } else {
            EnumSchema.Single(
                SingleSelectEnumSchema.Untitled(
                    UntitledSingleSelectEnumSchema(
                        title = title,
                        description = description,
                        values = enumValues,
                        default = default.firstOrNull(),
                    ),
                ),
            )
        }

    private fun buildMultiSelect(): EnumSchema =
        if (titled) {
            EnumSchema.Multi(
                MultiSelectEnumSchema.Titled(
                    TitledMultiSelectEnumSchema(
                        title = title,
                        description = description,
                        minItems = minItems,
                        maxItems = maxItems,
                        items = TitledItems(
                            anyOf = enumTitles.zip(enumValues).map { (title, constValue) ->
                                ConstTitle(constValue = constValue, title = title)
                            },
                        ),
                        default = default.ifEmpty { null },
                    ),
                ),
            )
        } else {
            EnumSchema.Multi(
                MultiSelectEnumSchema.Untitled(
                    UntitledMultiSelectEnumSchema(
                        title = title,
                        description = description,
                        minItems = minItems,
                        maxItems = maxItems,
                        items = UntitledItems(values = enumValues),
                        default = default.ifEmpty { null },
                    ),
                ),
            )
        }

    companion object {
        /**
         * Default implementation for a single-select enum builder.
         */
        fun default(): EnumSchemaBuilder =
            EnumSchemaBuilder()

        /**
         * Create a new single-select enum builder.
         */
        fun new(values: List<kotlin.String>): EnumSchemaBuilder =
            EnumSchemaBuilder(enumValues = values)
    }
}

/**
 * Type-safe elicitation schema for requesting structured user input.
 *
 * This enforces the MCP 2025-06-18 specification that elicitation schemas must
 * be objects with primitive-typed properties.
 */
@Serializable
data class ElicitationSchema(
    /**
     * Always object for elicitation schemas.
     */
    @SerialName("type")
    val type: kotlin.String = ObjectTypeConst.value,

    /**
     * Optional title for the schema.
     */
    val title: kotlin.String? = null,

    /**
     * Property definitions, which must be primitive types.
     */
    val properties: Map<kotlin.String, PrimitiveSchema>,

    /**
     * List of required property names.
     */
    val required: List<kotlin.String>? = null,

    /**
     * Optional description of what this schema represents.
     */
    val description: kotlin.String? = null,
) {
    /**
     * Set the required fields.
     */
    fun withRequired(required: List<kotlin.String>): ElicitationSchema =
        copy(required = required)

    /**
     * Set the title.
     */
    fun withTitle(title: kotlin.String): ElicitationSchema =
        copy(title = title)

    /**
     * Set the description.
     */
    fun withDescription(description: kotlin.String): ElicitationSchema =
        copy(description = description)

    companion object {
        /**
         * Create a new elicitation schema with the given properties.
         */
        fun new(properties: Map<kotlin.String, PrimitiveSchema>): ElicitationSchema =
            ElicitationSchema(properties = properties)

        /**
         * Convert from a JSON Schema object.
         */
        fun fromJsonSchema(schema: JsonObject): Result<ElicitationSchema> =
            runCatching {
                schemaJson.decodeFromString<ElicitationSchema>(schemaJson.encodeToString(schema))
            }

        /**
         * Create a builder for constructing elicitation schemas fluently.
         */
        fun builder(): ElicitationSchemaBuilder =
            ElicitationSchemaBuilder.new()
    }
}

/**
 * Fluent builder for constructing elicitation schemas.
 */
data class ElicitationSchemaBuilder(
    val properties: Map<kotlin.String, PrimitiveSchema> = emptyMap(),
    val required: List<kotlin.String> = emptyList(),
    val title: kotlin.String? = null,
    val description: kotlin.String? = null,
) {
    /**
     * Add a property to the schema.
     */
    fun property(name: kotlin.String, schema: PrimitiveSchema): ElicitationSchemaBuilder =
        copy(properties = properties + (name to schema))

    /**
     * Add a required property to the schema.
     */
    fun requiredProperty(name: kotlin.String, schema: PrimitiveSchema): ElicitationSchemaBuilder =
        copy(required = required + name, properties = properties + (name to schema))

    /**
     * Add a string property with custom builder.
     */
    fun stringProperty(
        name: kotlin.String,
        f: (StringSchema) -> StringSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.String(f(StringSchema.new())))

    /**
     * Add a required string property with custom builder.
     */
    fun requiredStringProperty(
        name: kotlin.String,
        f: (StringSchema) -> StringSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.String(f(StringSchema.new())))

    /**
     * Add a number property with custom builder.
     */
    fun numberProperty(
        name: kotlin.String,
        f: (NumberSchema) -> NumberSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Number(f(NumberSchema.new())))

    /**
     * Add a required number property with custom builder.
     */
    fun requiredNumberProperty(
        name: kotlin.String,
        f: (NumberSchema) -> NumberSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Number(f(NumberSchema.new())))

    /**
     * Add an integer property with custom builder.
     */
    fun integerProperty(
        name: kotlin.String,
        f: (IntegerSchema) -> IntegerSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Integer(f(IntegerSchema.new())))

    /**
     * Add a required integer property with custom builder.
     */
    fun requiredIntegerProperty(
        name: kotlin.String,
        f: (IntegerSchema) -> IntegerSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Integer(f(IntegerSchema.new())))

    /**
     * Add a boolean property with custom builder.
     */
    fun boolProperty(
        name: kotlin.String,
        f: (BooleanSchema) -> BooleanSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Boolean(f(BooleanSchema.new())))

    /**
     * Add a required boolean property with custom builder.
     */
    fun requiredBoolProperty(
        name: kotlin.String,
        f: (BooleanSchema) -> BooleanSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Boolean(f(BooleanSchema.new())))

    /**
     * Add a required string property.
     */
    fun requiredString(name: kotlin.String): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.String(StringSchema.new()))

    /**
     * Add an optional string property.
     */
    fun optionalString(name: kotlin.String): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.String(StringSchema.new()))

    /**
     * Add a required email property.
     */
    fun requiredEmail(name: kotlin.String): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.String(StringSchema.email()))

    /**
     * Add an optional email property.
     */
    fun optionalEmail(name: kotlin.String): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.String(StringSchema.email()))

    /**
     * Add a required string property with custom builder.
     */
    fun requiredStringWith(
        name: kotlin.String,
        f: (StringSchema) -> StringSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.String(f(StringSchema.new())))

    /**
     * Add an optional string property with custom builder.
     */
    fun optionalStringWith(
        name: kotlin.String,
        f: (StringSchema) -> StringSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.String(f(StringSchema.new())))

    /**
     * Add a required number property with range.
     */
    fun requiredNumber(name: kotlin.String, min: Double, max: Double): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Number(NumberSchema.new().range(min, max)))

    /**
     * Add an optional number property with range.
     */
    fun optionalNumber(name: kotlin.String, min: Double, max: Double): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Number(NumberSchema.new().range(min, max)))

    /**
     * Add a required number property with custom builder.
     */
    fun requiredNumberWith(
        name: kotlin.String,
        f: (NumberSchema) -> NumberSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Number(f(NumberSchema.new())))

    /**
     * Add an optional number property with custom builder.
     */
    fun optionalNumberWith(
        name: kotlin.String,
        f: (NumberSchema) -> NumberSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Number(f(NumberSchema.new())))

    /**
     * Add a required integer property with range.
     */
    fun requiredInteger(name: kotlin.String, min: Long, max: Long): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Integer(IntegerSchema.new().range(min, max)))

    /**
     * Add an optional integer property with range.
     */
    fun optionalInteger(name: kotlin.String, min: Long, max: Long): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Integer(IntegerSchema.new().range(min, max)))

    /**
     * Add a required integer property with custom builder.
     */
    fun requiredIntegerWith(
        name: kotlin.String,
        f: (IntegerSchema) -> IntegerSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Integer(f(IntegerSchema.new())))

    /**
     * Add an optional integer property with custom builder.
     */
    fun optionalIntegerWith(
        name: kotlin.String,
        f: (IntegerSchema) -> IntegerSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Integer(f(IntegerSchema.new())))

    /**
     * Add a required boolean property.
     */
    fun requiredBool(name: kotlin.String): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Boolean(BooleanSchema.new()))

    /**
     * Add an optional boolean property with default value.
     */
    fun optionalBool(name: kotlin.String, default: Boolean): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Boolean(BooleanSchema.new().withDefault(default)))

    /**
     * Add a required boolean property with custom builder.
     */
    fun requiredBoolWith(
        name: kotlin.String,
        f: (BooleanSchema) -> BooleanSchema,
    ): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Boolean(f(BooleanSchema.new())))

    /**
     * Add an optional boolean property with custom builder.
     */
    fun optionalBoolWith(
        name: kotlin.String,
        f: (BooleanSchema) -> BooleanSchema,
    ): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Boolean(f(BooleanSchema.new())))

    /**
     * Add a required enum property using enum schema.
     */
    fun requiredEnumSchema(name: kotlin.String, enumSchema: EnumSchema): ElicitationSchemaBuilder =
        requiredProperty(name, PrimitiveSchema.Enum(enumSchema))

    /**
     * Add an optional enum property using enum schema.
     */
    fun optionalEnumSchema(name: kotlin.String, enumSchema: EnumSchema): ElicitationSchemaBuilder =
        property(name, PrimitiveSchema.Enum(enumSchema))

    /**
     * Add a required enum property using values.
     */
    @Deprecated("Use requiredEnumSchema with EnumSchema.builder instead")
    fun requiredEnum(name: kotlin.String, values: List<kotlin.String>): ElicitationSchemaBuilder =
        requiredProperty(
            name,
            PrimitiveSchema.Enum(
                EnumSchema.Legacy(
                    LegacyEnumSchema(
                        values = values,
                        enumNames = null,
                    ),
                ),
            ),
        )

    /**
     * Add an optional enum property using values.
     */
    @Deprecated("Use optionalEnumSchema with EnumSchema.builder instead")
    fun optionalEnum(name: kotlin.String, values: List<kotlin.String>): ElicitationSchemaBuilder =
        property(
            name,
            PrimitiveSchema.Enum(
                EnumSchema.Legacy(
                    LegacyEnumSchema(
                        values = values,
                        enumNames = null,
                    ),
                ),
            ),
        )

    /**
     * Mark an existing property as required.
     */
    fun markRequired(name: kotlin.String): ElicitationSchemaBuilder =
        copy(required = required + name)

    /**
     * Set the schema title.
     */
    fun title(title: kotlin.String): ElicitationSchemaBuilder =
        copy(title = title)

    /**
     * Set the schema description.
     */
    fun description(description: kotlin.String): ElicitationSchemaBuilder =
        copy(description = description)

    /**
     * Build the elicitation schema with validation.
     */
    fun build(): Result<ElicitationSchema> {
        for (fieldName in required) {
            if (fieldName !in properties) {
                return Result.failure(IllegalArgumentException("Required field does not exist in properties"))
            }
        }
        return Result.success(
            ElicitationSchema(
                title = title,
                properties = properties,
                required = required.ifEmpty { null },
                description = description,
            ),
        )
    }

    /**
     * Build the elicitation schema without validation. Throws on invalid schema.
     */
    fun buildUnchecked(): ElicitationSchema =
        build().getOrThrow()

    companion object {
        /**
         * Create a new builder.
         */
        fun new(): ElicitationSchemaBuilder =
            ElicitationSchemaBuilder()
    }
}

private val schemaJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
