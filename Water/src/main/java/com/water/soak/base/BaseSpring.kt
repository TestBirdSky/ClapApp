package com.water.soak.base

import android.app.Service
import android.app.job.JobService
import android.content.Intent
import android.os.IBinder


/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseSpring : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }
}