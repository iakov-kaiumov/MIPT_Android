package dev.phystech.mipt.ui.fragments.study.guest

import dev.phystech.mipt.adapters.SchedulersFilesAdapter
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import java.io.File

interface SchedulersGuestContract {
    interface View: BaseView {
        fun setAdapter(adapter: SchedulersFilesAdapter)
        fun showFile(file: File)
        fun showFile(file: String)
        fun checkIOPermission(requestIfNeeded: Boolean = true): Boolean
    }

    abstract class Presenter: BasePresenter<View>() {
    }

}