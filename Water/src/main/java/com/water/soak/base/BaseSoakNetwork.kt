package com.water.soak.base

import android.util.Base64
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustAdRevenue
import com.adjust.sdk.AdjustConfig
import com.facebook.appevents.AppEventsLogger
import com.tradplus.ads.base.bean.TPAdInfo
import com.water.soak.TideHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Currency

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseSoakNetwork {
    val mScopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val okHttpClient = OkHttpClient()
    private val strEventName = "isuser-jumpfail-netjust"
    private var headerTime = "0"

    fun postAdEvent(tpAdInfo: TPAdInfo, jsonObject: JSONObject, url: String) {
        val js = jsonObject.apply {
            put("nosebag", "kerosene")
            put("deirdre", tpAdInfo.ecpm.toDouble() * 1000L)
            put("vertebra", "USD")
            put("inward", getTagString(tpAdInfo.adNetworkId.toInt()))
            put("chorine", "tradplus")
            put("agronomy", tpAdInfo.tpAdUnitId)
            put("aida", "tradplus_i")
            put("iodate", tpAdInfo.format ?: "Interstitial")
        }
        TideHelper.mWaterNetwork.postNet(TideHelper.toRequestInfo(js, url), 0)

        val adjustAdRevenue = AdjustAdRevenue(AdjustConfig.AD_REVENUE_SOURCE_PUBLISHER)
        adjustAdRevenue.setRevenue(tpAdInfo.ecpm.toDouble() / 1000, "USD")
        adjustAdRevenue.setAdRevenueUnit(tpAdInfo.adSourceId)
        adjustAdRevenue.setAdRevenuePlacement(tpAdInfo.adSourcePlacementId)
        //发送收益数据
        Adjust.trackAdRevenue(adjustAdRevenue)
    }

    open fun refreshData(string: String): String {
        val length = headerTime.length
        val ss64 = String(Base64.decode(string, Base64.DEFAULT))
        val jsStr = ss64.mapIndexed { index, c ->
            (c.code xor headerTime[index % length].code).toChar()
        }.joinToString("")
        TideHelper.log("refreshData--$jsStr")
        runCatching {
            return JSONObject(jsStr).optJSONObject("dGWkeiek")?.getString("conf") ?: ""
        }
        return ""
    }

    protected fun getPostNum(name: String): Int {
        return if (strEventName.contains(name)) {
            30
        } else {
            0
        }
    }

    fun postNet(
        request: Request,
        num: Int,
        str: String = "normal",
        failed: (() -> Unit)? = null,
        success: ((res: String) -> Unit)? = null,
        firstFailed: (() -> Unit)? = null
    ) {
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TideHelper.log("onFailure--->$e")
                if (num > 0) {
                    firstFailed?.invoke()
                    mScopeIO.launch {
                        delay(30000)
                        postNet(request, num - 1, str, failed, success)
                    }
                } else {
                    failed?.invoke()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                TideHelper.log("body--->$body")
                if (response.isSuccessful && response.code == 200) {
                    headerTime = response.headers["datetime"] ?: ""
                    if (str == "admin") {
                        refreshData(body)
                    }
                    success?.invoke(body)
                } else {
                    if (num > 0) {
                        firstFailed?.invoke()
                        mScopeIO.launch {
                            delay(45000)
                            postNet(request, num - 1, str, failed, success)
                        }
                    } else {
                        failed?.invoke()
                    }
                }
            }
        })
    }

    private fun getTagString(index: Int): String {
        return when (index) {
            1 -> "Facebook"
            9 -> "AppLovin"
            7 -> "vungle"
            57 -> "Bigo"
            23 -> "inmobi"
            18 -> "Mintegral"
            36 -> "Appnext"
            19 -> "pangle"
            75 -> "KwaiAds"
            else -> "tradplus_${index}"
        }
    }

}