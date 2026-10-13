// port-lint: source model/extension.rs
package io.github.kotlinmania.rmcp.model

import io.github.kotlinmania.http.Extensions as HttpExtensions

typealias Extensions = HttpExtensions

fun Extensions(): Extensions = HttpExtensions.new()

fun Extensions.copy(): Extensions = clone()
