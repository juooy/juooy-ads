package kr.co.juooy.ads

data class AdConfig(
    val appId: String,
    val bannerAdUnitId: String,
    val interstitialAdUnitId: String,
    val rewardedAdUnitId: String,
    val nativeAdUnitId: String,
    val exitAdUnitId: String,
    val openAdUnitId: String = "",
    val isTestMode: Boolean = false
) {
    init {
        require(appId.isNotBlank()) { "appId must not be blank" }
    }
}
