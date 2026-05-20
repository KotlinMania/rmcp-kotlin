// port-lint: source transport/common/client_side_sse.rs
package io.github.kotlinmania.rmcp.transport.common

import io.github.kotlinmania.rmcp.model.ServerJsonRpcMessage
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

typealias BoxedSseResponse = Iterator<Result<Sse>>

data class Sse(
    val id: String? = null,
    val event: String? = null,
    val data: String? = null,
    val retry: Duration? = null,
)

interface SseRetryPolicy {
    fun retry(currentTimes: Int): Duration?
}

data class FixedInterval(
    val maxTimes: Int? = null,
    val duration: Duration = DEFAULT_MIN_DURATION,
) : SseRetryPolicy {
    override fun retry(currentTimes: Int): Duration? {
        val maxTimes = maxTimes
        if (maxTimes != null && currentTimes >= maxTimes) {
            return null
        }
        return duration
    }

    companion object {
        val DEFAULT_MIN_DURATION: Duration = 1.seconds
    }
}

data class ExponentialBackoff(
    val maxTimes: Int? = null,
    val baseDuration: Duration = DEFAULT_DURATION,
) : SseRetryPolicy {
    override fun retry(currentTimes: Int): Duration? {
        val maxTimes = maxTimes
        if (maxTimes != null && currentTimes >= maxTimes) {
            return null
        }
        return baseDuration * 2.0.pow(currentTimes).toInt()
    }

    companion object {
        val DEFAULT_DURATION: Duration = 1.seconds
    }
}

data object NeverRetry : SseRetryPolicy {
    override fun retry(currentTimes: Int): Duration? = null
}

class NeverReconnect<E : Throwable>(
    private var error: E?,
) : SseStreamReconnect<E> {
    override fun retryConnection(lastEventId: String?): Result<BoxedSseResponse> =
        Result.failure<BoxedSseResponse>(error ?: error("should not be called again"))
            .also { error = null }
}

/**
 * Abstraction for SSE reconnection logic. Implementors can hook into
 * [handleControlEvent] to consume control frames that arrive when a server
 * restarts an SSE stream. The default implementation is a no-op, keeping
 * existing behaviour intact.
 */
interface SseStreamReconnect<E : Throwable> {
    fun retryConnection(lastEventId: String?): Result<BoxedSseResponse>

    fun handleControlEvent(event: Sse): Result<Unit> =
        Result.success(Unit)

    fun handleStreamError(error: Throwable, lastEventId: String?) {
        if (lastEventId != null) {
            println("sse stream error for $lastEventId: ${error.message}")
        } else {
            println("sse stream error: ${error.message}")
        }
    }
}

class SseAutoReconnectStream<R, E>(
    private val retryPolicy: SseRetryPolicy,
    private val connector: R,
    stream: BoxedSseResponse,
) where R : SseStreamReconnect<E>, E : Throwable {
    private var lastEventId: String? = null
    private var serverRetryInterval: Duration? = null
    private var state: SseAutoReconnectStreamState = SseAutoReconnectStreamState.Connected(stream)

    fun next(): Result<ServerJsonRpcMessage>? {
        while (true) {
            when (val current = state) {
                is SseAutoReconnectStreamState.Connected -> {
                    if (!current.stream.hasNext()) {
                        return null
                    }

                    val next = current.stream.next()
                    val sse = next.getOrElse { error ->
                        connector.handleStreamError(error, lastEventId)
                        state = SseAutoReconnectStreamState.Retrying(
                            retryTimes = 0,
                            retrying = connector.retryConnection(lastEventId),
                        )
                        continue
                    }

                    sse.retry?.let { serverRetryInterval = it }
                    sse.id?.let { lastEventId = it }

                    val isMessageEvent = sse.event == null || sse.event == "" || sse.event == "message"
                    if (!isMessageEvent) {
                        val handled = connector.handleControlEvent(sse)
                        val error = handled.exceptionOrNull()
                        if (error != null) {
                            state = SseAutoReconnectStreamState.Terminated
                            return Result.failure(error)
                        }
                        continue
                    }

                    val data = sse.data ?: continue
                    val parsed = runCatching { Json.parseToJsonElement(data) }
                        .getOrElse { continue }
                    return runCatching { Json.decodeFromJsonElement<ServerJsonRpcMessage>(parsed) }
                }

                is SseAutoReconnectStreamState.Retrying -> {
                    val retryResult = current.retrying
                    retryResult.fold(
                        onSuccess = { newStream ->
                            state = SseAutoReconnectStreamState.Connected(newStream)
                        },
                        onFailure = { error ->
                            val nextRetryTimes = current.retryTimes + 1
                            val retryInterval = retryPolicy.retry(nextRetryTimes)
                            if (retryInterval != null) {
                                val interval = serverRetryInterval?.let { maxOf(it, retryInterval) } ?: retryInterval
                                state = SseAutoReconnectStreamState.WaitingNextRetry(
                                    sleep = interval,
                                    retryTimes = nextRetryTimes,
                                )
                            } else {
                                state = SseAutoReconnectStreamState.Terminated
                                return Result.failure(error)
                            }
                        },
                    )
                }

                is SseAutoReconnectStreamState.WaitingNextRetry -> {
                    state = SseAutoReconnectStreamState.Retrying(
                        retryTimes = current.retryTimes,
                        retrying = connector.retryConnection(lastEventId),
                    )
                }

                SseAutoReconnectStreamState.Terminated -> return null
            }
        }
    }

    companion object {
        fun <R, E> new(
            stream: BoxedSseResponse,
            connector: R,
            retryPolicy: SseRetryPolicy,
        ): SseAutoReconnectStream<R, E> where R : SseStreamReconnect<E>, E : Throwable =
            SseAutoReconnectStream(retryPolicy, connector, stream)

        fun <E : Throwable> neverReconnect(
            stream: BoxedSseResponse,
            errorWhenReconnect: E,
        ): SseAutoReconnectStream<NeverReconnect<E>, E> =
            SseAutoReconnectStream(
                retryPolicy = NeverRetry,
                connector = NeverReconnect(errorWhenReconnect),
                stream = stream,
            )
    }
}

sealed class SseAutoReconnectStreamState {
    data class Connected(
        val stream: BoxedSseResponse,
    ) : SseAutoReconnectStreamState()

    data class Retrying(
        val retryTimes: Int,
        val retrying: Result<BoxedSseResponse>,
    ) : SseAutoReconnectStreamState()

    data class WaitingNextRetry(
        val sleep: Duration,
        val retryTimes: Int,
    ) : SseAutoReconnectStreamState()

    data object Terminated : SseAutoReconnectStreamState()
}
