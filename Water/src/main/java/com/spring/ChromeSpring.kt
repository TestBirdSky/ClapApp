package com.spring

import android.webkit.WebChromeClient
import android.webkit.WebView
import com.water.soak.SteamHelper

/**
 * Date：2025/2/18
 * Describe:
 */
class ChromeSpring : WebChromeClient() {
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (newProgress == 100) {
            SteamHelper.soakSeek(newProgress)
        }
    }
}