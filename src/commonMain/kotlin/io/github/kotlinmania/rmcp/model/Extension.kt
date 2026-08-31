// port-lint: source rmcp/src/model/extension.rs
package io.github.kotlinmania.rmcp.model

import kotlin.reflect.KClass

/**
 * A container for extra data carried on requests or notifications.
 *
 * This file is copied and modified from crate http.
 *
 * - Original code license: https://github.com/hyperium/http/blob/master/LICENSE-MIT
 * - Original code: https://github.com/hyperium/http/blob/master/src/extensions.rs
 */

/**
 * A type map of protocol extensions.
 *
 * [Extensions] can be used by request, notification, and response values to
 * store extra data derived from the underlying protocol.
 */
class Extensions private constructor(
    @PublishedApi
    internal var map: MutableMap<KClass<*>, Any>?,
) {
    constructor() : this(null)

    /**
     * Insert a type into this [Extensions].
     *
     * If an extension of this type already existed, it will be returned and
     * replaced with the new one.
     */
    inline fun <reified T : Any> insert(value: T): T? {
        val entries = entries()
        val previous = entries.put(T::class, value)
        return previous as? T
    }

    /**
     * Get a reference to a type previously inserted on this [Extensions].
     */
    inline fun <reified T : Any> get(): T? =
        map?.get(T::class) as? T

    /**
     * Get a mutable reference to a type previously inserted on this
     * [Extensions].
     */
    inline fun <reified T : Any> getMut(): T? =
        get<T>()

    /**
     * Get a mutable reference to a type, inserting [value] if not already
     * present on this [Extensions].
     */
    inline fun <reified T : Any> getOrInsert(value: T): T =
        getOrInsertWith { value }

    /**
     * Get a mutable reference to a type, inserting the value created by
     * [create] if not already present on this [Extensions].
     */
    inline fun <reified T : Any> getOrInsertWith(create: () -> T): T {
        val entries = entries()
        val existing = entries[T::class] as? T
        if (existing != null) {
            return existing
        }
        val value = create()
        entries[T::class] = value
        return value
    }

    /**
     * Get a mutable reference to a type, inserting the type's default value if
     * not already present on this [Extensions].
     */
    inline fun <reified T : Any> getOrInsertDefault(createDefault: () -> T): T =
        getOrInsertWith(createDefault)

    /**
     * Remove a type from this [Extensions].
     *
     * If an extension of this type existed, it will be returned.
     */
    inline fun <reified T : Any> remove(): T? =
        map?.remove(T::class) as? T

    /**
     * Clear the [Extensions] of all inserted extensions.
     */
    fun clear() {
        map?.clear()
    }

    /**
     * Check whether the extension set is empty.
     */
    fun isEmpty(): Boolean =
        map?.isEmpty() ?: true

    /**
     * Get the number of extensions available.
     */
    fun len(): Int =
        map?.size ?: 0

    /**
     * Extend this extension set with another [Extensions].
     *
     * If an instance of a specific type exists in both, the one in this
     * [Extensions] is overwritten with the one from [other].
     */
    fun extend(other: Extensions) {
        val otherEntries = other.map ?: return
        entries().putAll(otherEntries)
    }

    @PublishedApi
    internal fun entries(): MutableMap<KClass<*>, Any> =
        map ?: mutableMapOf<KClass<*>, Any>().also { replacement ->
            map = replacement
        }

    fun copy(): Extensions =
        Extensions(map?.toMutableMap())

    override fun toString(): String =
        "Extensions"

    companion object {
        /**
         * Create an empty [Extensions].
         */
        fun new(): Extensions = Extensions()
    }
}
