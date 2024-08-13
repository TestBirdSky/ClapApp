package com.water.soak.base

import com.tencent.mmkv.MMKV
import com.water.soak.TideHelper

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseLake {
    abstract fun setKey(): String

    protected fun saveValue(value: String) {
        TideHelper.mmkv.encode(setKey(), value)
    }

    protected fun getStrCache(): String? {
        return TideHelper.mmkv.decodeString(setKey())
    }

}