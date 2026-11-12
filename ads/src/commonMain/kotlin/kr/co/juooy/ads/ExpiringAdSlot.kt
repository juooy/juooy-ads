package kr.co.juooy.ads

/** Google: discard rewarded/interstitial ad objects held longer than 1 hour; expired ads earn nothing. */
internal const val FULL_SCREEN_AD_MAX_AGE_MS = 60 * 60 * 1000L

/** Holds one preloaded ad and drops it once it is older than [maxAgeMs]. */
internal class ExpiringAdSlot<T : Any>(
    private val maxAgeMs: Long = FULL_SCREEN_AD_MAX_AGE_MS,
    private val nowMs: () -> Long,
) {
    private var ad: T? = null
    private var loadedAtMs = 0L

    fun put(value: T) {
        ad = value
        loadedAtMs = nowMs()
    }

    fun clear() {
        ad = null
    }

    /** Returns the ad if still valid. An expired ad is dropped and [onExpired] runs once. */
    fun fresh(onExpired: () -> Unit = {}): T? {
        val current = ad ?: return null
        val age = nowMs() - loadedAtMs
        if (age in 0 until maxAgeMs) return current
        ad = null
        onExpired()
        return null
    }
}
