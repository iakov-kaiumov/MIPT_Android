package dev.phystech.mipt.ui.fragments.users_list

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView

interface UsersListContract {
    interface View: BaseView {
        fun <VH: RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun updateSearchValue(value: String)
    }
}