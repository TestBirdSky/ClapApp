package com.water.soak

import android.app.Activity
import android.content.Context
import android.util.Base64
import com.tradplus.ads.base.bean.TPAdError
import com.tradplus.ads.base.bean.TPAdInfo
import com.tradplus.ads.open.interstitial.InterstitialAdListener
import com.tradplus.ads.open.interstitial.TPInterstitial
import com.water.soak.base.BaseSoakNetwork
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Date：2024/8/13
 * Describe:
 */
class WaterNetwork : BaseSoakNetwork(), InterstitialAdListener {
    lateinit var context: Context
    var mChange: ConfigureChange? = null
    private var lastAdSaveTime = 0L
    private var isLoading = false
    private var lastRequestTime = 0L
    private var mTPInterstitial: TPInterstitial? = null
    private var mTPInterstitial2: TPInterstitial? = null
    private val second = 1000L
    var timeCheck = 40 * second
    private var timeWait = 460 * second
    private var timePeriod = 50 * second

    var isNeedCheckConfigure = true

    //不上报日志(snow) 方案B和不外弹(water) 方案A(spring)
    private var status = ""

    // 广告id
    private var idSnow = ""
    private var idSnow2 = ""

    private val mJsonCommonImpl by lazy { JsonCommonImpl(context.packageName) }

    private var lastTime = 0L
    fun postAdmin(isCheckTime: Boolean = true) {
        if (TideHelper.mWaterNetwork.isNeedCheckConfigure.not()) return
        if (isCheckTime && System.currentTimeMillis() - lastTime in 0 until 1000 * 60 * 60) return
        lastTime = System.currentTimeMillis()
        val time = "${System.currentTimeMillis()}"
        val length = time.length
        val body = mJsonCommonImpl.getAdminBodyJson().toString()
        val bodyEncoder = body.mapIndexed { index, c ->
            (c.code xor time[index % length].code).toChar()
        }.joinToString("")
        val request = TideHelper.toRequestInfo(
            Base64.encodeToString(bodyEncoder.toByteArray(), Base64.DEFAULT),
            mJsonCommonImpl.urlA,
            map = mapOf("dt" to time)
        )
        postNet(request, 15, failed = {
            postAdmin(false)
        }, success = {
            lastTime = System.currentTimeMillis()
        }, firstFailed = {
            if (TideHelper.mCacheImpl.mConfigure.isNotBlank()) {
                runCatching {
                    ref(JSONObject(TideHelper.mCacheImpl.mConfigure))
                }
            }
        }, str = "admin")
    }

    fun isTimeWait(): Boolean {
        return System.currentTimeMillis() - TideHelper.mCacheImpl.mInstallTime in 0 until timeWait
    }

    private fun ref(jsonObject: JSONObject) {
        jsonObject.apply {
            idSnow = optString("snow_id")
            idSnow2 = optString("spring_id")
            status = optString("info_cache")
            val listStr = optString("ice_time", "")
            if (listStr.isNotBlank() && listStr.contains("S")) {
                val lis = listStr.split("S")
                timeCheck = lis[0].toInt() * second
                timePeriod = lis[1].toInt() * second
                timeWait = lis[2].toInt() * second
            }
        }
        mChange?.changeBean(status, timePeriod)
    }

    override fun refreshData(string: String): String {
        val s = super.refreshData(string)
        runCatching {
            ref(JSONObject(s))
            TideHelper.mCacheImpl.mConfigure = s
        }
        return ""
    }

    fun postList(list: List<String>) {
        if (status.contains("snow")) {
            TideHelper.log("cancel post event $list")
            return
        }
        TideHelper.log("post event $list")
        val jsArray = JSONArray()
        list.forEach {
            jsArray.put(getEventJs(it))
        }
        val request = TideHelper.toRequestInfo(jsArray, mJsonCommonImpl.urlPost)
        postNet(request, 0)
    }

    fun postEvent(name: String, pair: Pair<String, String>? = null) {
        val num = getPostNum(name)
        if (status.contains("snow") && num == 0) {
            TideHelper.log("cancel post event $name ")
            return
        }
        TideHelper.log("post event $name $pair")
        val js = getEventJs(name, pair)
        val request = TideHelper.toRequestInfo(js, mJsonCommonImpl.urlPost)
        postNet(request, num)
    }

    private fun getEventJs(name: String, pair: Pair<String, String>? = null): JSONObject {
        val js = mJsonCommonImpl.getCommonJson().apply {
            put("enclave", name)
            pair?.let {
                put("janeiro<${it.first}", it.second)
            }
        }
        return js
    }

    private var closeInvoke: (() -> Unit)? = null

    fun showAd(activity: Activity, close: () -> Unit): Boolean {
        val ad = getReadyAd() ?: return false
        closeInvoke = {
            close.invoke()
        }
        ad.showAd(activity, "")
        return true
    }

    private fun postAdEvent(tpAdInfo: TPAdInfo) {
        postAdEvent(tpAdInfo, mJsonCommonImpl.getCommonJson(), mJsonCommonImpl.urlPost)
    }

    fun loadAd() {
        if (TideHelper.isInitAdSuccess.not()) return
        val str = load()
        TideHelper.log("loadAd-->$str")
    }

    private fun load(): String {
        if (idSnow.isBlank() && idSnow2.isBlank()) {
            return "ad id is null"
        }
        if (isLoading && System.currentTimeMillis() - lastRequestTime < 60000) {
            return "ad is loading"
        }
        if (isReady()) {
            return "ad is ready"
        }
        isLoading = true
        lastRequestTime = System.currentTimeMillis()
        lastAdSaveTime = System.currentTimeMillis()

        if (mTPInterstitial == null && idSnow.isNotBlank()) {
            mTPInterstitial = TPInterstitial(context, idSnow)
        }
        if (mTPInterstitial2 == null && idSnow2.isNotBlank()) {
            mTPInterstitial2 = TPInterstitial(context, idSnow2)
        }

        mTPInterstitial?.loadAd()
        mTPInterstitial?.setAdListener(this)
        mTPInterstitial2?.loadAd()
        mTPInterstitial2?.setAdListener(this)
        postEvent("reqprogress")
        return "ad load"
    }

    fun isReady(): Boolean {
        return isReadyAd() || isReadyAd2()
    }

    private fun getReadyAd(): TPInterstitial? {
        if (isReadyAd()) {
            return mTPInterstitial
        }
        if (isReadyAd2()) {
            return mTPInterstitial2
        }
        return null
    }

    private fun isReadyAd(): Boolean {
        val ad = mTPInterstitial ?: return false
        return ad.isReady
    }

    private fun isReadyAd2(): Boolean {
        val ad = mTPInterstitial2 ?: return false
        return ad.isReady
    }

    override fun onAdLoaded(p0: TPAdInfo?) {
        postEvent("getprogress")
        isLoading = false
        lastAdSaveTime = System.currentTimeMillis()
        job?.cancel()
        mChange?.loadAdSuccess()
    }

    private var job: Job? = null
    override fun onAdFailed(p0: TPAdError?) {
        postEvent("showfailer", Pair("string", "error_${p0?.errorCode}_${p0?.errorMsg}"))
        job?.cancel()
        job = mScopeIO.launch {
            delay(16000)
            isLoading = false
            load()
        }
    }

    override fun onAdImpression(p0: TPAdInfo?) {
        postEvent("showsuccess")
        p0?.let {
            postAdEvent(it)
        }
    }

    override fun onAdClicked(p0: TPAdInfo?) = Unit

    override fun onAdClosed(p0: TPAdInfo?) {
        closeInvoke?.invoke()
        closeInvoke = null
    }

    override fun onAdVideoError(p0: TPAdInfo?, p1: TPAdError?) {
        closeInvoke?.invoke()
        closeInvoke = null
    }

    override fun onAdVideoStart(p0: TPAdInfo?) = Unit

    override fun onAdVideoEnd(p0: TPAdInfo?) = Unit

}