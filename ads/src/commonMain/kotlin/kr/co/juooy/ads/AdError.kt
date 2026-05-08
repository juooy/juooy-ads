package kr.co.juooy.ads

data class AdError(
    val code: Int,
    val message: String,
    val type: AdType
)
