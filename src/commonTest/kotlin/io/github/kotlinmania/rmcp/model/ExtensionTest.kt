// port-lint: tests model/extension.rs
package io.github.kotlinmania.rmcp.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtensionTest {
    @Test
    fun testExtensions() {
        data class MyType(
            val value: Int,
        )

        val extensions = Extensions.new()

        extensions.insert(5)
        extensions.insert(MyType(10))

        assertEquals(5, extensions.get<Int>())
        assertEquals(5, extensions.getMut<Int>())

        val ext2 = extensions.copy()

        assertEquals(5, extensions.remove<Int>())
        assertNull(extensions.get<Int>())

        // clone still has it
        assertEquals(5, ext2.get<Int>())
        assertEquals(MyType(10), ext2.get<MyType>())

        assertNull(extensions.get<Boolean>())
        assertEquals(MyType(10), extensions.get<MyType>())
    }
}
