package dev.phystech.mipt.ui.fragments.support.blocking_users

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView

interface BlockingUsersContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun loadBlockingUsers()
        abstract fun unblockUser(id: String)
    }
}