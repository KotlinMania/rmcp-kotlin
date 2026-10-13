// port-lint: tests transport/common/client_side_sse.rs
package io.github.kotlinmania.rmcp.transport.common

import io.github.kotlinmania.rmcp.model.ServerJsonRpcMessage
import io.github.kotlinmania.ssestream.Sse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ClientSideSseTest {
    @Test
    fun testFixedIntervalRetry() {
        val policy = FixedInterval(maxTimes = 3, duration = 2.seconds)
        assertEquals(2.seconds, policy.retry(0))
        assertEquals(2.seconds, policy.retry(1))
        assertEquals(2.seconds, policy.retry(2))
        assertNull(policy.retry(3))
    }

    @Test
    fun testExponentialBackoffRetry() {
        val policy = ExponentialBackoff(maxTimes = 4, baseDuration = 1.seconds)
        assertEquals(1.seconds, policy.retry(0))
        assertEquals(2.seconds, policy.retry(1))
        assertEquals(4.seconds, policy.retry(2))
        assertEquals(8.seconds, policy.retry(3))
        assertNull(policy.retry(4))
    }

    @Test
    fun testNeverRetry() {
        assertNull(NeverRetry.retry(0))
        assertNull(NeverRetry.retry(1))
    }

    @Test
    fun testSseAutoReconnectStreamNextMessage() {
        val sse = Sse().data("""{"jsonrpc":"2.0","method":"notifications/custom-server","params":{"hello":"world"}}""")
        val stream: BoxedSseResponse = listOf(Result.success(sse)).iterator()

        val autoStream =
            SseAutoReconnectStream.neverReconnect(
                stream = stream,
                errorWhenReconnect = IllegalStateException("disconnected"),
            )

        val next = autoStream.next()
        assertTrue(next != null && next.isSuccess)
        val msg = next.getOrNull()
        assertTrue(msg is ServerJsonRpcMessage)

        assertNull(autoStream.next())
    }

    @Test
    fun testSseAutoReconnectStreamIgnoresNonMessageEvents() {
        val pingSse = Sse().event("ping").data("ping-payload")
        val messageSse = Sse().data("""{"jsonrpc":"2.0","method":"notifications/custom-server","params":{"hello":"world"}}""")
        val stream: BoxedSseResponse = listOf(Result.success(pingSse), Result.success(messageSse)).iterator()

        val autoStream =
            SseAutoReconnectStream.neverReconnect(
                stream = stream,
                errorWhenReconnect = IllegalStateException("disconnected"),
            )

        val next = autoStream.next()
        assertTrue(next != null && next.isSuccess)

        assertNull(autoStream.next())
    }
}
