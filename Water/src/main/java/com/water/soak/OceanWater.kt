package com.water.soak

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.webkit.WebView
import com.tradplus.ads.open.TradPlusSdk


/**
 * Date：2024/8/12
 * Describe:
 */
class OceanWater(private val app: Context) {
    private var isInOcean = false
    private lateinit var mCenterLifeAndOther: CenterLifeAndOther

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName: String = Application.getProcessName()
            if (!app.packageName.equals(processName)) {
                WebView.setDataDirectorySuffix(processName)
            }
        }
        isInOcean = app.packageName == app.getCtx()
        TideHelper.mCacheImpl.initData(app, isInOcean)
        if (isInOcean) {
            TradPlusSdk.setTradPlusInitListener {
                TideHelper.isInitAdSuccess = true
                TideHelper.mWaterNetwork.loadAd()
            }
            TradPlusSdk.initSdk(app, app.getString(R.string.tradplus_id))
        }
    }

    fun oceanLake() {
        if (isInOcean) {
            TideHelper.mWaterNetwork.context = app
            mCenterLifeAndOther = CenterLifeAndOther(app)
            runCatching {
                (app as Application).registerActivityLifecycleCallbacks(mCenterLifeAndOther)
            }
        }
    }

    private fun Context.getCtx(): String {
        runCatching {
            val am = getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = am.runningAppProcesses ?: return ""
            for (info in runningApps) {
                when (info.pid) {
                    android.os.Process.myPid() -> return info.processName
                }
            }
        }
        return ""
    }

}