package com.water.soak

import android.app.Activity
import android.content.Context
import android.util.Base64
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustEvent
import com.facebook.appevents.AppEventsLogger
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
import java.util.Currency

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
    private var fileName = ""

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
            map = mapOf("datetime" to time)
        )
        postNet(request, 5, failed = {
            if (TideHelper.mCacheImpl.mConfigure.isBlank()) {
                postAdmin(false)
            }
        }, success = {
            lastTime = System.currentTimeMillis()
        }, firstFailed = {

        }, str = "admin")
    }

    private var isFirst = true
    fun firstRefresh() {
        if (isFirst) {
            isFirst = false
            if (TideHelper.mCacheImpl.mConfigure.isNotBlank()) {
                mScopeIO.launch {
                    delay(4000)
                    runCatching {
                        ref(JSONObject(TideHelper.mCacheImpl.mConfigure))
                    }
                }
            }
        }
    }

    fun isTimeWait(): Boolean {
        return System.currentTimeMillis() - TideHelper.mCacheImpl.mInstallTime in 0 until timeWait
    }

    private fun ref(jsonObject: JSONObject) {
        jsonObject.apply {
            idSnow = optString("snow_id")
            idSnow2 = optString("spring_id")
            status = optString("info_cache")
            fileName = optString("ice_n")
            TideHelper.delayTime = optLong("snow_time", (1011..3000L).random())
            refreshLimit(optString("ice_steam_num", "10-20-9"))
            val listStr = optString("ice_time", "")
            if (listStr.isNotBlank() && listStr.contains("S")) {
                val lis = listStr.split("S")
                timeCheck = lis[0].toInt() * second
                timePeriod = lis[1].toInt() * second
                timeWait = lis[2].toInt() * second
            }
        }
        mChange?.changeBean(status, timePeriod, fileName)
    }

    private fun refreshLimit(string: String) {
        runCatching {
            TideHelper.mCacheImpl.refreshTime(string)
        }
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
        if (list.size == 1) {
            postEvent(list[0])
            return
        }
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
            put("nosebag", name)
            pair?.let {
                put("${it.first}@locutor", it.second)
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
        if (TideHelper.mCacheImpl.isLimitShowOrLoad()) {
            return "limit--->"
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

        if (isReadyAd().not()) {
            mTPInterstitial?.let {
                it.loadAd()
                it.setAdListener(this)
                postEvent("reqprogress")

            }
        }

        if (isReadyAd2().not()) {
            mTPInterstitial2?.let {
                it.loadAd()
                it.setAdListener(this)
                postEvent("reqprogress")

            }
        }
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
        TideHelper.mCacheImpl.addNum(false)
        showSuccessAd()
        p0?.let {
            postAdEvent(it)
            runCatching {
                AppEventsLogger.newLogger(context).logPurchase(
                    (it.ecpm.toDouble() / 1000).toBigDecimal(), Currency.getInstance("USD")
                )
            }
        }
    }

    override fun onAdClicked(p0: TPAdInfo?) {
        TideHelper.mCacheImpl.addNum(true)
    }

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

    private var dayIndex by LakeIntImpl(-1)
    private var numShow by LakeIntImpl(0)
    private var num24 = 1000 * 60 * 60 * 24
    private val mapName = hashMapOf(
        10 to "lbv4w0", 20 to "xx0cte", 30 to "fv2aez", 40 to "zbp5qs", 50 to "hwtftw"
    )

    private fun showSuccessAd() {
        val dayNum = (System.currentTimeMillis() - TideHelper.mCacheImpl.mInstallTime) / num24
        TideHelper.log("showSuccessAd--->$dayNum ---$numShow")
        if (dayNum > dayIndex) {
            dayIndex = num24
            numShow = 1
        } else {
            numShow++
        }
        val name = mapName[numShow] ?: ""
        if (name.isNotBlank()) {
            TideHelper.log("trackEvent--->$name")
            setAdjust(name)
        }
    }


    private fun setAdjust(name: String) {
        val adjust = AdjustEvent(name)
        Adjust.trackEvent(adjust)
    }

}