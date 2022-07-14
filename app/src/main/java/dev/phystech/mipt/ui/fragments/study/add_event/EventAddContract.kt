package dev.phystech.mipt.ui.fragments.study.add_event

import android.content.BroadcastReceiver
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.models.add_event_view.AddEventFieldsViewModel
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditFragment
import java.util.ArrayList

interface EventAddContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)

        fun setContent(model: AddEventFieldsViewModel)

        fun setNameError(@StringRes valueId: Int)
        fun setAuditoryError(@StringRes valueId: Int)
        fun setTeacherError(@StringRes valueId: Int)
        fun setScheduleError(@StringRes valueId: Int)
        fun setDateError(@StringRes valueId: Int)
        fun setTimeBeginError(@StringRes valueId: Int)
        fun setTimeEndError(@StringRes valueId: Int)
        fun setTypesError(@StringRes valueId: Int)

        fun setDeadlinesVisibilist(isVisible: Boolean)
        fun updateTimeSliceVisibility(isChecked: Boolean)

        fun setTypes(types: List<SchedulerType>)
        fun <VH : RecyclerView.ViewHolder>setLinksAdapter(adapter: RecyclerView.Adapter<VH>)

        fun showTeacherList()
        fun showAuditoryList()
        fun showScheduleList()
        fun back()

        fun buildModel(): AddEventFieldsViewModel



    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun clickAuditory()
        abstract fun clickTeacher()
        abstract fun clickSchedule()
        abstract fun selectDate(y: Int, m: Int, d: Int)
        abstract fun selectTimeBegin(h: Int, m: Int)
        abstract fun selectTimeEnd(h: Int, m: Int)
        abstract fun saveClicked()
        abstract fun addLinkClick()
        abstract fun allDayChange(isAllDay: Boolean)

        abstract val receiver: BroadcastReceiver

    }
}