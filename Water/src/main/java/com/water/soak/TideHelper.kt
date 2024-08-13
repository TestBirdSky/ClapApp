package com.water.soak

import android.util.Log
import com.tencent.mmkv.MMKV
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Date：2024/8/12
 * Describe:
 */
// todo del
const val IS_TEST = true

object TideHelper {
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
}