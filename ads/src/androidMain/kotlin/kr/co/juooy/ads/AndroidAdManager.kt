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
    private val readyGate = ReadyGate()

    private var preloadedInterstitial: InterstitialAd? = null
    private val isLoadingInterstitial = AtomicBoolean(false)
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
        val cfg = config ?: run {
            Log.e(TAG, "initialize(AdConfig) must be called before initializeWithActivity()")
            return
        }
        // Queue before the initCalled check: a second caller during in-flight init must not lose onReady
        readyGate.await(onReady)
        if (initCalled.getAndSet(true)) return
        consentStrategy.requestConsent(
            activity = activity,
            onSuccess = { initMobileAds(activity, cfg) },
            onFailure = {
                listener?.onAdFailed(kr.co.juooy.ads.AdError(-1, "Consent failed or unavailable", AdType.BANNER))
                readyGate.open()
            }
        )
    }

    private fun initMobileAds(activity: Activity, cfg: AdConfig) {
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
                readyGate.open()
            }
        }
        MobileAds.initialize(activity) {
            timeoutJob?.cancel()
            if (initialized.getAndSet(true)) return@initialize
            Log.d(TAG, "MobileAds initialized")
            listener?.onAdLoaded(AdType.BANNER)
            readyGate.open()
            weakActivity.get()?.let { act ->
                preloadInterstitial(act.applicationContext)
                preloadRewarded(act.applicationContext)
            }
        }
    }

    // --- Native ads ---

    override fun loadNativeAd(
        context: PlatformContext,
        adUnitId: String,
        onLoaded: (PlatformNativeAd) -> Unit,
        onFailed: ((kr.co.juooy.ads.AdError) -> Unit)?
    ) {
        if (adUnitId.isBlank()) {
            Log.e(TAG, "nativeAdUnitId is blank")
            return
        }
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd -> onLoaded(PlatformNativeAd(nativeAd)) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(TAG, "Native ad failed: ${error.message}")
                    val adError = kr.co.juooy.ads.AdError(error.code, error.message, AdType.NATIVE)
                    listener?.onAdFailed(adError)
                    onFailed?.invoke(adError)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    // --- Interstitial ads ---

    fun preloadInterstitial(context: Context) {
        val adUnitId = config?.interstitialAdUnitId?.takeIf { it.isNotBlank() } ?: return
        if (preloadedInterstitial != null || isLoadingInterstitial.getAndSet(true)) return
        InterstitialAd.load(
            context, adUnitId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoadingInterstitial.set(false)
                    preloadedInterstitial = ad
                    listener?.onAdLoaded(AdType.INTERSTITIAL)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoadingInterstitial.set(false)
                    Log.e(TAG, "Interstitial failed to load: ${error.message}")
                    listener?.onAdFailed(kr.co.juooy.ads.AdError(error.code, error.message, AdType.INTERSTITIAL))
                }
            }
        )
    }

    fun hasInterstitial(): Boolean = preloadedInterstitial != null

    // PlatformContext = Activity on Android, so this satisfies both the direct API and the interface override.
    override fun showInterstitial(context: PlatformContext, onDismissed: (() -> Unit)?): Boolean {
        if (context.isFinishing || context.isDestroyed) return false
        val ad = preloadedInterstitial ?: run {
            // A failed preload (e.g. offline at launch) is otherwise never retried in-process
            if (initialized.get()) preloadInterstitial(context.applicationContext)
            return false
        }
        val appContext = context.applicationContext
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                listener?.onAdShown(AdType.INTERSTITIAL)
            }

            override fun onAdClicked() {
                listener?.onAdClicked(AdType.INTERSTITIAL)
            }

            override fun onAdDismissedFullScreenContent() {
                preloadedInterstitial = null
                preloadInterstitial(appContext)
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                preloadedInterstitial = null
                preloadInterstitial(appContext)
                val adError = kr.co.juooy.ads.AdError(error.code, error.message, AdType.INTERSTITIAL)
                listener?.onAdFailedToShow(adError)
                listener?.onAdFailed(adError)
                onDismissed?.invoke()
            }
        }
        ad.show(context)
        return true
    }

    // --- Rewarded ads ---

    fun preloadRewarded(context: Context) {
        val adUnitId = config?.rewardedAdUnitId?.takeIf { it.isNotBlank() } ?: return
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

    // PlatformContext = Activity on Android, so this satisfies both the direct API and the interface override.
    override fun showRewarded(
        context: PlatformContext,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: (() -> Unit)?
    ): Boolean {
        if (context.isFinishing || context.isDestroyed) return false
        val ad = preloadedRewarded ?: return false
        val appContext = context.applicationContext
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                listener?.onAdShown(AdType.REWARDED)
            }

            override fun onAdClicked() {
                listener?.onAdClicked(AdType.REWARDED)
            }

            override fun onAdDismissedFullScreenContent() {
                preloadedRewarded = null
                preloadRewarded(appContext)
                onDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                val adError = kr.co.juooy.ads.AdError(error.code, error.message, AdType.REWARDED)
                listener?.onAdFailedToShow(adError)
                listener?.onAdFailed(adError)
                onDismissed?.invoke()
            }
        }
        ad.show(context) { reward ->
            listener?.onUserEarnedReward(reward.amount, reward.type)
            onUserEarnedReward(reward.amount, reward.type)
        }
        return true
    }

    override fun isInitialized(): Boolean = initialized.get()

    override fun initializeWithPlatformContext(context: PlatformContext, onReady: (() -> Unit)?) {
        initializeWithActivity(context, onReady = onReady)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun initializeWithContext(context: Any, onReady: (() -> Unit)?) {
        val activity = context as? Activity ?: run {
            Log.w(TAG, "initializeWithContext: expected Activity, got ${context::class}")
            return
        }
        initializeWithActivity(activity, onReady = onReady)
    }
}
