package kr.co.juooy.ads

import android.app.Activity
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "AppOpenAdManager"
internal const val AD_EXPIRY_MS = 4 * 60 * 60 * 1000L // 4 hours

class AppOpenAdManager(
    private val adManager: AndroidAdManager,
) : DefaultLifecycleObserver {

    private var appOpenAd: AppOpenAd? = null
    internal var loadTime: Long = 0L
    private val isShowingAd = AtomicBoolean(false)
    private val isLoadingAd = AtomicBoolean(false)

    private var _currentActivity: WeakReference<Activity>? = null

    var currentActivity: Activity?
        get() = _currentActivity?.get()
        set(value) { _currentActivity = if (value != null) WeakReference(value) else null }

    // Host sets this to true during sessions where ads should be suppressed.
    var isAdSuppressed: Boolean = false

    // Called after the SDK confirms the ad was actually displayed.
    var onAdShown: (() -> Unit)? = null

    fun loadAd(activity: Activity, adUnitId: String) {
        if (adUnitId.isBlank()) return
        if (isLoadingAd.get() || isAdAvailable()) return
        isLoadingAd.set(true)
        AppOpenAd.load(
            activity.applicationContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTime = System.currentTimeMillis()
                    isLoadingAd.set(false)
                    Log.d(TAG, "App open ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingAd.set(false)
                    Log.e(TAG, "App open ad failed to load: ${error.message}")
                }
            }
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isAdSuppressed) return
        showAdIfAvailable()
    }

    private fun showAdIfAvailable() {
        if (isShowingAd.get()) return
        if (!isAdAvailable()) {
            val cfg = adManager.config ?: return
            currentActivity?.let { loadAd(it, cfg.openAdUnitId) }
            return
        }
        val activity = currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        val ad = appOpenAd ?: return

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onAdShown?.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd.set(false)
                val cfg = adManager.config ?: return
                currentActivity?.let { loadAd(it, cfg.openAdUnitId) }
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                isShowingAd.set(false)
                Log.e(TAG, "App open ad failed to show: ${error.message}")
            }
        }

        isShowingAd.set(true)
        ad.show(activity)
    }

    internal fun isAdAvailable(now: Long = System.currentTimeMillis()): Boolean {
        val ad = appOpenAd ?: return false
        return (now - loadTime) < AD_EXPIRY_MS
    }
}
