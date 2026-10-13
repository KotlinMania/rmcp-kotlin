// port-lint: source transport/common/server_side_http.rs
@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.kotlinmania.rmcp.transport.common

import io.github.kotlinmania.bytes.Bytes
import io.github.kotlinmania.http.StatusCode
import io.github.kotlinmania.ssestream.Sse
import io.github.kotlinmania.ssestream.toByteArray
import io.github.kotlinmania.tokioutil.sync.cancellationtoken.CancellationToken
import io.github.kotlinmania.uuid.Uuid
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

typealias SessionId = String

fun sessionId(): SessionId {
    val bytes = Random.nextBytes(16)
    bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
    return Uuid.fromBytes(bytes).toString()
}

val DEFAULT_AUTO_PING_INTERVAL: Duration = 15.seconds

data class SimpleHttpResponse(
    val status: StatusCode,
    val headers: Map<String, String> = emptyMap(),
    val body: Bytes = Bytes.new(),
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
    SimpleHttpResponse(status = StatusCode.ACCEPTED)

fun sseStreamResponse(
    stream: Iterable<ServerSseMessage>,
    keepAlive: Duration?,
    ct: CancellationToken = CancellationToken(),
): SimpleHttpResponse {
    val body =
        if (ct.isCancelled()) {
            Bytes.new()
        } else {
            val byteList = mutableListOf<ByteArray>()
            for (message in stream) {
                val sse = Sse()
                if (message.message != null) {
                    sse.data = message.message.toString()
                } else {
                    sse.data = ""
                }
                sse.id = message.eventId
                message.retry?.let { sse.retryDuration(it) }
                byteList.add(sse.toByteArray())
            }
            val totalSize = byteList.sumOf { it.size }
            val combined = ByteArray(totalSize)
            var offset = 0
            for (bytes in byteList) {
                bytes.copyInto(combined, offset)
                offset += bytes.size
            }
            Bytes.from(combined)
        }
    val headers =
        buildMap {
            put("Content-Type", EVENT_STREAM_MIME_TYPE)
            put("Cache-Control", "no-cache")
            keepAlive?.let { put("X-Keep-Alive-Millis", it.inWholeMilliseconds.toString()) }
        }
    return SimpleHttpResponse(status = StatusCode.OK, headers = headers, body = body)
}

fun sseStreamResponse(
    stream: Iterable<ServerSseMessage>,
    keepAlive: Duration?,
    cancelled: Boolean,
): SimpleHttpResponse {
    val ct = CancellationToken()
    if (cancelled) {
        ct.cancel()
    }
    return sseStreamResponse(stream, keepAlive, ct)
}

fun internalErrorResponse(context: String): (Throwable) -> SimpleHttpResponse =
    { error ->
        SimpleHttpResponse(
            status = StatusCode.INTERNAL_SERVER_ERROR,
            body = Bytes.from("Encounter an error when $context: ${error.message}"),
        )
    }

fun unexpectedMessageResponse(expect: String): SimpleHttpResponse =
    SimpleHttpResponse(
        status = StatusCode.UNPROCESSABLE_ENTITY,
        body = Bytes.from("Unexpected message, expect $expect"),
    )

fun expectJson(body: String): Result<JsonElement> =
    runCatching { Json.parseToJsonElement(body) }

fun expectJson(body: Bytes): Result<JsonElement> =
    runCatching { Json.parseToJsonElement(body.asSlice().decodeToString()) }
