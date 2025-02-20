package com.water.soak

import android.util.Log
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.random.Random

/**
 * Date：2024/8/12
 * Describe:
 */
// todo del
const val IS_TEST = true

object TideHelper {
    var h5Status = 0 //44 h5  99 ad
    val mWaterNetwork by lazy { WaterNetwork() }
    var isInitAdSuccess = false
    var isShowService = false

    val mmkv: MMKV by lazy { MMKV.defaultMMKV() }
    val mCacheImpl by lazy { CacheImpl() }
    private const val TAG = "Tide->"

    fun log(msg: String) {
        if (IS_TEST) {
            Log.e(TAG, msg)
        }
    }

    fun requestAdmin() {
        mWaterNetwork.postAdmin()
    }

    fun toRequestInfo(any: Any, url: String, map: Map<String, String>? = null): Request {
        return Request.Builder().post(
            any.toString().toRequestBody("application/json".toMediaType())
        ).apply {
            map?.forEach { (t, u) ->
                header(t, u)
            }
        }.url(url).build()
    }

    val delayTime get() = Random.nextLong(delayTimeStart, delayTimeEnd)

    var delayTimeStart = 1000L
    var delayTimeEnd = 3000L

    fun isH5Allow(): Boolean {
        if (mWaterNetwork.stringH5Url.isBlank()) return false
        if (mCacheImpl.numH5Hour >= mWaterNetwork.hourMaxH5 || mCacheImpl.numH5Day >= mWaterNetwork.dayMaxH5) return false
        return true
    }

    fun isInH5Time(): Boolean {
        if (isH5Allow().not()) return false
        if (System.currentTimeMillis() - mCacheImpl.mInstallTime > mWaterNetwork.h5TimeNow + mWaterNetwork.timeWait) return false
        return true
    }

    suspend fun showH5Event() {
        withContext(Dispatchers.Main) {
            delay(600)
            runCatching {
                h5Status = 44
                val clazz = Class.forName("com.water.soak.SteamHelper")
                clazz.getMethod("getFlagByString", String::class.java).invoke(null, "3")
            }
        }
    }
}