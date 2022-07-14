package dev.phystech.mipt.ui.fragments.study.scheduler_edit

import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.models.SportSectionModel
import dev.phystech.mipt.models.api.SportSectionResponseModel

interface SchedulerEditContract {
    interface View: BaseView {
        fun setCourses(value: ArrayList<String>)
        fun setAuditories(value: List<String>)
        fun setLectors(value: List<String>)
        fun setDayOfWeeks(value: List<String>)
        fun setBeginTimes(value: List<String>)
        fun setEndTimes(value: List<String>)
        fun setRepeatValues(value: List<String>)
        fun setCategories(value: List<SportSectionModel>)
        fun setTypesOfScheduler(value: ArrayList<SchedulerType>)
        fun setLinkAdapter(adapter: LinksAdapter)
        fun back()

        fun setSchedulerName(value: String?)
        fun setAuitoryName(value: String?)
        fun setTeacherName(value: String?)
        fun showCallbackMessage(message: String, callback: (() -> Unit)? = null)
        fun navigate(fragment: BaseFragment)
        fun sendSchedulerEditedBroadcast(newId: Int?)
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun saveClick()
        abstract fun cleatChangesClick()
        abstract fun deleteClick()
        abstract fun backClick()
        abstract fun addLinkClick()
        abstract fun editClick(model: ScheduleItem, forGroup: Boolean)
        abstract fun saveClick(model: ScheduleItem, forGroup: Boolean)
    }
}