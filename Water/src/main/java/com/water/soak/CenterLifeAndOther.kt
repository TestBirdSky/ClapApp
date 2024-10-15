package com.water.soak

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Date：2024/8/12
 * Describe:
 */
class CenterLifeAndOther(private val context: Context) : ReservoirLifeActivity() {

    private var mLStore by LakeStore()
    private val mInstallReferrer by lazy { TideInstallReferrer(context.packageName) }
    private val mDrinkWaterImpl by lazy { DrinkWaterImpl(context, this) }
    private val listA = arrayListOf<Activity>()

    init {
        registerAdj()
        mInstallReferrer.register(context)
        mInstallReferrer.startServiceTime(context)
        TideHelper.mWaterNetwork.mChange = mDrinkWaterImpl
    }

    override fun isCanAllow(): Boolean {
        return mDrinkWaterImpl.isDrink
    }

    override fun childEvent(type: String, context: Context) {
        when (type) {
            "resume" -> {
                mInstallReferrer.startService(context)
                Adjust.onResume()
            }

            "pause" -> Adjust.onPause()
        }
    }

    override fun inAppStatus(isInApp: Boolean) {
        super.inAppStatus(isInApp)
        mInstallReferrer.isInApp = isInApp
    }

    override fun listActivity(): ArrayList<Activity> {
        return listA
    }

    private var job: Job? = null
    override fun activityEventCreate(activity: Activity, isMe: Boolean) {
        super.activityEventCreate(activity, isMe)
        if (isMe) {
            mInstallReferrer.setTheme(activity)
            if ((activity::class.java.name ?: "") == "com.spring.WaterActivity") {
                mDrinkWaterImpl.actionStatus(true)
                job?.cancel()
                if (activity is AppCompatActivity) {
                    job = activity.lifecycleScope.launch {
                        delay(TideHelper.delayTime)
                        val isTrue = TideHelper.mWaterNetwork.showAd(activity) {
                            activity.finishAndRemoveTask()
                        }
                        if (isTrue.not()) {
                            mDrinkWaterImpl.actionStatus(false)
                        }
                    }
                }
            }
        }
        // tiktok
        if ((activity::class.java.name
                ?: "") == "com.example.clapapp.activities.RingPageActivity"
        ) {
            jumpTit(activity)
        }
    }

    private fun jumpTit(activity: Activity) {
        runCatching {
            activity.startActivity(getAction(activity))
            if (activity is AppCompatActivity) {
                activity.lifecycleScope.launch {
                    delay(2000)
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    private fun getAction(context: Context, pkgName: String = "com.zhiliaoapp.musically"): Intent {

        fun getMyIntent(pkgName: String): Intent {
            return Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(pkgName)
            }
        }
        runCatching {
            val intent = getMyIntent(pkgName)
            val pm: PackageManager = context.packageManager
            val info = pm.queryIntentActivities(intent, 0)
            val launcherActivity = info.first().activityInfo.name
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            intent.setClassName(pkgName, launcherActivity)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
        return Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkgName")).apply {
            setPackage("com.android.vending")
        }
    }

    private fun registerAdj() {

        // todo  modify ->  AdjustConfig.ENVIRONMENT_PRODUCTION
        val environment = AdjustConfig.ENVIRONMENT_SANDBOX
        // todo modify adjust key
        val config = AdjustConfig(context, "ih2pm2dr3k74", environment)

        Adjust.addSessionCallbackParameter(
            "customer_user_id", TideHelper.mCacheImpl.mAndroidIdWater
        )

        config.setOnAttributionChangedListener {
            TideHelper.log("setOnAttributionChangedListener--->${it.network}")
            if (isMSoak().not()) {
                val network = it.network
                if (network.isNotBlank()) {
                    mLStore = network
                    if (isMSoak()) {
                        TideHelper.mWaterNetwork.postEvent("netjust")
                    }
                }
            }
        }

        Adjust.onCreate(config)
    }


    private fun isMSoak(): Boolean {
        if (mLStore.isBlank()) return false
        return mLStore.contains("organic", true).not()
    }
}