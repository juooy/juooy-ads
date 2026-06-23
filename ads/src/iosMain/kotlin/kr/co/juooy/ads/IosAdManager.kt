@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import cocoapods.Google_Mobile_Ads_SDK.GADAdLoader
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenContentDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenPresentingAdProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADInterstitialAd
import cocoapods.Google_Mobile_Ads_SDK.GADMobileAds
import cocoapods.Google_Mobile_Ads_SDK.GADNativeAd
import cocoapods.Google_Mobile_Ads_SDK.GADNativeAdLoaderDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import cocoapods.Google_Mobile_Ads_SDK.GADRewardedAd
import cocoapods.Google_Mobile_Ads_SDK.GADAdLoaderAdTypeNative
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

private const val TAG = "IosAdManager"

class IosAdManager : AdManager {

    override var config: AdConfig? = null
        private set

    private var listener: AdEventListener? = null

    private var initCalled = false
    private var _isInitialized = false

    private var preloadedInterstitial: GADInterstitialAd? = null
    private var preloadedRewarded: GADRewardedAd? = null

    private val activeNativeLoaders = mutableListOf<GADAdLoader>()

    override fun initialize(config: AdConfig, listener: AdEventListener?) {
        this.config = config
        this.listener = listener
    }

    fun initializeWithViewController(
        viewController: UIViewController,
        consentStrategy: IosConsentStrategy = AttIosConsentStrategy(),
        onReady: (() -> Unit)? = null
    ) {
        if (initCalled) {
            if (_isInitialized) onReady?.invoke()
            return
        }
        initCalled = true
        consentStrategy.requestConsent { startGAD(onReady) }
    }

    private fun startGAD(onReady: (() -> Unit)?) {
        val cfg = config ?: run {
            println("[$TAG] initialize(AdConfig) must be called before initializeWithViewController()")
            return
        }

        if (cfg.isTestMode) {
            GADMobileAds.sharedInstance().requestConfiguration.testDeviceIdentifiers =
                listOf("GADSimulatorID")
        }

        var readyFired = false
        val timer = platform.Foundation.NSTimer.scheduledTimerWithTimeInterval(
            interval = 5.0,
            repeats = false
        ) { _ ->
            if (!readyFired) {
                readyFired = true
                println("[$TAG] GADMobileAds.start timed out after 5s")
                listener?.onAdFailed(AdError(-2, "GADMobileAds start timed out", AdType.BANNER))
                onReady?.invoke()
            }
        }

        GADMobileAds.sharedInstance().startWithCompletionHandler { _ ->
            timer.invalidate()
            if (!readyFired) {
                readyFired = true
                _isInitialized = true
                println("[$TAG] GADMobileAds initialized")
                listener?.onAdLoaded(AdType.BANNER)
                onReady?.invoke()
                preloadInterstitial()
                preloadRewarded()
            }
        }
    }

    // --- Native ads ---

    override fun loadNativeAd(
        context: PlatformContext,
        adUnitId: String,
        onLoaded: (PlatformNativeAd) -> Unit,
        onFailed: ((AdError) -> Unit)?
    ) {
        if (adUnitId.isBlank()) {
            println("[$TAG] nativeAdUnitId is blank")
            return
        }
        val delegate = NativeAdLoaderDelegate(
            onLoaded = { ad -> onLoaded(PlatformNativeAd(ad)) },
            onFailed = { error ->
                val adError = AdError(error.code.toInt(), error.localizedDescription, AdType.NATIVE)
                listener?.onAdFailed(adError)
                onFailed?.invoke(adError)
            },
            onLoadFinished = { loader -> activeNativeLoaders.remove(loader) }
        )
        val loader = GADAdLoader(
            adUnitID = adUnitId,
            rootViewController = context,
            adTypes = listOf(GADAdLoaderAdTypeNative),
            options = null
        )
        loader.delegate = delegate
        activeNativeLoaders.add(loader)
        loader.loadRequest(GADRequest())
    }

    // --- Interstitial ads ---

    fun preloadInterstitial() {
        val adUnitId = config?.interstitialAdUnitId ?: return
        GADInterstitialAd.loadWithAdUnitID(
            adUnitID = adUnitId,
            request = GADRequest()
        ) { ad, error ->
            if (ad != null) {
                preloadedInterstitial = ad
                listener?.onAdLoaded(AdType.INTERSTITIAL)
            } else {
                val err = error ?: return@loadWithAdUnitID
                println("[$TAG] Interstitial failed: ${err.localizedDescription}")
                listener?.onAdFailed(AdError(err.code.toInt(), err.localizedDescription, AdType.INTERSTITIAL))
            }
        }
    }

    fun hasInterstitial(): Boolean = preloadedInterstitial != null

    // PlatformContext = UIViewController on iOS, so this satisfies both direct API and interface override.
    override fun showInterstitial(context: PlatformContext, onDismissed: (() -> Unit)?): Boolean {
        val ad = preloadedInterstitial ?: return false
        val delegate = InterstitialDelegate(
            onDismissed = {
                preloadedInterstitial = null
                preloadInterstitial()
                onDismissed?.invoke()
            },
            onFailed = { error ->
                listener?.onAdFailed(error)
                onDismissed?.invoke()
            }
        )
        ad.fullScreenContentDelegate = delegate
        ad.presentFromRootViewController(context)
        return true
    }

    // --- Rewarded ads ---

    fun preloadRewarded() {
        val adUnitId = config?.rewardedAdUnitId ?: return
        GADRewardedAd.loadWithAdUnitID(
            adUnitID = adUnitId,
            request = GADRequest()
        ) { ad, error ->
            if (ad != null) {
                preloadedRewarded = ad
                listener?.onAdLoaded(AdType.REWARDED)
            } else {
                val err = error ?: return@loadWithAdUnitID
                println("[$TAG] Rewarded failed: ${err.localizedDescription}")
                listener?.onAdFailed(AdError(err.code.toInt(), err.localizedDescription, AdType.REWARDED))
            }
        }
    }

    fun hasRewarded(): Boolean = preloadedRewarded != null

    // PlatformContext = UIViewController on iOS, so this satisfies both direct API and interface override.
    override fun showRewarded(
        context: PlatformContext,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: (() -> Unit)?
    ): Boolean {
        val ad = preloadedRewarded ?: return false
        val delegate = RewardedDelegate(
            onDismissed = {
                preloadedRewarded = null
                preloadRewarded()
                onDismissed?.invoke()
            },
            onFailed = { error -> listener?.onAdFailed(error) }
        )
        ad.fullScreenContentDelegate = delegate
        val reward = ad.adReward
        ad.presentFromRootViewController(context) {
            listener?.onUserEarnedReward(reward.amount.intValue, reward.type)
            onUserEarnedReward(reward.amount.intValue, reward.type)
        }
        return true
    }

    override fun isInitialized(): Boolean = _isInitialized

    override fun initializeWithPlatformContext(context: PlatformContext, onReady: (() -> Unit)?) {
        initializeWithViewController(context, onReady = onReady)
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun initializeWithContext(context: Any, onReady: (() -> Unit)?) {
        val vc = context as? UIViewController ?: return
        initializeWithViewController(vc, onReady = onReady)
    }
}

// ---------------------------------------------------------------------------
// ObjC delegate implementations
// ---------------------------------------------------------------------------

private class InterstitialDelegate(
    private val onDismissed: () -> Unit,
    private val onFailed: (AdError) -> Unit
) : NSObject(), GADFullScreenContentDelegateProtocol {

    override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        onDismissed()
    }

    override fun ad(
        ad: GADFullScreenPresentingAdProtocol,
        didFailToPresentFullScreenContentWithError: NSError
    ) {
        onFailed(
            AdError(
                code = didFailToPresentFullScreenContentWithError.code.toInt(),
                message = didFailToPresentFullScreenContentWithError.localizedDescription,
                type = AdType.INTERSTITIAL
            )
        )
    }
}

private class RewardedDelegate(
    private val onDismissed: () -> Unit,
    private val onFailed: (AdError) -> Unit
) : NSObject(), GADFullScreenContentDelegateProtocol {

    override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        onDismissed()
    }

    override fun ad(
        ad: GADFullScreenPresentingAdProtocol,
        didFailToPresentFullScreenContentWithError: NSError
    ) {
        onFailed(
            AdError(
                code = didFailToPresentFullScreenContentWithError.code.toInt(),
                message = didFailToPresentFullScreenContentWithError.localizedDescription,
                type = AdType.REWARDED
            )
        )
    }
}

private class NativeAdLoaderDelegate(
    private val onLoaded: (GADNativeAd) -> Unit,
    private val onFailed: (NSError) -> Unit,
    private val onLoadFinished: (GADAdLoader) -> Unit
) : NSObject(), GADNativeAdLoaderDelegateProtocol {

    override fun adLoader(adLoader: GADAdLoader, didReceiveNativeAd: GADNativeAd) {
        onLoaded(didReceiveNativeAd)
        onLoadFinished(adLoader)
    }

    override fun adLoader(adLoader: GADAdLoader, didFailToReceiveAdWithError: NSError) {
        onFailed(didFailToReceiveAdWithError)
        onLoadFinished(adLoader)
    }

    override fun adLoaderDidFinishLoading(adLoader: GADAdLoader) {
        onLoadFinished(adLoader)
    }
}
