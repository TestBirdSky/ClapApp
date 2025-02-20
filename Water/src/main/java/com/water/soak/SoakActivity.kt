package com.water.soak

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.water.soak.databinding.ClapLakeActivityBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Date：2025/2/20
 * Describe:
 * com.water.soak.SoakActivity
 */
class SoakActivity : AppCompatActivity() {
    private var isSuccess = false
    private var index = 3
    private val listLoading =
        arrayListOf("Ads Loading ...", "Ads Loading ..", "Ads Loading .", "Ads Loading ")

    private val binding: ClapLakeActivityBinding by lazy {
        ClapLakeActivityBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.ivClose.setOnClickListener {
            TideHelper.mWaterNetwork.postEvent("closebrowser")
            finishAndRemoveTask()
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                TideHelper.log("onProgressChanged--->$newProgress")
                if (newProgress > 76) {
                    isSuccess = true
                }
            }
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                TideHelper.log("onPageFinished--->$url")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                TideHelper.log("onPageStarted--->$url")
            }
        }

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.userAgentString = "/${TideHelper.mWaterNetwork.stringNameH5}"
        }
        binding.webView.loadUrl(TideHelper.mWaterNetwork.stringH5Url)
        lifecycleScope.launch {
            withTimeoutOrNull("3001".toLong()) {
                while (isSuccess.not()) {
                    if (index < 0) {
                        index = 3
                    }
                    binding.tvLoading.text = listLoading[index]
                    index--
                    delay(400)
                }
            }
            binding.layoutLoading.visibility = View.GONE
            binding.ivClose.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}