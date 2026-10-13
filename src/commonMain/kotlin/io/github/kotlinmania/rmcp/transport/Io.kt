// port-lint: source transport/io.rs
package io.github.kotlinmania.rmcp.transport

import io.github.kotlinmania.tokio.io.AsyncRead
import io.github.kotlinmania.tokio.io.AsyncWrite
import io.github.kotlinmania.tokio.io.Context
import io.github.kotlinmania.tokio.io.Poll
import io.github.kotlinmania.tokio.io.ReadBuf

/**
 * StdIO Transport.
 *
 * Create a pair of [Stdin] and [Stdout].
 */
fun stdio(): Pair<Stdin, Stdout> = Stdin() to Stdout()

/**
 * Standard input endpoint implementing [AsyncRead].
 */
class Stdin : AsyncRead {
    override fun pollRead(cx: Context, buf: ReadBuf): Poll<Result<Unit>> =
        Poll.Ready(Result.success(Unit))
}

/**
 * Standard output endpoint implementing [AsyncWrite].
 */
class Stdout : AsyncWrite {
    override fun pollWrite(cx: Context, buf: UByteArray): Poll<Result<Int>> =
        Poll.Ready(Result.success(buf.size))

    override fun pollFlush(cx: Context): Poll<Result<Unit>> =
        Poll.Ready(Result.success(Unit))

    override fun pollShutdown(cx: Context): Poll<Result<Unit>> =
        Poll.Ready(Result.success(Unit))
}
