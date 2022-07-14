package dev.phystech.mipt.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.phystech.mipt.R
import dev.phystech.mipt.base.BaseFragment

class ExampleFragment: BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_what_news, container, false)
    }

    override fun bindView(view: View?) {

    }
}