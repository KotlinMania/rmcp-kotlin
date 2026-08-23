// port-lint: source model/annotated.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.rmcp.model

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Annotations(
    val audience: List<Role>? = null,
    val priority: Float? = null,
    val lastModified: Instant? = null,
) {
    companion object {
        /**
         * Creates a new Annotations instance specifically for resources,
         * optional priority, and a timestamp.
         */
        fun forResource(priority: Float, timestamp: Instant): Annotations {
            require(priority in 0.0f..1.0f) {
                "Priority $priority must be between 0.0 and 1.0"
            }
            return Annotations(
                priority = priority,
                lastModified = timestamp,
                audience = null,
            )
        }
    }
}

@Serializable
data class Annotated<T : AnnotateAble>(
    val raw: T,
    val annotations: Annotations?,
) {
    companion object

    fun new(raw: T, annotations: Annotations?): Annotated<T> =
        Annotated(raw, annotations)

    fun removeAnnotation(): Annotations? = annotations

    fun audience(): List<Role>? = annotations?.audience

    fun priority(): Float? = annotations?.priority

    fun timestamp(): Instant? = annotations?.lastModified

    fun withAudience(audience: List<Role>): Annotated<T> =
        copy(
            annotations =
                annotations?.copy(audience = audience)
                    ?: Annotations(audience = audience),
        )

    fun withPriority(priority: Float): Annotated<T> =
        copy(
            annotations =
                annotations?.copy(priority = priority)
                    ?: Annotations(priority = priority),
        )

    fun withTimestamp(timestamp: Instant): Annotated<T> =
        copy(
            annotations =
                annotations?.copy(lastModified = timestamp)
                    ?: Annotations(lastModified = timestamp),
        )

    fun withTimestampNow(): Annotated<T> =
        withTimestamp(Clock.System.now())
}

interface AnnotateAble

fun <T : AnnotateAble> T.optionalAnnotate(annotations: Annotations?): Annotated<T> =
    Annotated(this, annotations)

fun <T : AnnotateAble> T.annotate(annotations: Annotations): Annotated<T> =
    Annotated(this, annotations)

fun <T : AnnotateAble> T.noAnnotation(): Annotated<T> =
    Annotated(this, null)

fun <T : AnnotateAble> T.withAudience(audience: List<Role>): Annotated<T> =
    annotate(Annotations(audience = audience))

fun <T : AnnotateAble> T.withPriority(priority: Float): Annotated<T> =
    annotate(Annotations(priority = priority))

fun <T : AnnotateAble> T.withTimestamp(timestamp: Instant): Annotated<T> =
    annotate(Annotations(lastModified = timestamp))

fun <T : AnnotateAble> T.withTimestampNow(): Annotated<T> =
    withTimestamp(Clock.System.now())
