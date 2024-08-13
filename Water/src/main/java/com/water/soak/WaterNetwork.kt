package com.water.soak

import android.app.Activity
import android.content.Context
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.tradplus.ads.base.bean.TPAdError
import com.tradplus.ads.base.bean.TPAdInfo
import com.tradplus.ads.open.interstitial.InterstitialAdListener
import com.tradplus.ads.open.interstitial.TPInterstitial
import com.water.soak.base.BaseSoakNetwork
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Date：2024/8/13
 * Describe:
 */
class WaterNetwork : BaseSoakNetwork(), InterstitialAdListener {
    lateinit var context: Context
    private var lastAdSaveTime = 0L
    private var isLoading = false
    private var lastRequestTime = 0L
    private var mTPInterstitial: TPInterstitial? = null
    private val second = 1000L
    private var timeCheck = 40 * second
    private var timeWait = 60 * second
    private var timePeriod = 50 * second

    //不上报日志(snow) 不外弹(water)
    var status = ""

    // 广告id
    var idSnow = ""

    private val mJsonCommonImpl = JsonCommonImpl()

    fun postAdmin() {
        val time = "${System.currentTimeMillis()}"
        val length = time.length
        val body = mJsonCommonImpl.getAdminBodyJson().toString()
        val bodyEncoder = body.mapIndexed { index, c ->
            (c.code xor time[index % length].code).toChar()
        }.joinToString("")
        val request = TideHelper.toRequestInfo(
            Base64.encodeToString(bodyEncoder.toByteArray(), Base64.DEFAULT),
            mJsonCommonImpl.URL_A,
            header = mapOf("dt" to time)
        )
        postNet(request, 15, failed = {
            postAdmin()
        }, success = {

        })

    }

    override fun refreshData(string: String): String {
        super.refreshData(string)

    }

    fun postEvent(name: String, pair: Pair<String, String>? = null) {
        val num = getPostNum(name)
        if (status.contains("snow") && num == 0) {
            TideHelper.log("cancel post event $name ")
            return
        }
        TideHelper.log("post event $name $pair")
        val js = getEventJs(name, pair)
        val request = TideHelper.toRequestInfo(js, mJsonCommonImpl.URL_POST)
        postNet(request, num)
    }

    private fun getEventJs(name: String, pair: Pair<String, String>? = null): JSONObject {
        val js = mJsonCommonImpl.getCommonJson()
        return js
    }

    private var closeInvoke: (() -> Unit)? = null

    fun showAd(activity: Activity, close: () -> Unit): Boolean {
        val ad = mTPInterstitial ?: return false
        if (isReadyAd()) {
            closeInvoke = {
                close.invoke()
            }
            ad.showAd(activity, "")
            return true
        }
        return false
    }


    fun loadAd() {
        val str = load()
        TideHelper.log("loadAd-->$str")
    }

    private fun load(): String {
        if (idSnow.isBlank()) {
            return "ad is null"
        }
        if (isLoading && System.currentTimeMillis() - lastRequestTime < 60000) {
            return "ad is loading"
        }
        if (isReadyAd()) {
            return "ad is ready"
        }
        isLoading = true
        lastRequestTime = System.currentTimeMillis()
        lastAdSaveTime = System.currentTimeMillis()
        if (mTPInterstitial == null) {
            mTPInterstitial = TPInterstitial(context, idSnow)
        }
        mTPInterstitial?.loadAd()
        mTPInterstitial?.setAdListener(this)
        postEvent("reqprogress")
        return "ad load"
    }

    fun isReadyAd(): Boolean {
        val ad = mTPInterstitial ?: return false
        if (System.currentTimeMillis() - lastAdSaveTime > 55 * 60000) return false
        return ad.isReady
    }


    override fun onAdLoaded(p0: TPAdInfo?) {
        postEvent("getprogress")
        isLoading = false
        lastAdSaveTime = System.currentTimeMillis()
    }

    override fun onAdFailed(p0: TPAdError?) {
        postEvent("showfailer", Pair("string", "error_${p0?.errorCode}_${p0?.errorMsg}"))
        mScopeIO.launch {
            delay(16000)
            isLoading = false
            load()
        }
    }

    override fun onAdImpression(p0: TPAdInfo?) {
        postEvent("showsuccess")
    }

    override fun onAdClicked(p0: TPAdInfo?) {

    }

    override fun onAdClosed(p0: TPAdInfo?) {
        closeInvoke?.invoke()
        closeInvoke = null
    }

    override fun onAdVideoError(p0: TPAdInfo?, p1: TPAdError?) {
        closeInvoke?.invoke()
        closeInvoke = null
    }

    override fun onAdVideoStart(p0: TPAdInfo?) {

    }

    override fun onAdVideoEnd(p0: TPAdInfo?) {

    }

}