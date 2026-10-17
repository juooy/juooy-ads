package kr.co.juooy.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

private const val TAG = "UmpConsentStrategy"

class UmpConsentStrategy : ConsentStrategy {
    override fun requestConsent(
        activity: Activity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val consentInfo = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInfo.requestConsentInfoUpdate(
            activity, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Consent form error ${formError.errorCode}: ${formError.message}")
                    }
                    if (canInitializeAds(consentInfo.canRequestAds(), consentInfo.consentStatus)) onSuccess() else onFailure()
                }
            },
            { requestError ->
                // e.g. no privacy message configured in AdMob, or offline
                Log.w(TAG, "Consent info update failed ${requestError.errorCode}: ${requestError.message}")
                if (canInitializeAds(consentInfo.canRequestAds(), consentInfo.consentStatus)) onSuccess() else onFailure()
            }
        )
    }
}

/**
 * Ads may start when UMP allows it, or when consent status was never determined
 * (the info update failed: missing AdMob privacy message, network). A known status that
 * disallows ads (consent required but not given) blocks initialization.
 */
internal fun canInitializeAds(canRequestAds: Boolean, consentStatus: Int): Boolean =
    canRequestAds || consentStatus == ConsentInformation.ConsentStatus.UNKNOWN
