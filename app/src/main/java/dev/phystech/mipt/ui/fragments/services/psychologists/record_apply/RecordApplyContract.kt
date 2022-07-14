package dev.phystech.mipt.ui.fragments.services.psychologists.record_apply

import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel

interface RecordApplyContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun creatingEventInSchedule()
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun recordApply(id: String, phone: String)
        abstract fun createSchedule(eventModel: EventModel)
    }
}