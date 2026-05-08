package kr.co.juooy.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "AndroidAdManager"
private const val INIT_TIMEOUT_MS = 5_000L

class AndroidAdManager : kr.co.juooy.ads.AdManager {

    override var config: AdConfig? = null
        private set

    private var listener: AdEventListener? = null
    private val initCalled = AtomicBoolean(false)
    private val initialized = AtomicBoolean(false)

    private var preloadedInterstitial: InterstitialAd? = null
    private var preloadedRewarded: RewardedAd? = null

    override fun initialize(config: AdConfig, listener: AdEventListener?) {
        this.config = config
        this.listener = listener
    }

    fun initializeWithActivity(
        activity: Activity,
        consentStrategy: ConsentStrategy = UmpConsentStrategy(),
        onReady: (() -> Unit)? = null
    ) {
        if (initCalled.getAndSet(true)) {
            if (initialized.get()) onReady?.invoke()
            return
        }
        val cfg = config ?: run {
            Log.e(TAG, "initialize(AdConfig) must be called before initializeWithActivity()")
            return
        }
        consentStrategy.requestConsent(
            activity = activity,
            onSuccess = { initMobileAds(activity, cfg, onReady) },
            onFailure = {
                listener?.onAdFailed(kr.co.juooy.ads.AdError(-1, "Consent failed or unavailable", AdType.BANNER))
                onReady?.invoke()
            }
        )
    }

    private fun initMobileAds(activity: Activity, cfg: AdConfig, onReady: (() -> Unit)?) {
        if (cfg.isTestMode) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
            )
        }
        val weakActivity = WeakReference(activity)
        var timeoutJob: Job? = null
        timeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(INIT_TIMEOUT_MS)
            if (!initialized.get()) {
                Log.w(TAG, "MobileAds.initialize timed out after ${INIT_TIMEOUT_MS}ms")
                listener?.onAdFailed(kr.co.juooy.ads.AdError(-2, "MobileAds initialization timed out", AdType.BANNER))
                onReady?.invoke()
            }
        }
        MobileAds.initialize(activity) {
            timeoutJob?.cancel()
            if (initialized.getAndSet(true)) return@initialize
            Log.d(TAG, "MobileAds initialized")
            listener?.onAdLoaded(AdType.BANNER)
            onReady?.invoke()
            weakActivity.get()?.let { act ->
                preloadInterstitial(act.applicationContext)
                preloadRewarded(act.applicationContext)
            }
        }
    }

    // --- Native ads ---

    fun loadNativeAd(
        context: Context,
        adUnitId: String = config?.nativeAdUnitId ?: "",
        onLoaded: (NativeAd) -> Unit,
        onFailed: ((LoadAdError) -> Unit)? = null
    ) {
        if (adUnitId.isBlank()) {
            Log.e(TAG, "nativeAdUnitId is blank")
            return
        }
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd -> onLoaded(nativeAd) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed: ${error.message}")
                    listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.NATIVE))
                    onFailed?.invoke(error)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    // --- Interstitial ads ---

    fun preloadInterstitial(context: Context) {
        val adUnitId = config?.interstitialAdUnitId ?: return
        InterstitialAd.load(
            context, adUnitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    preloadedInterstitial = ad
                    listener?.onAdLoaded(AdType.INTERSTITIAL)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Interstitial failed to load: ${error.message}")
                    listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.INTERSTITIAL))
                }
            }
        )
    }

    fun hasInterstitial(): Boolean = preloadedInterstitial != null

    fun showInterstitial(activity: Activity, onDismissed: (() -> Unit)? = null): Boolean {
        val ad = preloadedInterstitial ?: return false
        val appContext = activity.applicationContext
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadedInterstitial = null
                preloadInterstitial(appContext)
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.INTERSTITIAL))
                onDismissed?.invoke()
            }
        }
        ad.show(activity)
        return true
    }

    // --- Rewarded ads ---

    fun preloadRewarded(context: Context) {
        val adUnitId = config?.rewardedAdUnitId ?: return
        RewardedAd.load(
            context, adUnitId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    preloadedRewarded = ad
                    listener?.onAdLoaded(AdType.REWARDED)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${error.message}")
                    listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.REWARDED))
                }
            }
        )
    }

    fun hasRewarded(): Boolean = preloadedRewarded != null

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: (() -> Unit)? = null
    ): Boolean {
        val ad = preloadedRewarded ?: return false
        val appContext = activity.applicationContext
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadedRewarded = null
                preloadRewarded(appContext)
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.REWARDED))
                onDismissed?.invoke()
            }
        }
        ad.show(activity) { reward ->
            listener?.onUserEarnedReward(reward.amount, reward.type)
            onUserEarnedReward(reward.amount, reward.type)
        }
        return true
    }

    override fun isInitialized(): Boolean = initialized.get()
}
