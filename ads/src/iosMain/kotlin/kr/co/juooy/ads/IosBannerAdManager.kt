@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import cocoapods.Google_Mobile_Ads_SDK.GADBannerView
import cocoapods.Google_Mobile_Ads_SDK.GADBannerViewDelegateProtocol
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import platform.Foundation.NSError
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * Factory for GADBannerView instances.
 *
 * Returns a loaded GADBannerView (UIView subclass). Embed it in your view hierarchy
 * using UIKit or UIViewRepresentable in SwiftUI. Configure adSize after creation if
 * needed (e.g. bannerView.adSize = GADAdSizeFluid).
 *
 * Example (Swift):
 *   let bannerView = IosBannerAdManager(adManager: iosAdManager).loadBannerAd(
 *       viewController: self,
 *       adUnitId: "ca-app-pub-xxx/yyy"
 *   )
 *   view.addSubview(bannerView)
 */
class IosBannerAdManager(private val adManager: IosAdManager) {

    private val activeDelegates = mutableMapOf<GADBannerView, BannerDelegate>()

    fun loadBannerAd(
        viewController: UIViewController,
        adUnitId: String = adManager.config?.bannerAdUnitId ?: "",
        onLoaded: (() -> Unit)? = null,
        onFailed: ((AdError) -> Unit)? = null
    ): GADBannerView {
        val bannerView = GADBannerView()
        bannerView.adUnitID = adUnitId
        bannerView.rootViewController = viewController
        val delegate = BannerDelegate(onLoaded = onLoaded, onFailed = onFailed)
        activeDelegates[bannerView] = delegate
        bannerView.delegate = delegate
        bannerView.loadRequest(GADRequest())
        return bannerView
    }

    fun removeBannerAd(view: GADBannerView) {
        activeDelegates.remove(view)
    }
}

private class BannerDelegate(
    private val onLoaded: (() -> Unit)?,
    private val onFailed: ((AdError) -> Unit)?
) : NSObject(), GADBannerViewDelegateProtocol {

    override fun bannerViewDidReceiveAd(bannerView: GADBannerView) {
        onLoaded?.invoke()
    }

    override fun bannerView(bannerView: GADBannerView, didFailToReceiveAdWithError: NSError) {
        onFailed?.invoke(AdError(didFailToReceiveAdWithError.code.toInt(), didFailToReceiveAdWithError.localizedDescription, AdType.BANNER))
    }
}
