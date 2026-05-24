// port-lint: source transport/io.rs
package io.github.kotlinmania.rmcp.transport

/**
 * StdIO Transport.
 *
 * Create a pair of [Stdin] and [Stdout].
 */
fun stdio(): Pair<Stdin, Stdout> = Stdin to Stdout

/**
 * Multiplatform representation of the process standard input endpoint.
 */
data object Stdin

/**
 * Multiplatform representation of the process standard output endpoint.
 */
data object Stdout
