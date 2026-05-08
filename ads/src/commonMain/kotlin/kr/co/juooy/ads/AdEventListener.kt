package kr.co.juooy.ads

interface AdEventListener {
    fun onAdLoaded(type: AdType) {}
    fun onAdFailed(error: AdError) {}
    fun onAdRevenuePaid(value: Double, currencyCode: String) {}
    fun onUserEarnedReward(amount: Int, rewardType: String) {}
}
