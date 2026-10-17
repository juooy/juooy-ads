@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import cocoapods.Google_Mobile_Ads_SDK.GADMediaView
import cocoapods.Google_Mobile_Ads_SDK.GADNativeAdView
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIButton
import platform.UIKit.UIButtonTypeSystem
import platform.UIKit.UIColor
import platform.UIKit.UIControlStateNormal
import platform.UIKit.UIFont
import platform.UIKit.UIFontTextStyleBody
import platform.UIKit.UIFontTextStyleFootnote
import platform.UIKit.UIFontTextStyleHeadline
import platform.UIKit.UIImageView
import platform.UIKit.UILabel
import platform.UIKit.UILayoutConstraintAxisHorizontal
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentLeading
import platform.UIKit.UIStackViewAlignmentTop
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode

/**
 * Factory for a medium native ad card — iOS counterpart of Android `NativeAdViewBinder`.
 *
 * Returns a bound GADNativeAdView (UIView subclass). Embed it in your view hierarchy
 * using UIKit or UIViewRepresentable in SwiftUI. Fonts use Dynamic Type text styles,
 * so user font scaling is honored.
 *
 * Example (Swift):
 *   let adView = IosNativeAdViewFactory.shared.createNativeAdView(
 *       ad: platformNativeAd,
 *       badgeText: NSLocalizedString("ad_badge", comment: "")
 *   )
 *   stackView.addArrangedSubview(adView)
 */
object IosNativeAdViewFactory {

    fun createNativeAdView(ad: PlatformNativeAd, badgeText: String = "Ad"): GADNativeAdView {
        val nativeAd = ad.gadNativeAd
        val adView = GADNativeAdView()
        adView.backgroundColor = UIColor.systemBackgroundColor

        val badge = label(UIFontTextStyleFootnote, UIColor.whiteColor, lines = 0).apply {
            text = badgeText
            // White on grey 616161 = 6.2:1 contrast (WCAG AA)
            backgroundColor = UIColor.colorWithRed(0.38, green = 0.38, blue = 0.38, alpha = 1.0)
        }

        val icon = UIImageView().apply {
            contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
            translatesAutoresizingMaskIntoConstraints = false
            image = nativeAd.icon?.image
            hidden = nativeAd.icon == null
        }
        val headline = label(UIFontTextStyleHeadline, UIColor.labelColor, lines = 0).apply {
            text = nativeAd.headline
        }
        val body = label(UIFontTextStyleBody, UIColor.secondaryLabelColor, lines = 0).apply {
            text = nativeAd.body
            hidden = nativeAd.body.isNullOrBlank()
        }
        val media = GADMediaView().apply {
            translatesAutoresizingMaskIntoConstraints = false
            mediaContent = nativeAd.mediaContent
        }
        val callToAction = UIButton.buttonWithType(UIButtonTypeSystem).apply {
            setTitle(nativeAd.callToAction, forState = UIControlStateNormal)
            titleLabel?.font = UIFont.preferredFontForTextStyle(UIFontTextStyleBody)
            titleLabel?.adjustsFontForContentSizeCategory = true
            // GADNativeAdView handles the tap; the button itself must not swallow it
            userInteractionEnabled = false
            hidden = nativeAd.callToAction.isNullOrBlank()
            translatesAutoresizingMaskIntoConstraints = false
        }

        val texts = stack(UILayoutConstraintAxisVertical, spacing = 2.0, views = listOf(headline, body))
        val header = stack(UILayoutConstraintAxisHorizontal, spacing = 8.0, views = listOf(icon, texts)).apply {
            alignment = UIStackViewAlignmentTop
        }
        val root = stack(UILayoutConstraintAxisVertical, spacing = 8.0, views = listOf(badge, header, media, callToAction)).apply {
            alignment = UIStackViewAlignmentLeading
        }
        adView.addSubview(root)

        NSLayoutConstraint.activateConstraints(
            listOf(
                root.topAnchor.constraintEqualToAnchor(adView.topAnchor, constant = 8.0),
                root.bottomAnchor.constraintEqualToAnchor(adView.bottomAnchor, constant = -8.0),
                root.leadingAnchor.constraintEqualToAnchor(adView.leadingAnchor, constant = 12.0),
                root.trailingAnchor.constraintEqualToAnchor(adView.trailingAnchor, constant = -12.0),
                icon.widthAnchor.constraintEqualToConstant(40.0),
                icon.heightAnchor.constraintEqualToConstant(40.0),
                header.widthAnchor.constraintEqualToAnchor(root.widthAnchor),
                // MediaView: required by AdMob policy for main image/video, min 120pt
                media.widthAnchor.constraintEqualToAnchor(root.widthAnchor),
                media.heightAnchor.constraintGreaterThanOrEqualToConstant(120.0),
                callToAction.widthAnchor.constraintEqualToAnchor(root.widthAnchor),
                callToAction.heightAnchor.constraintGreaterThanOrEqualToConstant(44.0)
            )
        )

        adView.headlineView = headline
        adView.bodyView = body
        adView.iconView = icon
        adView.mediaView = media
        adView.callToActionView = callToAction
        adView.nativeAd = nativeAd
        return adView
    }

    /** lines = 0: no cap, so accessibility text sizes never truncate. */
    private fun label(textStyle: String?, color: UIColor, lines: Long): UILabel = UILabel().apply {
        font = UIFont.preferredFontForTextStyle(textStyle!!)
        adjustsFontForContentSizeCategory = true
        textColor = color
        numberOfLines = lines
        translatesAutoresizingMaskIntoConstraints = false
    }

    private fun stack(axis: Long, spacing: Double, views: List<UIView>): UIStackView =
        UIStackView(arrangedSubviews = views).apply {
            this.axis = axis
            this.spacing = spacing
            translatesAutoresizingMaskIntoConstraints = false
        }
}
