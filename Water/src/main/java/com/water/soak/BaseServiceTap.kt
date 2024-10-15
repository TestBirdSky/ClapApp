package com.water.soak

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseServiceTap : Service() {
    private val versionStr = arrayListOf("33", "26")
    private val list = arrayListOf("", "Notification", "Notification Channel")

    abstract fun createNotification(title: String, channelId: String): Notification

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching {
            val notification = createNotification(list[0], list[1])
            startForeground(1902, notification)
        }
        return START_STICKY
    }

    @SuppressLint("NewApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= versionStr[1].toInt()) {
            val channel =
                NotificationChannel("Notification", list[2], NotificationManager.IMPORTANCE_DEFAULT)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
        runCatching {
            if (Build.VERSION.SDK_INT <= versionStr[0].toInt()) {
                val notification = createNotification(list[0], list[1])
                startForeground(1902, notification)
            }
        }
        TideHelper.isShowService = true
    }

    override fun onDestroy() {
        super.onDestroy()
        TideHelper.isShowService = false
    }
}