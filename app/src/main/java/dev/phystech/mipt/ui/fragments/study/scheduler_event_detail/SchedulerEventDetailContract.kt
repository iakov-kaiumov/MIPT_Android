package dev.phystech.mipt.ui.fragments.study.scheduler_event_detail

import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel

interface SchedulerEventDetailContract {
    interface View: BaseView {
        fun setContent(model: EventModel)
        fun showEditAlert(canEdit: Boolean)
        fun sendLink(link: String)
        fun navigate(fragment: BaseFragment)
        fun back()
        fun toNews()
        fun showStudy()
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun sharePressed()
        abstract fun editPressed()
        abstract fun locationPressed()
        abstract fun lectorPressed()
        abstract fun translationPressed()
        abstract fun confirmEditPersonal()
        abstract fun confirmEditCommon()
        abstract fun confirmDelete()
        abstract fun updateNote(value: String)
        abstract fun usersClick()
    }
}