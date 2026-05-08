package kr.co.juooy.ads

interface AdManager {
    fun initialize(config: AdConfig, listener: AdEventListener? = null)
    fun isInitialized(): Boolean
    val config: AdConfig?
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
