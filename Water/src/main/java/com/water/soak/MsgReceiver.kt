package com.water.soak

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Date：2024/8/12
 * Describe:
 */
class MsgReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent == null) {
                context.unregisterReceiver(this)
            } else {
                runCatching {
                    if (intent.hasExtra("Bc")) {
                        val intent1 = intent.getParcelableExtra("Bc") as Intent?
                        if (intent1 != null) {
                            context.startActivity(intent1)
                        }
                    }
                }
            }
        }
    }
}