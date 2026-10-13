// port-lint: tests transport/common/server_side_http.rs
package io.github.kotlinmania.rmcp.transport.common

import io.github.kotlinmania.bytes.Bytes
import io.github.kotlinmania.http.StatusCode
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ServerSideHttpTest {
    @Test
    fun testSessionIdGeneratesValidUuid() {
        val id1 = sessionId()
        val id2 = sessionId()
        assertFalse(id1 == id2)
        assertEquals(36, id1.length)
        assertEquals('-', id1[8])
        assertEquals('-', id1[13])
        assertEquals('-', id1[18])
        assertEquals('-', id1[23])
        assertEquals('4', id1[14]) // UUID v4
    }

    @Test
    fun testAcceptedResponse() {
        val response = acceptedResponse()
        assertEquals(StatusCode.ACCEPTED, response.status)
        assertEquals(0, response.body.len())
    }

    @Test
    fun testInternalErrorResponse() {
        val handler = internalErrorResponse("reading request")
        val response = handler(IllegalStateException("connection reset"))
        assertEquals(StatusCode.INTERNAL_SERVER_ERROR, response.status)
        val text = response.body.asSlice().decodeToString()
        assertTrue(text.contains("Encounter an error when reading request: connection reset"))
    }

    @Test
    fun testUnexpectedMessageResponse() {
        val response = unexpectedMessageResponse("initialize")
        assertEquals(StatusCode.UNPROCESSABLE_ENTITY, response.status)
        val text = response.body.asSlice().decodeToString()
        assertEquals("Unexpected message, expect initialize", text)
    }

    @Test
    fun testSseStreamResponseCancelled() {
        val response =
            sseStreamResponse(
                stream = listOf(ServerSseMessage(eventId = "1", message = JsonPrimitive("hello"))),
                keepAlive = null,
                cancelled = true,
            )
        assertEquals(StatusCode.OK, response.status)
        assertEquals(0, response.body.len())
    }

    @Test
    fun testSseStreamResponseActive() {
        val messages =
            listOf(
                ServerSseMessage(eventId = "e1", message = JsonPrimitive("test message"), retry = 3.seconds),
                ServerSseMessage(eventId = null, message = null), // priming event
            )
        val response =
            sseStreamResponse(
                stream = messages,
                keepAlive = 15.seconds,
                cancelled = false,
            )
        assertEquals(StatusCode.OK, response.status)
        assertEquals(EVENT_STREAM_MIME_TYPE, response.headers["Content-Type"])
        assertEquals("no-cache", response.headers["Cache-Control"])
        assertEquals("15000", response.headers["X-Keep-Alive-Millis"])

        val text = response.body.asSlice().decodeToString()
        assertTrue(text.contains("id: e1"))
        assertTrue(text.contains("retry: 3000"))
        assertTrue(text.contains("data: \"test message\""))
    }

    @Test
    fun testExpectJson() {
        val validBytes = Bytes.from("{\"key\":\"value\"}")
        val res = expectJson(validBytes)
        assertTrue(res.isSuccess)
        assertNotNull(res.getOrNull())

        val invalidBytes = Bytes.from("not json")
        val failRes = expectJson(invalidBytes)
        assertTrue(failRes.isFailure)
    }
}
