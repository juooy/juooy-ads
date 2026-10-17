package kr.co.juooy.ads

import kotlin.test.Test
import kotlin.test.assertEquals

class ReadyGateTest {

    @Test
    fun `callbacks registered before open fire on open`() {
        val gate = ReadyGate()
        val calls = mutableListOf<String>()
        gate.await { calls += "base" }
        gate.await { calls += "subclass" }
        assertEquals(emptyList(), calls)

        gate.open()

        assertEquals(listOf("base", "subclass"), calls)
    }

    @Test
    fun `callback registered after open fires immediately`() {
        val gate = ReadyGate()
        gate.open()
        var count = 0

        gate.await { count++ }

        assertEquals(1, count)
    }

    @Test
    fun `second open does not fire callbacks again`() {
        val gate = ReadyGate()
        var count = 0
        gate.await { count++ }

        gate.open()
        gate.open()

        assertEquals(1, count)
    }

    @Test
    fun `null callback is ignored`() {
        val gate = ReadyGate()
        gate.await(null)
        gate.open()
    }
}
