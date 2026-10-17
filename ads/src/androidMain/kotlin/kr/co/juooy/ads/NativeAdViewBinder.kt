package kr.co.juooy.ads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * Renders a [PlatformNativeAd] into the library's `juooy_ads_native_medium` layout.
 *
 * Example:
 *   val adView = NativeAdViewBinder.inflate(container)
 *   container.addView(adView)
 *   NativeAdViewBinder.bind(adView, platformNativeAd)
 *
 * Release the ad with [PlatformNativeAd.destroy] when the host is destroyed.
 */
object NativeAdViewBinder {

    fun inflate(parent: ViewGroup): NativeAdView =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.juooy_ads_native_medium, parent, false) as NativeAdView

    /** [adView] must come from [inflate] (or include `juooy_ads_native_medium`). Makes it visible. */
    fun bind(adView: NativeAdView, ad: PlatformNativeAd) {
        val nativeAd = ad.nativeAd
        val headline = adView.findViewById<TextView>(R.id.juooy_ads_headline)
        val body = adView.findViewById<TextView>(R.id.juooy_ads_body)
        val icon = adView.findViewById<ImageView>(R.id.juooy_ads_icon)
        val media = adView.findViewById<MediaView>(R.id.juooy_ads_media)
        val callToAction = adView.findViewById<Button>(R.id.juooy_ads_call_to_action)

        adView.headlineView = headline
        adView.bodyView = body
        adView.iconView = icon
        adView.mediaView = media
        adView.callToActionView = callToAction

        headline.text = nativeAd.headline
        body.text = nativeAd.body
        body.visibility = if (nativeAd.body.isNullOrBlank()) View.GONE else View.VISIBLE
        callToAction.text = nativeAd.callToAction
        callToAction.visibility = if (nativeAd.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE

        val iconDrawable = nativeAd.icon?.drawable
        icon.setImageDrawable(iconDrawable)
        icon.visibility = if (iconDrawable == null) View.GONE else View.VISIBLE
        nativeAd.mediaContent?.let { media.mediaContent = it }

        adView.setNativeAd(nativeAd)
        adView.visibility = View.VISIBLE
    }
}
