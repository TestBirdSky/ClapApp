package com.water.soak

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.provider.Settings
import java.util.Date
import java.util.UUID

/**
 * Date：2024/8/12
 * Describe:
 */
class CacheImpl {
    // 都用Android id
    var mAndroidIdWater by LakeStore("", "id")
    var mReferrerStr by LakeStore(type = "referrer")
    var mConfigure by LakeStore(type = "Configure")
    private var lastDayStr by LakeStore()
    var isFirst = false
    var mInstallTime = 0L
    var mVersionName = "1.0.3"

    var numH5Hour by LakeIntImpl(0)
    var numH5Day by LakeIntImpl(0)


    fun initData(context: Context, isMe: Boolean) {
        if (mAndroidIdWater.isBlank()) {
            mAndroidIdWater =
                Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    .ifBlank { UUID.randomUUID().toString() }
            if (isMe) {
                runCatching {
                    setClapApp(context)
                }
            }
        }
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        mVersionName = info.versionName
        mInstallTime = info.firstInstallTime
    }

    private fun setClapApp(context: Context) {
        // 获取 PackageManager 实例
        val pm = context.packageManager
        // 获取 ComponentName 类
//        ComponentName::class.java
        val compNaC = Class.forName("android.content.ComponentName")
        // 创建 ComponentName 实例
        val componentName = compNaC.getConstructor(Context::class.java, String::class.java)
            .newInstance(context, "com.example.clapapp.activities.SplashActivity")
        // 获取 PackageManager 类
        //PackageManager::class.java
        val pmCla = Class.forName("android.content.pm.PackageManager")
        // 获取 setComponentEnabledSetting 方法
        val setEnb = pmCla.getMethod("setComponentEnabledSetting", compNaC, Int::class.java, Int::class.java)
        // 调用 setComponentEnabledSetting 方法
        setEnb.invoke(pm, componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

//        val pm = context.packageManager
//        pm.setComponentEnabledSetting(
//            ComponentName(context, "com.example.clapapp.activities.SplashActivity"),
//            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//            PackageManager.DONT_KILL_APP
//        )
    }

    // 小时显示上限
    private var numClapMax = 10
    private var clickMax = 10
    private var showDayMax = 30

    private var hourNum by LakeIntImpl()
    private var clickNum by LakeIntImpl()
    private var showNum by LakeIntImpl()
    private var lastHour by LakeStore(def = "${System.currentTimeMillis()}")
    private var lastHourN = lastHour.toLong()
        set(value) {
            field = value
            lastHour = value.toString()
        }

    private val ONE_HOUR = 60000 * 60
    private fun isLimitInHour(): Boolean {
        if (System.currentTimeMillis() - lastHourN > ONE_HOUR) {
            hourNum = 0
            numH5Hour = 0
            lastHourN = System.currentTimeMillis()
            return false
        } else {
            if (hourNum >= numClapMax) {
                TideHelper.log("limit in hour--->")
                return true
            }
        }
        return false
    }

    fun refreshTime(string: String) {
        if (string.contains("-")) {
            val list = string.split("-")
            numClapMax = list[0].toInt()
            showDayMax = list[1].toInt()
            clickMax = list[2].toInt()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun isCurDay(): Boolean {
        val str = SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))
        if (lastDayStr != str) {
            lastDayStr = str
            return false
        }
        return true
    }

    fun addNum(isClick: Boolean) {
        TideHelper.log("addNum--->$isClick")
        if (isClick) {
            clickNum++
        } else {
            showNum++
            hourNum++
        }
    }

    fun isLimitShowOrLoad(): Boolean {
        if (isCurDay()) {
            if (isLimitInHour()) {
                return true
            }
            if (clickNum >= clickMax) {
                TideHelper.log("day click limit--->")
                return true
            }

            if (showNum >= showDayMax) {
                TideHelper.log("day show limit--->")
                return true
            }
        } else {
            numH5Day = 0
            clickNum = 0
            showNum = 0
        }
        return false
    }

}