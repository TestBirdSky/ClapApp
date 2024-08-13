package com.water.soak.base

import android.util.Base64
import com.water.soak.TideHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Date：2024/8/12
 * Describe:
 */
abstract class BaseSoakNetwork {
    val mScopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val okHttpClient = OkHttpClient()
    private val strEventName = "isuser-jumpfail-netjust"
    private var headerTime = "0"

    open fun refreshData(string: String): String {
        val length = headerTime.length
        val ss = string.mapIndexed { index, c ->
            (c.code xor headerTime[index % length].code).toChar()
        }.joinToString("")
        String(Base64.decode(string, Base64.DEFAULT))
        return ""
    }

    protected fun getPostNum(name: String): Int {
        return if (strEventName.contains(name)) {
            30
        } else {
            0
        }
    }

    fun postNet(
        request: Request,
        num: Int,
        failed: (() -> Unit)? = null,
        success: ((res: String) -> Unit)? = null
    ) {
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (num > 0) {
                    mScopeIO.launch {
                        delay(15000)
                        postNet(request, num - 1, failed, success)
                    }
                } else {
                    failed?.invoke()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""
                TideHelper.log("body--->$body")
                if (response.isSuccessful && response.code == 200) {
                    headerTime = response.headers["dt"] ?: ""
                    success?.invoke(body)
                } else {
                    if (num > 0) {
                        mScopeIO.launch {
                            delay(15000)
                            postNet(request, num - 1, failed, success)
                        }
                    } else {
                        failed?.invoke()
                    }
                }
            }
        })
    }


}