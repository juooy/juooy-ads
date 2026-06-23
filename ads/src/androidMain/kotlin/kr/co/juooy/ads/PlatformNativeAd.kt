package kr.co.juooy.ads

import com.google.android.gms.ads.nativead.NativeAd

actual class PlatformNativeAd(val nativeAd: NativeAd) {
    actual fun destroy() = nativeAd.destroy()
}
