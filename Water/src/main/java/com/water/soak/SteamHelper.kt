package com.water.soak

import android.app.ActivityManager
import android.app.Application
import android.app.job.JobService
import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * Date：2024/8/12
 * Describe:
 */
object SteamHelper {
    private val arrayList = arrayListOf<Any>("Demo")
    private var methodName = "goToOcean"

    fun init(context: Context) {
        MMKV.initialize(context)
        arrayList.add(context)
        arrayList.add("test")
        runCatching {
            val clazz = Class.forName("com.water.soak.SteamHelper")
            val conClazz = Class.forName("android.content.Context")
            clazz.getMethod(methodName, conClazz).invoke(null, arrayList["1".toInt()])
        }
    }

    @JvmStatic
    fun goToOcean(context: Context) {
        val ow = OceanWater(context)
        ow.oceanLake()
    }

    @JvmStatic
    external fun lakeStr(context: Context, string: String): Int


    @JvmStatic
    external fun aceNm(context: Context): String

    @JvmStatic
    fun isInMe(context: Context): Boolean {
        val am: ActivityManager =
            context.getSystemService(JobService.ACTIVITY_SERVICE) as ActivityManager
        val list0: List<ActivityManager.RunningAppProcessInfo> = am.runningAppProcesses
        for (info in list0) {
            if (!info.processName.equals(context.applicationInfo.processName) || info.importance != 100) {
                continue
            }
            return true
        }
        return false
    }

}