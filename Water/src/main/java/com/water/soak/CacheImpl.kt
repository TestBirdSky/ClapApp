package com.water.soak

import android.content.Context
import android.provider.Settings
import java.util.UUID

/**
 * Date：2024/8/12
 * Describe:
 */
class CacheImpl {
    // 都用Android id
    var mAndroidIdWater by LakeStore("", "id")
    var mReferrerStr by LakeStore("referrer")
    var mInstallTime = 0L
    var mVersionName = "1.0.6"

    fun initData(context: Context) {
        if (mAndroidIdWater.isBlank()) {
            mAndroidIdWater =
                Settings.System.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    .ifBlank { UUID.randomUUID().toString() }
        }
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        mVersionName = info.versionName
        mInstallTime = info.firstInstallTime
    }

}