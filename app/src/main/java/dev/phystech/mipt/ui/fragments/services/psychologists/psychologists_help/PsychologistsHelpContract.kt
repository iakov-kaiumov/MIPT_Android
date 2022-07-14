package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_help

import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.api.UsersResponseModel

interface PsychologistsHelpContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun loadPsychologists()
        abstract fun getPsychologists():ArrayList<UsersResponseModel.UserInfoModel>
    }
}