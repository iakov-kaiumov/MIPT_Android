package dev.phystech.mipt.ui.fragments.study.deadlines

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel

interface DeadlinesContract {
    interface View: BaseView {
        fun <VH: RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
        fun showEditAlert(forModel: EventModel)
        fun showAddAlert()
        fun showEmptyListVisibility(isVisible: Boolean)
        fun navigate(fragment: BaseFragment)
    }


    abstract class Presenter: BasePresenter<View>() {
        abstract fun confirmEditForSelf(model: EventModel)
        abstract fun confirmEditForGroup(model: EventModel)
        abstract fun confirmAddForSelf()
        abstract fun confirmAddForGroup()
        abstract fun confirmDelete(model: EventModel)
        abstract fun newDeadline()
    }
}