package dev.phystech.mipt.ui.fragments.services.psychologists

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment

class UrgentHelpFragment : BaseFragment() {

    lateinit var ivBack: ImageView
    lateinit var webViewUrgentHelp: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_urgent_help, container, false)
    }

    //  MVP VIEW
    override fun bindView(view: View?) {
        if (view == null) return

        ivBack = view.findViewById(R.id.ivBack)

        webViewUrgentHelp = view.findViewById(R.id.webViewUrgentHelp)
        webViewUrgentHelp.getSettings().setJavaScriptEnabled(true);
        webViewUrgentHelp.loadUrl("file:///android_asset/urgent_help.html");

        //  EVENTS

        ivBack.setOnClickListener(this::onBack)
    }

    override fun getBackgroundColor(): Int {
        return resources.getColor(R.color.color_pressed_button, null)
    }

    //  OTHERS

    fun onBack(view: View) {
        navigationPresenter.popFragment()
    }

}