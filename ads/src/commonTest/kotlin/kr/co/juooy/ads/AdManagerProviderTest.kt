package kr.co.juooy.ads

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AdManagerProviderTest {

    private val dummyConfig = AdConfig(
        appId = "test-app-id",
        bannerAdUnitId = "b",
        interstitialAdUnitId = "i",
        rewardedAdUnitId = "r",
        nativeAdUnitId = "n",
        exitAdUnitId = "e"
    )

    @Test
    fun `get throws when not set`() {
        val freshProvider = object {
            private var instance: AdManager? = null
            fun get(): AdManager = checkNotNull(instance) { "AdManager not set." }
        }
        assertFailsWith<IllegalStateException> { freshProvider.get() }
    }

    @Test
    fun `set and get returns same instance`() {
        val manager = object : AdManager {
            override fun initialize(config: AdConfig, listener: AdEventListener?) {}
            override fun isInitialized() = false
            override val config: AdConfig = dummyConfig
        }
        AdManagerProvider.set(manager)
        assertNotNull(AdManagerProvider.getOrNull())
    }
}
