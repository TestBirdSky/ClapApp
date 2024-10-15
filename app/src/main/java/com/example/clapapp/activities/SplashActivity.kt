package com.example.clapapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.clapapp.R
import com.example.clapapp.ispurchased
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initGDPRDialog(this)
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Ads...")
        progressDialog.setCancelable(false)

        val adRequest: AdRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,
            resources.getString(R.string.interstitial),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        Handler(Looper.myLooper()!!).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            if (!ispurchased) {
                if (mInterstitialAd != null) {
                    mInterstitialAd?.show(this)
                }
                lifecycleScope.launch {
                    delay(1000)
                    finish()
                }
            } else {
                finish()
                Log.d("BANNER_LC_2", "Can not show ad.")
            }
        }, 4000)

    }

    fun initGDPRDialog(activity: Activity) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("62FBE61399C9207A308F43FB62346591") // S9
            .build()

        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.reset()

        consentInformation.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                if (loadAndShowError != null) {
                    Log.d(
                        "TAGSplashConsentDi_",
                        "${loadAndShowError.errorCode}: ${loadAndShowError.message}"
                    )
                }

                if (consentInformation.isConsentFormAvailable) {
                    loadForm(activity, consentInformation)
                }

                Log.d(
                    "TAGSplashConsentDi_",
                    "phase 1 - $loadAndShowError\n" + "phase 2 - ${consentInformation.consentStatus} = ${consentInformation.isConsentFormAvailable} " + "= ${consentInformation.privacyOptionsRequirementStatus} = ${consentInformation.canRequestAds()}"
                )

                if (consentInformation.canRequestAds()) {
                    // App can start requesting ads.
                }
            }
        }, { requestConsentError ->
            Log.e(
                "TAGSplashConsentDi_2",
                "${requestConsentError.errorCode}: ${requestConsentError.message}"
            )
        })
    }

    fun loadForm(activity: Activity, consentInformation: ConsentInformation) {
        UserMessagingPlatform.loadConsentForm(activity, { consentForm ->
            if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.REQUIRED) {
                consentForm.show(activity) { formError ->
                    if (consentInformation.consentStatus == ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                    }

                    Log.d("TAGSplashConsentDi_", "phase 3 - ${consentInformation.consentStatus}")

                    loadForm(activity, consentInformation)
                }
            }
        }, { formError ->
            Log.e(
                "TAGSplashConsentDi_3", "${formError.errorCode}: ${formError.message}"
            )
        })
    }

}