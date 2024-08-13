package com.ice.snow

import android.app.Notification
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.water.soak.BaseServiceTap
import com.water.soak.R

/**
 * Date：2024/8/12
 * Describe:
 */
class IceService : BaseServiceTap() {

    override fun createNotification(title: String, channelId: String): Notification {
        return NotificationCompat.Builder(this, channelId).setAutoCancel(false)
            .setContentText(getString(R.string.ice_service_context))
            .setSmallIcon(R.drawable.ic_ice_snow).setOngoing(true).setOnlyAlertOnce(true)
            .setContentTitle(title)
            .setCustomContentView(RemoteViews(packageName, R.layout.layout_notification_tips))
            .build()
    }
}