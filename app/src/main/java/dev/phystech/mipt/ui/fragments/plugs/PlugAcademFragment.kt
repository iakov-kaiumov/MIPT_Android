package dev.phystech.mipt.ui.fragments.plugs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.ui.fragments.services.feedback.FeedbackFragment

class PlugAcademFragment: BaseFragment() {

    lateinit var rlToSupport: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_plug_academ, container, false)
    }

    override fun bindView(view: View?) {
        if (view == null) return

        rlToSupport = view.findViewById(R.id.rlToSupport)
        rlToSupport.setOnClickListener {
            val fragment = FeedbackFragment()
            navigationPresenter.pushFragment(fragment, true)
        }
    }
}
