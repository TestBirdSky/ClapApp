package com.example.clapapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.ads.MobileAds
import com.water.soak.SteamHelper

var isactive: Boolean = false
val key = "done"

var ispurchased: Boolean = false
val key1 = "my_imApp_Purchase_key"
var count : Int = 0

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//
//        MobileAds.initialize(this)
//        AppOpenManager(this)

        val key = "done"
        val defaultValue = false

        val key1 = "my_imApp_Purchase_key"
        val defaultValue1 = false

        ispurchased = getBooleanFromSharedPreferences1(this, key1, defaultValue1)
        isactive = getBooleanFromSharedPreferences(this, key, defaultValue)

        // Create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "my_channel_id",
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
        SteamHelper.init(this)
    }

    fun getBooleanFromSharedPreferences1(
        context: Context,
        key: String,
        defaultValue: Boolean
    ): Boolean {
        val sharedPreferences = context.getSharedPreferences("MyAppPurchase", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getBooleanFromSharedPreferences(
        context: Context,
        key: String,
        defaultValue: Boolean
    ): Boolean {
        val sharedPreferences = context.getSharedPreferences("MySelection", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}