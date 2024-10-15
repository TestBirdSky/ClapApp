package com.water.soak

import kotlin.reflect.KProperty

/**
 * Date：2024/10/14
 * Describe:
 */
class LakeIntImpl(val def: Int = 0) {
    operator fun getValue(me: Any?, p: KProperty<*>): Int {
        return TideHelper.mmkv.decodeInt(p.name, def)
    }

    operator fun setValue(me: Any?, p: KProperty<*>, value: Int) {
        TideHelper.mmkv.encode(p.name, value)
    }
}