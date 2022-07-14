package dev.phystech.mipt.ui.activities.auth_webview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseActivity
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.utils.AppConfig
import dev.phystech.mipt.view.AuthorizationWebViewClient

class AuthorizationWebViewActivity: BaseActivity(), AuthorizationWebViewClient.Delegate {

    lateinit var webView: WebView
//    val USER_AGENT_FAKE = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"

    var baseUrl = AppConfig.authUrl

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        baseUrl = intent.extras?.getString("url", baseUrl) ?: baseUrl

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
        authClient.delegate = this
        webView.webViewClient = authClient
//        webView.settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; JSN-L21 Build/HONORJSN-L21; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/92.0.4515.166 Mobile Safari/537.36"
        webView.settings.userAgentString = webView.settings.userAgentString.replace(" wv", "")
        //  Mozilla/5.0 (Linux; Android 10; JSN-L21 Build/HONORJSN-L21; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/92.0.4515.166 Mobile Safari/537.36

    }


    //  Auth client delegate
    override fun onError() {
        setResult(RESULT_ERROR)
//        finish()
    }

    override fun onSuccess(code: String) {
        UserRepository.shared.setCode(code)

        val resultIntent = Intent()
        intent.extras?.let { resultIntent.putExtras(it) }

        setResult(RESULT_SUCCESS, resultIntent)
        finish()
    }

    companion object {
        const val RESULT_SUCCESS = 100
        const val RESULT_ERROR = 101
    }
}