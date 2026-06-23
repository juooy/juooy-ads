package kr.co.juooy.ads

interface AdManager {
    fun initialize(config: AdConfig, listener: AdEventListener? = null)
    fun isInitialized(): Boolean
    val config: AdConfig?

    fun initializeWithPlatformContext(context: PlatformContext, onReady: (() -> Unit)? = null) {}

    fun showInterstitial(context: PlatformContext, onDismissed: (() -> Unit)? = null): Boolean = false

    fun showRewarded(
        context: PlatformContext,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: (() -> Unit)? = null
    ): Boolean = false

    fun loadNativeAd(
        context: PlatformContext,
        adUnitId: String,
        onLoaded: (PlatformNativeAd) -> Unit,
        onFailed: ((AdError) -> Unit)? = null
    ) {}

    @Deprecated(
        message = "Use initializeWithPlatformContext instead.",
        replaceWith = ReplaceWith("initializeWithPlatformContext(context as PlatformContext, onReady)")
    )
    fun initializeWithContext(context: Any, onReady: (() -> Unit)? = null) {}
}

object AdManagerProvider {
    @kotlin.concurrent.Volatile
    private var instance: AdManager? = null

    fun set(manager: AdManager) {
        instance = manager
    }

    fun get(): AdManager = checkNotNull(instance) {
        "AdManager not set. Call AdManagerProvider.set() in Application.onCreate()."
    }

    fun getOrNull(): AdManager? = instance
}
