package com.water.soak

import android.os.Handler
import android.os.Message

/**
 * Date：2025/2/18
 * Describe:
 */
abstract class BaseHandler : Handler() {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        SteamHelper.soakSeek(msg.what)
    }
}