@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined

interface IosConsentStrategy {
    fun requestConsent(onComplete: () -> Unit)
}

class AttIosConsentStrategy : IosConsentStrategy {
    override fun requestConsent(onComplete: () -> Unit) {
        val status = ATTrackingManager.trackingAuthorizationStatus
        if (status == ATTrackingManagerAuthorizationStatusNotDetermined) {
            ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { _ -> onComplete() }
        } else {
            onComplete()
        }
    }
}

class NoOpIosConsentStrategy : IosConsentStrategy {
    override fun requestConsent(onComplete: () -> Unit) = onComplete()
}
