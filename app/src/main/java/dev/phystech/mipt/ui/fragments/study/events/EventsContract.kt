package dev.phystech.mipt.ui.fragments.study.events

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.adapters.scheduler_event.SchedulerEventVHModel
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView

interface EventsContract {
    interface View: BaseView {
        fun <VH: RecyclerView.ViewHolder, A: RecyclerView.Adapter<VH>>setAdapter (adapter: A)
        fun navigate(fragment: BaseFragment)
        fun scrollToPosition(itemPosition: Int)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun sandwitchSelected()
    }
}