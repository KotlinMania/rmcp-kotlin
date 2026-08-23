// port-lint: source transport/common/server_side_http.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.rmcp.transport.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

typealias SessionId = String

fun sessionId(): SessionId =
    "${Clock.System.now().toEpochMilliseconds()}-${Random.nextLong().toString(16)}"

val DEFAULT_AUTO_PING_INTERVAL: Duration = 15.seconds

data class SimpleHttpResponse(
    val status: Int,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
)

data class ServerSseMessage(
    /**
     * The event ID for this message. When set, clients can use this ID with
     * the Last-Event-ID header to resume the stream from this point.
     */
    val eventId: String? = null,
    /**
     * The JSON-RPC message content. Set to null for priming events.
     */
    val message: JsonElement? = null,
    /**
     * The retry interval hint for clients. Clients should wait this duration
     * before attempting to reconnect.
     */
    val retry: Duration? = null,
)

fun acceptedResponse(): SimpleHttpResponse =
    SimpleHttpResponse(status = 202)

fun sseStreamResponse(
    stream: Iterable<ServerSseMessage>,
    keepAlive: Duration?,
    cancelled: Boolean,
): SimpleHttpResponse {
    val body =
        if (cancelled) {
            ""
        } else {
            stream.joinToString(separator = "\n") { message ->
                buildString {
                    message.eventId?.let { append("id: ").append(it).append('\n') }
                    message.retry?.let { append("retry: ").append(it.inWholeMilliseconds).append('\n') }
                    append("data: ")
                    append(message.message?.toString().orEmpty())
                    append('\n')
                }
            }
        }
    val headers =
        buildMap {
            put("Content-Type", EVENT_STREAM_MIME_TYPE)
            put("Cache-Control", "no-cache")
            keepAlive?.let { put("X-Keep-Alive-Millis", it.inWholeMilliseconds.toString()) }
        }
    return SimpleHttpResponse(status = 200, headers = headers, body = body)
}

fun internalErrorResponse(context: String): (Throwable) -> SimpleHttpResponse =
    { error ->
        SimpleHttpResponse(
            status = 500,
            body = "Encounter an error when $context: ${error.message}",
        )
    }

fun unexpectedMessageResponse(expect: String): SimpleHttpResponse =
    SimpleHttpResponse(
        status = 422,
        body = "Unexpected message, expect $expect",
    )

fun expectJson(body: String): Result<JsonElement> =
    runCatching { Json.parseToJsonElement(body) }
