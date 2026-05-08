package kr.co.juooy.ads

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AdConfigTest {

    @Test
    fun `appId blank throws`() {
        assertFailsWith<IllegalArgumentException> {
            AdConfig(
                appId = "",
                bannerAdUnitId = "b",
                interstitialAdUnitId = "i",
                rewardedAdUnitId = "r",
                nativeAdUnitId = "n",
                exitAdUnitId = "e"
            )
        }
    }

    @Test
    fun `valid config created`() {
        val config = AdConfig(
            appId = "ca-app-pub-test~1234567890",
            bannerAdUnitId = "b",
            interstitialAdUnitId = "i",
            rewardedAdUnitId = "r",
            nativeAdUnitId = "n",
            exitAdUnitId = "e",
            isTestMode = true
        )
        assertEquals("ca-app-pub-test~1234567890", config.appId)
        assertEquals(true, config.isTestMode)
    }
}
