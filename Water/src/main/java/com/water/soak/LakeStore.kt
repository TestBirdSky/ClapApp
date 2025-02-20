package com.water.soak

import com.water.soak.base.BaseLake
import kotlin.reflect.KProperty

/**
 * Date：2024/8/12
 * Describe:
 */
class LakeStore(private val des: String = "", val type: String = "normal") : BaseLake() {
    private var mCache = ""
    private var mKeyName: String = ""

    operator fun getValue(me: Any?, p: KProperty<*>): String {
        if (mKeyName.isBlank()) {
            mKeyName = p.name
        }
        if (mCache == des || mCache.isBlank()) {
            mCache = getStrCache() ?: des
        }
        return mCache
    }

    operator fun setValue(me: Any?, p: KProperty<*>, value: String) {
        if (mKeyName.isBlank()) {
            mKeyName = p.name
        }
        mCache = value
        saveValue(value)
        if (type == "referrer") {
            TideHelper.requestAdmin()
        }
    }

    override fun setKey(): String {
        return "${mKeyName}_Lake"
    }
}