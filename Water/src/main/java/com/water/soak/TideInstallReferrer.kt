package com.water.soak

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.ice.snow.IceService
import com.water.soak.base.BaseSoakNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Date：2024/8/12
 * Describe:
 */
class TideInstallReferrer : BaseSoakNetwork() {
    private val mJsonCommonImpl by lazy { JsonCommonImpl() }
    private var mReferrerStrCache by LakeStore("referrerCache")
    var isInApp = false

    fun setTheme(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.setTranslucent(true)
        } else {
            activity.window.setBackgroundDrawableResource(R.color.color_soak)
        }
    }


    fun register(context: Context) {
        if (TideHelper.mCacheImpl.mReferrerStr.isBlank()) {
            mScopeIO.launch {
                while (TideHelper.mCacheImpl.mReferrerStr.isBlank()) {
                    referrerRegister(context)
                    delay(13000)
                }
            }
        } else {
            if (mReferrerStrCache.isNotBlank()) {
                postInstallReferrer(TideHelper.mCacheImpl.mReferrerStr)
            }
            TideHelper.requestAdmin()
        }
    }

    private fun referrerRegister(context: Context) {
        val referrerClient = InstallReferrerClient.newBuilder(context).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(p0: Int) {
                runCatching {
                    if (p0 == InstallReferrerClient.InstallReferrerResponse.OK) {
                        val response: ReferrerDetails = referrerClient.installReferrer
                        TideHelper.mCacheImpl.mReferrerStr = response.installReferrer
                        //todo delete
                        if (IS_TEST) {
                            TideHelper.log("mGoogleReferStr-->${TideHelper.mCacheImpl.mReferrerStr}")
                            TideHelper.mCacheImpl.mReferrerStr += "not%20set"
                        }
                        postInstallReferrer(TideHelper.mCacheImpl.mReferrerStr)
                        referrerClient.endConnection()
                    } else {
                        referrerClient.endConnection()
                    }
                }.onFailure {
                    referrerClient.endConnection()
                }
            }

            override fun onInstallReferrerServiceDisconnected() = Unit
        })
    }

    fun startServiceTime(context: Context) {
        mScopeIO.launch(Dispatchers.Main) {
            delay(500)
            while (true) {
                if (Build.VERSION.SDK_INT < 31 || isInApp) {
                    startService(context)
                }
                delay(3049)
            }
        }
    }

    private var mLastEventServiceTime = 0L
    fun startService(context: Context): Boolean {
        if (TideHelper.isShowService) return true
        if (System.currentTimeMillis() - mLastEventServiceTime < 2000) return false
        mLastEventServiceTime = System.currentTimeMillis()
        runCatching {
            ContextCompat.startForegroundService(context, Intent(context, IceService::class.java))
        }
        return false
    }

    fun postInstallReferrer(referrerStr: String) {
        val request = TideHelper.toRequestInfo(
            mJsonCommonImpl.getReferrerJson(referrerStr), mJsonCommonImpl.URL_POST
        )
        postNet(request, 30, failed = {
            postInstallReferrer(referrerStr)
        }, success = {
            mReferrerStrCache = ""
        })
    }


}