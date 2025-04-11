package com.water.soak

import android.app.Activity
import android.view.ViewGroup
import com.tradplus.ads.base.bean.TPAdError
import com.tradplus.ads.base.bean.TPAdInfo
import com.tradplus.ads.base.bean.TPBaseAd
import com.tradplus.ads.open.splash.SplashAdListener
import com.tradplus.ads.open.splash.TPSplash

/**
 * Date：2025/4/11
 * Describe:
 */
class SoakOpenAdImpl {
    // todo modify
    private val soakAdIdStr = "0A600053F2B2775FF79B1CD046A0098C"
    private var mTPSplash: TPSplash? = null
    private var isLoading = false
    private var loadingTime = 0L
    fun loadSoakApp(activity: Activity) {
        if (isLoading && System.currentTimeMillis() - loadingTime < 60000 * 6) return
        if (isReadySoak()) return
        isLoading = true
        loadingTime = System.currentTimeMillis()
        mTPSplash = TPSplash(activity, soakAdIdStr)
        mTPSplash?.setAdListener(object : SplashAdListener() {
            override fun onAdLoaded(p0: TPAdInfo?, p1: TPBaseAd?) {
                super.onAdLoaded(p0, p1)
                isLoading = false
            }

            override fun onAdLoadFailed(p0: TPAdError?) {
                super.onAdLoadFailed(p0)
                isLoading = false
            }
        })
        mTPSplash?.loadAd(null)
    }

    fun isReadySoak(): Boolean {
        return mTPSplash?.isReady == true
    }

    fun showSoakAd(adContainer: ViewGroup, close: () -> Unit) {
        mTPSplash?.setAdListener(object : SplashAdListener() {
            override fun onAdClosed(p0: TPAdInfo?) {
                super.onAdClosed(p0)
                adContainer.removeAllViews()
                close.invoke()
            }

            override fun onAdShowFailed(p0: TPAdInfo?, p1: TPAdError?) {
                super.onAdShowFailed(p0, p1)
                close.invoke()
            }
        })
        mTPSplash?.showAd(adContainer)
    }

}