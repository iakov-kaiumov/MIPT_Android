package dev.phystech.mipt.ui.fragments.study.scheduler_event_edit

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.SchedulerType
import java.util.*

interface SchedulerEventEditContract {
    interface View: BaseView {
        fun setContent(model: EventModel)
        fun updateTimeSliceVisibility(isChecked: Boolean)
        fun buildModel(): EventModel
        fun setAuditory(value: String)
        fun setTeacher(value: String)
        fun setDate(value: String)
        fun setTimeBegin(value: String)
        fun setTimeEnd(value: String)
        fun setTypes(types: ArrayList<SchedulerType>)
        fun <VH: RecyclerView.ViewHolder>setLinksAdapter(adapter: RecyclerView.Adapter<VH>)

        fun setNameError(@StringRes valueId: Int)
        fun setAuditoryError(@StringRes valueId: Int)
        fun setTeacherError(@StringRes valueId: Int)
        fun setDateError(@StringRes valueId: Int)
        fun setTimeBeginError(@StringRes valueId: Int)
        fun setTimeEndError(@StringRes valueId: Int)
        fun setTypesError(@StringRes valueId: Int)


        fun setResetChangesVisibility(isVisible: Boolean)

        fun showTeacherList()
        fun showAuditoryList()

        fun back()

    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun clickAuditory()
        abstract fun clickTeacher()
        abstract fun selectAuditory(auditoryId: String)
        abstract fun selectTeacher(teacherId: String)
        abstract fun selectTeacherByName(teacherName: String)
        abstract fun selectDate(y: Int, m: Int, d: Int)
        abstract fun selectTimeBegin(h: Int, m: Int)
        abstract fun selectTimeEnd(h: Int, m: Int)
        abstract fun saveClicked(forGroup: Boolean)
        abstract fun addLinkClick()
        abstract fun resetChanges()


    }
}