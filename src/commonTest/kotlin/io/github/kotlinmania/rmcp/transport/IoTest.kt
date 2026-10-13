// port-lint: tests transport/io.rs
package io.github.kotlinmania.rmcp.transport

import io.github.kotlinmania.tokio.io.AsyncRead
import io.github.kotlinmania.tokio.io.AsyncWrite
import io.github.kotlinmania.tokio.io.Context
import io.github.kotlinmania.tokio.io.Poll
import io.github.kotlinmania.tokio.io.ReadBuf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IoTest {
    @Test
    fun testStdio() {
        val (stdin, stdout) = stdio()
        assertNotNull(stdin)
        assertNotNull(stdout)

        val reader: AsyncRead = stdin
        val writer: AsyncWrite = stdout

        val buf = ReadBuf(UByteArray(16))
        val readPoll = reader.pollRead(Context(), buf)
        assertTrue(readPoll is Poll.Ready)

        val payload = ubyteArrayOf(1u, 2u, 3u)
        val writePoll = writer.pollWrite(Context(), payload)
        assertTrue(writePoll is Poll.Ready)
        val writtenBytes = writePoll.value.getOrThrow()
        assertEquals(3, writtenBytes)

        val flushPoll = writer.pollFlush(Context())
        assertTrue(flushPoll is Poll.Ready)

        val shutdownPoll = writer.pollShutdown(Context())
        assertTrue(shutdownPoll is Poll.Ready)
    }
}
