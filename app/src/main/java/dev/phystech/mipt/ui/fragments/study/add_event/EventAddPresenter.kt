package dev.phystech.mipt.ui.fragments.study.add_event

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.*
import dev.phystech.mipt.models.add_event_view.AddEventFieldsViewModel
import dev.phystech.mipt.models.add_event_view.SelectedDate
import dev.phystech.mipt.models.add_event_view.SelectedTime
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.fragments.study.auditory_list.AuditoryListFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditPresenter
import dev.phystech.mipt.ui.fragments.study.scheduler_event_edit.SchedulerEventEditPresenter
import dev.phystech.mipt.ui.fragments.study.teachers_list.TeachersListFragment
import dev.phystech.mipt.ui.fragments.study.user_scheduler_list.UserSchedulerListFragment
import io.realm.RealmList
import java.text.SimpleDateFormat
import java.util.*

class EventAddPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): EventAddContract.Presenter() {

    private var viewModel: AddEventFieldsViewModel? = null

    private var selectedDate: SelectedDate? = null
    private var selectedTimeBegin: SelectedTime? = null
    private var selectedTimeEnd: SelectedTime? = null

    private var selectedTeacher: Teacher? = null
    private var selectedAuditory: SchedulePlace? = null
    private var selectedScheduler: ScheduleItem? = null

    private val linkAdapter: LinksAdapter = LinksAdapter()

    private val types = arrayListOf(
        SchedulerType("exam", resources.getString(R.string.event_item_exam)),
        SchedulerType("conf", resources.getString(R.string.event_item_conf)),
        SchedulerType("quiz", resources.getString(R.string.event_item_quiz)),
        SchedulerType("assign", resources.getString(R.string.event_item_assign)),
        SchedulerType("other", resources.getString(R.string.event_item_other))
    )

    private val links = mutableMapOf(
        LINK_RECORDS to false,
        LINK_LIVE to false,
        LINK_MATERIALS to false
    )


    override fun attach(view: EventAddContract.View?) {
        super.attach(view)
        view?.setDeadlinesVisibilist(false)
        view?.setTypes(types)
        view?.setLinksAdapter(linkAdapter)
        viewModel?.let { view?.setContent(it) }
    }


    override fun clickAuditory() {
        viewModel = view?.buildModel()
        view?.showAuditoryList()
    }

    override fun clickTeacher() {
        viewModel = view?.buildModel()
        view?.showTeacherList()
    }

    override fun clickSchedule() {
        viewModel = view?.buildModel()
        view?.showScheduleList()
    }

    override fun selectDate(y: Int, m: Int, d: Int) {
        viewModel = view?.buildModel() ?: viewModel
        selectedDate = SelectedDate(y, m, d)

        val date = GregorianCalendar(y, m, d, 0, 0).time
        val timeValue = displayDateFormat.format(date)

        viewModel?.date = timeValue
        viewModel?.let { view?.setContent(it) }
    }

    override fun selectTimeBegin(h: Int, m: Int) {
        viewModel = view?.buildModel() ?: viewModel
        selectedTimeBegin = SelectedTime(h, m)

        val date = GregorianCalendar(0, 0, 0, h, m).time
        val timeValue = displayTimeFormat.format(date)

        viewModel?.timeBegin = timeValue
        viewModel?.let { view?.setContent(it) }
    }

    override fun selectTimeEnd(h: Int, m: Int) {
        viewModel = view?.buildModel() ?: viewModel
        selectedTimeEnd = SelectedTime(h, m)

        val date = GregorianCalendar(0, 0, 0, h, m).time
        val timeValue = displayTimeFormat.format(date)

        viewModel?.timeEnd = timeValue
        viewModel?.let { view?.setContent(it) }
    }

    override fun saveClicked() {
        val viewModel = view?.buildModel() ?: return

        if (checkModelValid(viewModel).not()) return

        val selectedDate = selectedDate!!
        val selectedTimeBegin = selectedTimeBegin!!
        val selectedTimeEnd = selectedTimeEnd!!

        val calendar = GregorianCalendar(
            selectedDate.year,
            selectedDate.month,
            selectedDate.day,
            selectedTimeBegin.hour,
            selectedTimeBegin.minute
        )
        val timeValue = serverDateFormat.format(calendar.time)
        var timeEndValue = timeValue

        if (viewModel.allDay == false) {
            val calendarEnd = GregorianCalendar(
                selectedDate.year,
                selectedDate.month,
                selectedDate.day,
                selectedTimeEnd.hour,
                selectedTimeEnd.minute
            )
            timeEndValue = serverDateFormat.format(calendarEnd.time)
        }

        val urlsField = UrlsWrap()
        linkAdapter.items.forEach {
            when(it.id) {
                LINK_LIVE -> urlsField.broadcast = linkAdapter.getLinkById(it.id)
                LINK_RECORDS -> urlsField.records = linkAdapter.getLinkById(it.id)
                LINK_MATERIALS -> urlsField.materials = linkAdapter.getLinkById(it.id)
            }
        }

        val typeValue = types.firstOrNull { v -> v.title == viewModel.type }

        val model = EventModel().apply {
            allDay = viewModel.allDay ?: false
            name = viewModel.name ?: ""
            type = typeValue?.type ?: ""
            startTime = timeValue
            endTime = timeEndValue
            notes = viewModel.notes
            teachers = RealmList(selectedTeacher!!)
            auditorium = RealmList(selectedAuditory!!)
            urls = urlsField
        }

        view?.showProgress()
        ScheduleEventRepository.shared.createModel(model, false) {
            view?.hideProgress()
            if (!it) {
                view?.showMessage(R.string.error_empty_string)
            } else {
                view?.back()
            }
        }

    }

    override fun addLinkClick() {
        val emptyLinks = links.filter { v -> v.value.not() }.map { v ->
            SchedulerEditPresenter.StringID(
                getLinkTitle(v.key),
                v.key
            )
        }

        bottomSheetController.showSelector(emptyLinks) {
            links[it.id] = true
            linkAdapter.items.add(LinksAdapter.LinkModel(it.id, it.value))
            linkAdapter.notifyItemInserted(linkAdapter.items.size - 1)
        }
    }

    override fun allDayChange(isAllDay: Boolean) {
        view?.updateTimeSliceVisibility(isAllDay.not())
    }

    override val receiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    TeachersListFragment.ACTION -> {
                        val modelId = intent.getStringExtra(TeachersListFragment.DATA_ID) ?: return
                        TeachersRepository.shared.getByID(modelId) {
                            if (it == null) {
                                view?.showMessage(R.string.teacher_not_found)
                                return@getByID
                            }
                            selectedTeacher = it

                            viewModel?.teacher = it.name
                            viewModel?.let { view?.setContent(it) }
                        }
                    }
                    AuditoryListFragment.ACTION -> {
                        val modelId = intent.getStringExtra(AuditoryListFragment.DATA_ID) ?: return
                        SchedulePlaceRepository.shared.getById(modelId) {
                            if (it == null) {
                                view?.showMessage(R.string.auditory_not_found)
                                return@getById
                            }
                            selectedAuditory = it

                            viewModel?.auditory = it.name
                            viewModel?.let { view?.setContent(it) }
                        }
                    }
                    UserSchedulerListFragment.ACTION -> {
                        val modelId = intent.getIntExtra(UserSchedulerListFragment.DATA_ID, -1)
                        SchedulersRepository.shared.getSchedulerItemById(modelId) {
                            if (it == null) {
                                view?.showMessage(R.string.scheduler_not_found)
                                return@getSchedulerItemById
                            }
                            selectedScheduler = it

                            viewModel?.schedule = it.name
                            viewModel?.let { view?.setContent(it) }
                        }
                    }
                }
            }
        }
    }


    //  OTHERS
    private fun getLinkTitle(id: Int): String {
        return when (id) {
            SchedulerEventEditPresenter.LINK_RECORDS -> resources.getString(R.string.link_records_title)
            SchedulerEventEditPresenter.LINK_LIVE -> resources.getString(R.string.link_live_title)
            SchedulerEventEditPresenter.LINK_MATERIALS -> resources.getString(R.string.link_meterials_title)
            else -> ""
        }
    }

    private fun checkModelValid(viewModel: AddEventFieldsViewModel): Boolean {
        var errors = 0

        if (viewModel.name.isNullOrEmpty()) {
            ++errors
            view?.setNameError(R.string.fill_field)
        }

        if (viewModel.auditory.isNullOrEmpty()) {
            ++errors
            view?.setNameError(R.string.fill_field)
        }

        if (viewModel.teacher.isNullOrEmpty()) {
            ++errors
            view?.setTeacherError(R.string.fill_field)
        }

        if (viewModel.type.isNullOrEmpty()) {
            ++errors
            view?.setTypesError(R.string.fill_field)
        }

        if (viewModel.allDay ?: false) {
            if (selectedDate == null)  {
                view.setDateError(R.string.fill_field)
                ++errors
            }

            if (selectedTimeBegin == null) {
                view.setTimeBeginError(R.string.fill_field)
                ++errors
            }

        } else {
            if (selectedTimeBegin == null) {
                view?.setTimeBeginError(R.string.fill_field)
                ++errors
            }
            if (selectedTimeEnd == null) {
                view?.setTeacherError(R.string.fill_field)
            }
            if (selectedDate == null) {
                view?.setDateError(R.string.fill_field)
            }
        }

        return errors == 0
    }



    companion object {
        const val LINK_RECORDS = 1
        const val LINK_LIVE = 2
        const val LINK_MATERIALS = 3


        val displayDateFormat = SimpleDateFormat("dd MMM yyyy")
        val displayTimeFormat = SimpleDateFormat("HH:mm")

        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}