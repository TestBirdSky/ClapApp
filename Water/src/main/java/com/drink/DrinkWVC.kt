package com.drink

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.Keep
import com.water.soak.TideHelper

/**
 * Date：2025/2/18
 * Describe:
 */

class DrinkWVC : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        // todo del
        TideHelper.log("onPageStarted--$url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        TideHelper.log("onPageFinished--$url")
    }
}