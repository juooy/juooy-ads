package kr.co.juooy.ads

interface AdEventListener {
    fun onAdLoaded(type: AdType) {}
    fun onAdFailed(error: AdError) {}
    fun onAdRevenuePaid(value: Double, currencyCode: String) {}
    fun onUserEarnedReward(amount: Int, rewardType: String) {}

    /** Full-screen ad (interstitial / rewarded) is about to cover the screen. */
    fun onAdShown(type: AdType) {}
    fun onAdClicked(type: AdType) {}

    /** Full-screen ad was loaded but could not be presented. [onAdFailed] fires too, for compatibility. */
    fun onAdFailedToShow(error: AdError) {}
}
