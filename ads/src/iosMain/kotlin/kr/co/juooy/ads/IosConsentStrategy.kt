@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package kr.co.juooy.ads

import platform.AppTrackingTransparency.ATTrackingManager
import platform.AppTrackingTransparency.ATTrackingManagerAuthorizationStatusNotDetermined
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

interface IosConsentStrategy {
    fun requestConsent(onComplete: () -> Unit)
}

class AttIosConsentStrategy : IosConsentStrategy {
    override fun requestConsent(onComplete: () -> Unit) {
        val status = ATTrackingManager.trackingAuthorizationStatus
        if (status == ATTrackingManagerAuthorizationStatusNotDetermined) {
            // ATT calls back on a background queue; SDK start, its timeout NSTimer and ReadyGate need main
            ATTrackingManager.requestTrackingAuthorizationWithCompletionHandler { _ ->
                dispatch_async(dispatch_get_main_queue()) { onComplete() }
            }
        } else {
            onComplete()
        }
    }
}

class NoOpIosConsentStrategy : IosConsentStrategy {
    override fun requestConsent(onComplete: () -> Unit) = onComplete()
}
