package com.water.soak

import android.annotation.SuppressLint
import android.content.Context
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
    var mInstallTime = 0L
    var mVersionName = "1.0.2"

    var numH5Hour by LakeIntImpl(0)
    var numH5Day by LakeIntImpl(0)


    fun initData(context: Context, isMe: Boolean) {
        if (mAndroidIdWater.isBlank()) {
            mAndroidIdWater =
                Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    .ifBlank { UUID.randomUUID().toString() }
        }
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        mVersionName = info.versionName
        mInstallTime = info.firstInstallTime
        if (isMe) {

        }
    }

    // 小时显示上限
    private var numClapMax = 10
    private var clickMax = 10
    private var showDayMax = 30

    private var hourNum by LakeIntImpl()
    private var clickNum by LakeIntImpl()
    private var showNum by LakeIntImpl()
    private var lastHour by LakeStore(des = "${System.currentTimeMillis()}")
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