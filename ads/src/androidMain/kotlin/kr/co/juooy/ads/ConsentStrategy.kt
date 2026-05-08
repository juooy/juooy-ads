package kr.co.juooy.ads

import android.app.Activity

interface ConsentStrategy {
    fun requestConsent(
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    )
}

class NoOpConsentStrategy : ConsentStrategy {
    override fun requestConsent(activity: Activity, onSuccess: () -> Unit, onFailure: () -> Unit) {
        onSuccess()
    }
}
