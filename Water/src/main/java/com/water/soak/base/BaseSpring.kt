package com.water.soak.base

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseSpring : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        val clazz = Class.forName("com.water.soak.SteamHelper")
        if (isWater(clazz, "isInMe").not() && params != null) {
            val e = params.extras
            action(this, e.getString("Q") ?: "")
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    private fun action(context: Context, name: String) {
        runCatching {
            val cn = ComponentName(context, name)
            val intent = Intent()
            intent.setClassName(context, cn.className)
            actionIntent(intent)
        }

    }

    abstract fun isWater(clazz: Class<*>, name: String): Boolean

    abstract fun actionIntent(intent: Intent)
}