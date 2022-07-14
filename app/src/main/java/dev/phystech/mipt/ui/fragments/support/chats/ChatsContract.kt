package dev.phystech.mipt.ui.fragments.support.chats

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView

interface ChatsContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
//        fun setContent(model: AddEventFieldsViewModel)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun load()
//        abstract val receiver: BroadcastReceiver
    }
}