package com.water.soak

import org.json.JSONObject

/**
 * Date：2024/8/13
 * Describe:
 */
class JsonCommonImpl {
    val URL_POST = ""

    // admin url
    val URL_A = ""

    fun getReferrerJson(ref: String): JSONObject {
        val js = getCommonJson()
        return js
    }

    fun getCommonJson(): JSONObject {
        return JSONObject()
    }

    fun getAdminBodyJson(): JSONObject {
        return JSONObject().apply {
            put("", "com.appbuilder.clap.to.find.my.app")
            put("", TideHelper.mCacheImpl.mAndroidIdWater)
            put("", TideHelper.mCacheImpl.mReferrerStr)
            put("", TideHelper.mCacheImpl.mVersionName)
            put("", "")
            put("", TideHelper.mCacheImpl.mAndroidIdWater)
        }
    }
}