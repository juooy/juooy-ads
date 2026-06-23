@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import cocoapods.Google_Mobile_Ads_SDK.GADAppOpenAd
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenContentDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADFullScreenPresentingAdProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

private const val TAG = "IosAppOpenAdManager"
private const val AD_EXPIRY_MS = 4L * 60L * 60L * 1000L // 4 hours in ms

/**
 * Manages App Open Ads on iOS.
 *
 * Usage: call loadAd() once after SDK initialization, then call showIfAvailable()
 * from your AppDelegate/SceneDelegate applicationDidBecomeActive (or equivalent).
 *
 * Unlike Android, iOS has no automatic foreground hook — the caller must trigger
 * showIfAvailable() at the appropriate lifecycle point.
 */
class IosAppOpenAdManager(
    private val adManager: IosAdManager
) {
    private var appOpenAd: GADAppOpenAd? = null
    internal var loadTimeMs: Long = 0L
    private var isShowingAd = false
    private var isLoadingAd = false

    var isAdSuppressed: Boolean = false

    fun loadAd(adUnitId: String = adManager.config?.openAdUnitId ?: "") {
        if (adUnitId.isBlank()) return
        if (isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        GADAppOpenAd.loadWithAdUnitID(
            adUnitID = adUnitId,
            request = GADRequest()
        ) { ad, error ->
            isLoadingAd = false
            if (ad != null) {
                appOpenAd = ad
                loadTimeMs = currentTimeMs()
                println("[$TAG] App open ad loaded")
            } else {
                val err = error ?: return@loadWithAdUnitID
                println("[$TAG] App open ad failed to load: ${err.localizedDescription}")
            }
        }
    }

    /**
     * Show the app open ad if one is available. Call from applicationDidBecomeActive
     * or sceneDidBecomeActive in your AppDelegate/SceneDelegate.
     *
     * @return true if the ad was shown, false otherwise.
     */
    fun showIfAvailable(
        viewController: UIViewController,
        onDismissed: (() -> Unit)? = null
    ): Boolean {
        if (isAdSuppressed || isShowingAd) return false
        if (!isAdAvailable()) {
            loadAd()
            return false
        }
        val ad = appOpenAd ?: return false

        val delegate = AppOpenDelegate(
            onDismissed = {
                appOpenAd = null
                isShowingAd = false
                loadAd()
                onDismissed?.invoke()
            },
            onFailed = {
                isShowingAd = false
                println("[$TAG] App open ad failed to show: ${it.localizedDescription}")
            }
        )
        ad.fullScreenContentDelegate = delegate
        isShowingAd = true
        ad.presentFromRootViewController(viewController)
        return true
    }

    internal fun isAdAvailable(now: Long = currentTimeMs()): Boolean {
        val ad = appOpenAd ?: return false
        return (now - loadTimeMs) < AD_EXPIRY_MS
    }

    private fun currentTimeMs(): Long = platform.posix.time(null) * 1000L
}

private class AppOpenDelegate(
    private val onDismissed: () -> Unit,
    private val onFailed: (NSError) -> Unit
) : NSObject(), GADFullScreenContentDelegateProtocol {

    override fun adDidDismissFullScreenContent(ad: GADFullScreenPresentingAdProtocol) {
        onDismissed()
    }

    override fun ad(
        ad: GADFullScreenPresentingAdProtocol,
        didFailToPresentFullScreenContentWithError: NSError
    ) {
        onFailed(didFailToPresentFullScreenContentWithError)
    }
}
