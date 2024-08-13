package com.water.soak

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class ReservoirLifeActivity : Application.ActivityLifecycleCallbacks {
    private var num = 0

    abstract fun isCanAllow(): Boolean

    abstract fun childEvent(type: String)

    abstract fun listActivity(): ArrayList<Activity>

    open fun activityEventCreate(activity: Activity, isMe: Boolean) {

    }

    open fun inAppStatus(isInApp: Boolean) {

    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        TideHelper.log("onActivityCreated--->$activity")
        listActivity().add(activity)
        activityEventCreate(activity, isCanAllow())
    }

    override fun onActivityStarted(activity: Activity) {
        num++
        inAppStatus(true)
    }

    override fun onActivityResumed(activity: Activity) {
        childEvent("resume")
    }

    override fun onActivityPaused(activity: Activity) {
        childEvent("pause")
    }

    override fun onActivityStopped(activity: Activity) {
        num--
        if (num <= 0) {
            num = 0
            inAppStatus(false)
            if (isCanAllow()) {
                ArrayList(listActivity()).forEach {
                    it.finishAndRemoveTask()
                }
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        TideHelper.log("onActivityDestroyed--->$activity")
        listActivity().remove(activity)
    }
}