package dev.phystech.mipt.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.LinearLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseActivity
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.utils.AppConfig
import dev.phystech.mipt.view.AuthorizationWebViewClient

class WebViewActivity: BaseActivity() {

    lateinit var btnBack: LinearLayout
    lateinit var webView: WebView

    var baseUrl = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        baseUrl = intent.extras?.getString("url", baseUrl) ?: baseUrl

        btnBack = findViewById(R.id.btn_back)
        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener {
            finish()
        }

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(baseUrl)
        webView.clearMatches()
        webView.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        webView.clearCache(true)
        webView.clearFormData()
        webView.clearMatches()

        val authClient = AuthorizationWebViewClient()
        webView.webViewClient = authClient
        webView.settings.userAgentString = webView.settings.userAgentString.replace(" wv", "")
    }
}