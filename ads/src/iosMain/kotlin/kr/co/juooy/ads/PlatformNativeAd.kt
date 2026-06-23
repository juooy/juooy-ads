@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import cocoapods.Google_Mobile_Ads_SDK.GADNativeAd

actual class PlatformNativeAd(val gadNativeAd: GADNativeAd) {
    actual fun destroy() {
        gadNativeAd.destroy()
    }
}
