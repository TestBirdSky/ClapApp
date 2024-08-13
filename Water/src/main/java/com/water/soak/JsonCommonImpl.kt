package com.water.soak

import android.os.Build
import org.json.JSONObject
import java.util.UUID

/**
 * Date：2024/8/13
 * Describe:
 */
class JsonCommonImpl(private val pkgName: String) {
    val urlPost =
        if (BuildConfig.DEBUG) "https://test-cassius.appbuilderclap.com/pronoun/dross/presume"
        else "https://cassius.appbuilderclap.com/heptane/citron/malton"

    // admin url
    val urlA = "https://dirtyeditor.appbuilderclap.com/api/ss/"

    fun getReferrerJson(ref: String): JSONObject {
        val js = getCommonJson().apply {
            put("gaffe", JSONObject().apply {
                put("deferent", "build/${Build.ID}")
                put("wispy", ref)
                put("glorify", "")
                put("shortage", "benedikt")
                put("murky", 0L)
                put("bullhide", 0L)
                put("save", 0L)
                put("spite", 0L)
                put("blush", System.currentTimeMillis())
                put("penrose", 0L)
            })
        }
        return js
    }

    fun getCommonJson(): JSONObject {
        return JSONObject().apply {
            put("guilt", JSONObject().apply {
                put("nw", TideHelper.mCacheImpl.mAndroidIdWater)
                put("bistable", false)
                put("pion", "")
                put("king", "_")
                put("tarpaper", Build.VERSION.RELEASE)
                put("bland", "")
                put("goucher", "cement")
            })
            put("sparrow", JSONObject().apply {
                put("tito", "")
                put("buffet", "")
                put("gnat", System.currentTimeMillis())
                put("imagery", TideHelper.mCacheImpl.mVersionName)
                put("impend", "")
                put("bechtel", pkgName)
                put("athletic", UUID.randomUUID().toString())
            })
        }
    }

    fun getAdminBodyJson(): JSONObject {
        return JSONObject().apply {
            put("oXnMbA", "com.appbuilder.clap.to.find.my.app")
            put("QMGqjJrFSa", TideHelper.mCacheImpl.mAndroidIdWater)
            put("CVJBnjn", TideHelper.mCacheImpl.mReferrerStr)
            put("FUAPZluX", TideHelper.mCacheImpl.mVersionName)
            put("sUeARbSP", "")
            put("GObwcdok", TideHelper.mCacheImpl.mAndroidIdWater)
        }
    }
}