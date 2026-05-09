package kr.co.juooy.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAdView(
    adUnitId: String,
    adSize: AdSize = AdSize.BANNER,
    modifier: Modifier = Modifier,
    onAdLoaded: (() -> Unit)? = null,
    onAdFailed: ((LoadAdError) -> Unit)? = null,
) {
    if (adUnitId.isBlank()) return

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(adUnitId)
            adListener = object : AdListener() {
                override fun onAdLoaded() { onAdLoaded?.invoke() }
                override fun onAdFailedToLoad(error: LoadAdError) { onAdFailed?.invoke(error) }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME  -> adView.resume()
                Lifecycle.Event.ON_PAUSE   -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    AndroidView(factory = { adView }, modifier = modifier)
}
