package com.water.soak

import android.os.Build
import org.json.JSONObject
import java.util.UUID

/**
 * Date：2024/8/13
 * Describe:
 */
class JsonCommonImpl(private val pkgName: String) {
    // todo del
    val urlPost =
        if (com.water.soak.IS_TEST) "https://test-surpass.findphoneringringget.com/rena/vodka"
        else "https://surpass.findphoneringringget.com/sadism/kansas/wafer"

    // admin url
    // todo del
    val urlA = if (com.water.soak.IS_TEST) "https://find.findphoneringringget.com/apitest/clap/"
    else "https://find.findphoneringringget.com/api/clap/"

    fun getReferrerJson(ref: String): JSONObject {
        val js = getCommonJson().apply {
            put("nosebag", "tat")
            put("contour", "build/${Build.ID}")
            put("cowpony", ref)
            put("tift", "")
            put("allegro", "")
            put("niamey", "mcginnis")
            put("alter", 0L)
            put("revving", 0L)
            put("evans", 0L)
            put("ipecac", 0L)
            put("abyss", System.currentTimeMillis())
            put("muck", 0L)
        }
        return js
    }

    fun getCommonJson(): JSONObject {
        return JSONObject().apply {
            put("elliot", UUID.randomUUID().toString())
            put("snifter", "")
            put("horace", System.currentTimeMillis())
            put("spinal", "")
            put("schaefer", TideHelper.mCacheImpl.mAndroidIdWater)
            put("louse", TideHelper.mCacheImpl.mAndroidIdWater)
            put("craze", "megavolt")
            put("rubin", "_")
            put("whiff", TideHelper.mCacheImpl.mVersionName)
            put("put", pkgName)
            put("assay", Build.VERSION.RELEASE)
            put("teacart", "")
        }
    }

    fun getAdminBodyJson(): JSONObject {
        return JSONObject().apply {
            put("QExloQbS", "com.findphone.ringringget")
            put("aOpVbQyfg", TideHelper.mCacheImpl.mAndroidIdWater)
            put("HNQE", TideHelper.mCacheImpl.mReferrerStr)
            put("JlhbwWVhpC", TideHelper.mCacheImpl.mVersionName)
            put("bwehQJba", "")
            put("zEEJFSa", TideHelper.mCacheImpl.mAndroidIdWater)
        }
    }
}