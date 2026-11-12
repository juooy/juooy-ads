package kr.co.juooy.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExpiringAdSlotTest {
    private var now = 1_000_000L
    private val slot = ExpiringAdSlot<String>(maxAgeMs = 3_600_000L) { now }

    @Test
    fun emptySlotReturnsNullAndDoesNotReportExpiry() {
        var expired = false
        assertNull(slot.fresh { expired = true })
        assertFalse(expired)
    }

    @Test
    fun adJustUnderMaxAgeIsReturned() {
        slot.put("ad")
        now += 3_599_999L
        assertEquals("ad", slot.fresh())
    }

    @Test
    fun adAtMaxAgeIsDroppedAndReportedOnce() {
        slot.put("ad")
        now += 3_600_000L
        var expiredCount = 0
        assertNull(slot.fresh { expiredCount++ })
        assertNull(slot.fresh { expiredCount++ })
        assertEquals(1, expiredCount)
    }

    @Test
    fun clockMovedBackwardsTreatsAdAsExpired() {
        slot.put("ad")
        now -= 1L
        var expired = false
        assertNull(slot.fresh { expired = true })
        assertTrue(expired)
    }

    @Test
    fun clearEmptiesWithoutReportingExpiry() {
        slot.put("ad")
        slot.clear()
        now += 3_600_000L
        var expired = false
        assertNull(slot.fresh { expired = true })
        assertFalse(expired)
    }

    @Test
    fun putRestartsTheAge() {
        slot.put("old")
        now += 3_000_000L
        slot.put("new")
        now += 3_000_000L
        assertEquals("new", slot.fresh())
    }
}
