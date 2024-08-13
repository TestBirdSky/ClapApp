package com.example.clapapp

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.clapapp.activities.SplashActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date


//class AppOpenManager(myApplication: MyApplication) : LifecycleObserver,
//    Application.ActivityLifecycleCallbacks {
//    private val myApplication: MyApplication
//    private var appOpenAd: AppOpenAd? = null
//    private var currentActivity: Activity? = null
//    private var loadTime: Long = 0
//
//    init {
//        this.myApplication = myApplication
//        this.myApplication.registerActivityLifecycleCallbacks(this)
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun onStart() {
//        if (currentActivity !is SplashActivity) {
//            showAdIfAvailable()
//        }
//    }
//
//    fun showAdIfAvailable() {
//        if (!isShowingAd && isAdAvailable) {
//            Log.d("AppOpenManagerLC_1", "Will show ad.")
//            val fullScreenContentCallback: FullScreenContentCallback =
//                object : FullScreenContentCallback() {
//                    override fun onAdDismissedFullScreenContent() {
//                        appOpenAd = null
//                        isShowingAd = false
//                        fetchAd()
//                    }
//
//                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
//                    override fun onAdShowedFullScreenContent() {
//                        isShowingAd = true
//                    }
//                }
//            if (!ispurchased) {
//                appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
//                appOpenAd!!.show(currentActivity!!)
//            }
//        } else {
//            Log.d("AppOpenManagerLC_2", "Can not show ad.")
//            fetchAd()
//        }
//    }
//
//    fun fetchAd() {
//        if (isAdAvailable) {
//            return
//        }
//        val loadCallback: AppOpenAd.AppOpenAdLoadCallback =
//            object : AppOpenAd.AppOpenAdLoadCallback() {
//                override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                    super.onAdLoaded(appOpenAd)
//                    this@AppOpenManager.appOpenAd = appOpenAd
//                    loadTime = Date().getTime()
//                }
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    super.onAdFailedToLoad(loadAdError)
//                }
//            }
//        val request: AdRequest = adRequest
//        AppOpenAd.load(
//            myApplication,
//            currentActivity!!.getString(R.string.app_open),
//            request,
//            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//            loadCallback
//        )
//////        AppOpenAd.load(
//////            myApplication,
//////            "ca-app-pub-3940256099942544/3419835294",
//////            request,
//////            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//////            loadCallback
////        )
//    }
//
//    private val adRequest: AdRequest
//        private get() = AdRequest.Builder().build()
//
//    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
//        val dateDifference: Long = Date().getTime() - loadTime
//        val numMilliSecondsPerHour: Long = 3600000
//        return dateDifference < numMilliSecondsPerHour * numHours
//    }
//    val isAdAvailable: Boolean
//        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
//
//    override fun onActivityCreated(activity: Activity, @Nullable bundle: Bundle?) {}
//    override fun onActivityStarted(activity: Activity) {
//        currentActivity = activity
//        isIsShowAd = true
//    }
//
//    override fun onActivityResumed(activity: Activity) {
//        currentActivity = activity
//    }
//
//    override fun onActivityPaused(activity: Activity) {
//        isIsShowAd = false
//    }
//
//    override fun onActivityStopped(activity: Activity) {
//        isShowingAd = false
//    }
//
//    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
//    override fun onActivityDestroyed(activity: Activity) {
//        currentActivity = null
//    }
//
//    companion object {
//        var isShowingAd = false
//        var isIsShowAd = false
//            private set
//
//        fun setIsShowAd(isShowAd: Boolean) {
//            isIsShowAd = isShowAd
//        }
//    }
//}