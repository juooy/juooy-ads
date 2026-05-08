package kr.co.juooy.ads

import android.app.Activity
import android.util.Log
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
                    if (consentInfo.canRequestAds()) onSuccess() else onFailure()
                }
            },
            { requestError ->
                Log.w(TAG, "Consent info update failed ${requestError.errorCode}: ${requestError.message}")
                onFailure()
            }
        )
    }
}
