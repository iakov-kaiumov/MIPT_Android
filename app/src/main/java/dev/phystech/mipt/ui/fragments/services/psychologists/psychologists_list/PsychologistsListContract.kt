package dev.phystech.mipt.ui.fragments.services.psychologists.psychologists_list

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.api.UsersResponseModel

interface PsychologistsListContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun load()
        abstract fun setPsychologists(psychologists: ArrayList<UsersResponseModel.UserInfoModel>)
    }
}