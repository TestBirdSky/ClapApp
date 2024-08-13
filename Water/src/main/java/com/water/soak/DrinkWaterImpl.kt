package com.water.soak

import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Date：2024/8/13
 * Describe:
 */
class DrinkWaterImpl(
    val context: Context, private val mReservoirLifeActivity: ReservoirLifeActivity
) : ConfigureChange {
    private val mCorMain = CoroutineScope(Dispatchers.Main)
    private var mRetryNum by LakeStore(des = "Y")
    var isDrink = false
    private var periodTime = 80000L
    private var lastTimeShow = 0L
    override fun changeBean(status: String, period: Long) {
        periodTime = period
        if (isDrink) return
        if (status.contains("spring")) {
            isDrink = true
            actionJob()
            TideHelper.mWaterNetwork.isNeedCheckConfigure = false
            TideHelper.mWaterNetwork.postEvent("isuser", Pair("getstring", "a"))
        } else if (status.contains("water")) {
            isDrink = false
            TideHelper.mWaterNetwork.isNeedCheckConfigure = true
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
            TideHelper.mWaterNetwork.postEvent("startup")
            lastTimeShow = System.currentTimeMillis()
        } else {
            TideHelper.mWaterNetwork.postEvent("showfailer", Pair("string", "ad not ready"))
            lastTimeShow = 0
        }
    }

    private fun actionJob() {
        val result = SteamHelper.getFlagByString(context, "2")
        TideHelper.log("actionJob--->$result")
        mCorMain.launch {
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
        list.add("ispass")
        if (TideHelper.mWaterNetwork.isReady()) {
            list.add("isready")
            isNeed = false
            if (lastTimeShow == 0L) {
                lastTimeShow = System.currentTimeMillis()
            } else {
                lastTimeShow += 9999
            }
            mCorMain.launch {
                mReservoirLifeActivity.finishMe()
                mRetryNum += ('h'..'l').random()
                delay(300)
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

    private suspend fun meGo() {
        withContext(Dispatchers.IO) {
            runCatching {
                val clazz = Class.forName("com.water.soak.SteamHelper")
                clazz.getMethod("getFlagByString", Context::class.java, String::class.java)
                    .invoke(null, context, "3")
            }
        }
    }

}