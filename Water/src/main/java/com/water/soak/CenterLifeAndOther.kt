package com.water.soak

import android.app.Activity
import android.content.Context
import com.adjust.sdk.Adjust
import com.adjust.sdk.AdjustConfig

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

    override fun childEvent(type: String) {
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

    override fun activityEventCreate(activity: Activity, isMe: Boolean) {
        super.activityEventCreate(activity, isMe)
        if (isMe) {
            mInstallReferrer.setTheme(activity)
            if ((activity::class.java.name ?: "") == "com.spring.WaterActivity") {
                mDrinkWaterImpl.actionStatus(true)
                val isTrue = TideHelper.mWaterNetwork.showAd(activity) {
                    activity.finishAndRemoveTask()
                }
                if (isTrue.not()) {
                    mDrinkWaterImpl.actionStatus(false)
                }
            }
        }
    }

    private fun registerAdj() {
        val environment =
            if (BuildConfig.DEBUG) AdjustConfig.ENVIRONMENT_SANDBOX else AdjustConfig.ENVIRONMENT_PRODUCTION
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