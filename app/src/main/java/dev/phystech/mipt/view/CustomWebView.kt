package dev.phystech.mipt.view

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient

class AuthorizationWebViewClient: WebViewClient() {

    interface Delegate {
        fun onError()
        fun onSuccess(code: String)
    }

    var delegate: Delegate? = null


    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        val uri = Uri.parse(url)

        if (url?.contains("localhost") != true) {
            super.onPageStarted(view, url, favicon)
            return
        }

        val error = uri.getQueryParameter("authError")
        val code = uri.getQueryParameter("code")

        if (error.isNullOrEmpty() && !code.isNullOrEmpty()) {
            delegate?.onSuccess(code)
        } else {
            delegate?.onError()
        }


    }


}