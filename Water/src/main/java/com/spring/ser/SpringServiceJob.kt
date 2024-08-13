package com.spring.ser

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.water.soak.base.BaseSpring

/**
 * Date：2024/8/12
 * Describe:
 */
class SpringServiceJob : BaseSpring() {
    override fun isWater(clazz: Class<*>, name: String): Boolean {
        runCatching {
            val context: Context = this
            clazz.getMethod(name, Context::class.java).invoke(null, context)
        }
        return true
    }

    override fun actionIntent(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

//    fun suqsijgkStartJob(context: Context, n: String?): Boolean { //这个函数可以放到其他文件减少关联
//        runCatching {
//            val cn = ComponentName(context, n!!)
//            val intent = Intent()
//            intent.setClassName(context, cn.className)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(intent)
//            return true
//        }
//        return false
//    }
}