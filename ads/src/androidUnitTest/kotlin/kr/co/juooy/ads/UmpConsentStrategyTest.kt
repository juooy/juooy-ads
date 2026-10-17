package kr.co.juooy.ads

import com.google.android.ump.ConsentInformation.ConsentStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UmpConsentStrategyTest {

    @Test
    fun `allowed by UMP - initializes`() {
        assertTrue(canInitializeAds(canRequestAds = true, consentStatus = ConsentStatus.OBTAINED))
        assertTrue(canInitializeAds(canRequestAds = true, consentStatus = ConsentStatus.NOT_REQUIRED))
    }

    @Test
    fun `status never determined - initializes (no privacy message configured or offline)`() {
        assertTrue(canInitializeAds(canRequestAds = false, consentStatus = ConsentStatus.UNKNOWN))
    }

    @Test
    fun `consent required but not given - blocks`() {
        assertFalse(canInitializeAds(canRequestAds = false, consentStatus = ConsentStatus.REQUIRED))
    }
}
