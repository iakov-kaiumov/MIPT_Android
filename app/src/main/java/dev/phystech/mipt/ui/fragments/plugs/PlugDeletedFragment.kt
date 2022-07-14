package dev.phystech.mipt.ui.fragments.plugs

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.fragments.services.feedback.FeedbackFragment

class PlugDeletedFragment: BaseFragment() {

    lateinit var rlToSupport: RelativeLayout
    lateinit var tvDescription: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plug_deleted, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        tvDescription = view.findViewById(R.id.tvDescription)
        rlToSupport = view.findViewById(R.id.rlToSupport)


        tvDescription.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://pk.mipt.ru/perevodvoss/"))
            startActivity(intent)
        }
        rlToSupport.setOnClickListener {
            val fragment = FeedbackFragment()
            navigationPresenter.pushFragment(fragment, true)
        }
    }
}
