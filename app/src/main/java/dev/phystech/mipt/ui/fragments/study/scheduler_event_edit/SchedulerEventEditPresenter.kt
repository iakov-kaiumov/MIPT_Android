package dev.phystech.mipt.ui.fragments.study.scheduler_event_edit

import android.content.res.Resources
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.*
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.ui.fragments.study.scheduler_edit.SchedulerEditPresenter
import io.realm.RealmList
import java.text.SimpleDateFormat
import java.util.*

class SchedulerEventEditPresenter(
    val modelId: Int,
    val resources: Resources,
    val bottomSheetController: BottomSheetController
    ): SchedulerEventEditContract.Presenter() {

    private var event: EventModel? = null
    private var selectedTeacher: Teacher? = null
    private var selectedAuditory: SchedulePlace? = null
    private var modelCourse: String? = null

    private var selectedDate: Date? = null
    private var selectedTimeBegin: Date? = null
    private var selectedTimeEnd: Date? = null

    private val linkAdapter: LinksAdapter = LinksAdapter()

    private val links = mutableMapOf(
        LINK_RECORDS to false,
        LINK_LIVE to false,
        LINK_MATERIALS to false
    )

    private val types = arrayListOf(
        SchedulerType("exam", resources.getString(R.string.event_item_exam)),
        SchedulerType("assign", resources.getString(R.string.event_item_assign)),
        SchedulerType("conference", resources.getString(R.string.event_item_conf)),
        SchedulerType("quiz", resources.getString(R.string.event_item_quiz)),
        SchedulerType("other", resources.getString(R.string.event_item_other))
    )

    override fun attach(view: SchedulerEventEditContract.View?) {
        super.attach(view)

        if (event == null) {
            ScheduleEventRepository.shared.getById(modelId) {
//            event = it
                it?.let {
                    modelCourse = it.course
                    selectedTeacher = it.teachers.firstOrNull()
                    selectedAuditory = it.auditorium.firstOrNull()
                    view?.setContent(it)
                    serverDateFormat.parse(it.startTime)?.let {
                        selectedDate = it
                        selectedTimeBegin = it
                        view?.setDate(displayDateFormat.format(it))
                        view?.setTimeBegin(displayTimeFormat.format(it))
                    }
                    serverDateFormat.parse(it.endTime)?.let {
                        view?.setTimeEnd(displayTimeFormat.format(it))
                        selectedTimeEnd = it
                    }

                    it.urls?.materials?.let { link ->
                        if (link.isEmpty() || linkAdapter.items.firstOrNull{ v -> v.id == LINK_MATERIALS } != null) return@let
                        links[LINK_MATERIALS] = true;
                        linkAdapter.items.add(LinksAdapter.LinkModel(
                            LINK_MATERIALS, getLinkTitle(LINK_MATERIALS
                            ), link))
                    }
                    it.urls?.records?.let { link ->
                        if (link.isEmpty() || linkAdapter.items.firstOrNull{ v -> v.id == LINK_RECORDS } != null) return@let
                        links[LINK_RECORDS] = true
                        linkAdapter.items.add(LinksAdapter.LinkModel(
                            LINK_RECORDS, getLinkTitle(LINK_RECORDS), link)
                        )
                    }
                    it.urls?.broadcast?.let { link ->
                        if (link.isEmpty() || linkAdapter.items.firstOrNull{ v -> v.id == LINK_LIVE } != null) return@let
                        links[LINK_LIVE] = true
                        linkAdapter.items.add(LinksAdapter.LinkModel(LINK_LIVE, getLinkTitle(
                                LINK_LIVE
                            ), link))
                    }
                }
            }
        } else {
            view?.setContent(event!!)
            serverDateFormat.parse(event!!.startTime)?.let {
                view?.setDate(displayDateFormat.format(it))
                view?.setTimeBegin(displayTimeFormat.format(it))
            }
            serverDateFormat.parse(event!!.endTime)?.let {
                view?.setTimeEnd(displayTimeFormat.format(it))
            }
        }

        view?.setTypes(types)
        view?.setLinksAdapter(linkAdapter)


        selectedAuditory?.name?.let {
            view?.setAuditory(it)
        }

        selectedTeacher?.name?.let {
            view?.setTeacher(it)
        }
    }


    override fun clickAuditory() {
        event = getModel()
        view.showAuditoryList()
    }

    override fun clickTeacher() {
        event = getModel()
        view.showTeacherList()
    }

    override fun selectAuditory(auditoryId: String) {
        view.showProgress()
        SchedulePlaceRepository.shared.getById(auditoryId) {
            view.hideProgress()
            selectedAuditory = it

            it?.name?.let {
                view?.setAuditory(it)
            }
        }
    }

    override fun selectTeacher(teacherId: String) {
        view.showProgress()
        TeachersRepository.shared.getByID(teacherId) {
            view.hideProgress()
            selectedTeacher = it

            it?.name?.let {
                view?.setTeacher(it)
            }
        }
    }

    override fun selectTeacherByName(teacherName: String) {
        selectedTeacher = Teacher().apply {
            id = null
            name = teacherName
        }

        view?.setTeacher(teacherName)
    }

    override fun selectDate(y: Int, m: Int, d: Int) {
        val date = GregorianCalendar(y, m, d, 0, 0).time
        selectedDate = date
        view?.setDate(displayDateFormat.format(date))
    }

    override fun selectTimeBegin(h: Int, m: Int) {
        val date = GregorianCalendar(0, 0, 0, h, m).time
        selectedTimeBegin = date
        val timeValue = displayTimeFormat.format(date)
        view?.setTimeBegin(timeValue)
    }

    override fun selectTimeEnd(h: Int, m: Int) {
        val date = GregorianCalendar(0, 0, 0, h, m).time
        selectedTimeEnd = date

        val timeValue = displayTimeFormat.format(date)
        view?.setTimeEnd(timeValue)
    }

    override fun saveClicked(forGroup: Boolean) {
        val model = getModel()
        var errors = 0

        if (model?.name.isNullOrEmpty()) {
            ++errors
            view.setNameError(R.string.fill_field)
        }

        if (model?.auditorium.isNullOrEmpty()) {
            ++errors
            view.setAuditoryError(R.string.fill_field)
        }

        if (model?.teachers.isNullOrEmpty() && model?.prof.isNullOrEmpty()) {
            ++errors
            view.setTeacherError(R.string.fill_field)
        }

        if (model?.startTime.isNullOrEmpty()) {
            ++errors
            view.setDateError(R.string.fill_field)
            view.setTimeBeginError(R.string.fill_field)
        }

        if (model?.allDay == false && model.endTime.isNullOrEmpty()) {
            ++errors
            view.setTimeEndError(R.string.fill_field)
        }

        if (model?.type.isNullOrEmpty()) {
            ++errors
            view.setTypesError(R.string.fill_field)
        }

        if (errors > 0 || model == null) return

        view.showProgress()

        val groupValue = if (forGroup) 1 else null
        ScheduleEventRepository.shared.updateModel(model, forGroup) {
            view.hideProgress()
            if (it) {
                view?.showMessage(R.string.success)
                view.back()
            } else {
                view?.showMessage(R.string.message_some_error_try_late)
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

    override fun resetChanges() {
        view?.showProgress()
        ScheduleEventRepository.shared.resetChanges(modelId) {
            view?.hideProgress()
            if (it.not()) {
                view?.showMessage(R.string.message_some_error_try_late)
            }
        }
    }


    private fun getModel(): EventModel? {
        val model = view?.buildModel()?.apply {
            id = modelId
            urls = this.urls ?: UrlsWrap()
            course = modelCourse
        }

        linkAdapter.items.forEach {
            when(it.id) {
                LINK_LIVE -> model?.urls?.broadcast = linkAdapter.getLinkById(it.id)
                LINK_RECORDS -> model?.urls?.records = linkAdapter.getLinkById(it.id)
                LINK_MATERIALS -> model?.urls?.materials = linkAdapter.getLinkById(it.id)
            }
        }

        selectedAuditory?.let { model?.auditorium = RealmList(*arrayListOf(it).toTypedArray()) }
        selectedTeacher?.let {
            if (it.id == null) {
                model?.teachers = RealmList()
                model?.prof = it.name
            } else {
                model?.teachers = RealmList(*arrayListOf(it).toTypedArray())
                model?.prof = it.name
            }
        }

        selectedDate?.let { selectedDate ->
            selectedTimeBegin?.let {
                val calendar = GregorianCalendar().apply { time = selectedDate }
                val calendarStart = GregorianCalendar().apply { time = it }

                var dt = calendar.time

                val h = calendarStart.get(Calendar.HOUR_OF_DAY)
                val m = calendarStart.get(Calendar.MINUTE)

                calendar.set(Calendar.HOUR_OF_DAY, h.toInt())
                calendar.set(Calendar.MINUTE, m.toInt())

                dt = calendar.time

                model?.startTime = serverDateFormat.format(calendar.time)
            }

            selectedTimeEnd?.let {
                val calendar = GregorianCalendar().apply { timeInMillis = selectedDate.time }
                val calendarEnd = GregorianCalendar().apply { time = it }

                val h = calendarEnd.get(Calendar.HOUR_OF_DAY)
                val m = calendarEnd.get(Calendar.MINUTE)

                calendar.set(Calendar.HOUR_OF_DAY, h)
                calendar.set(Calendar.MINUTE, m)

                model?.endTime = serverDateFormat.format(it)
            }
        }

        return model
    }

    private fun getLinkTitle(id: Int): String {
        return when (id) {
            LINK_RECORDS -> resources.getString(R.string.link_records_title)
            LINK_LIVE -> resources.getString(R.string.link_live_title)
            LINK_MATERIALS -> resources.getString(R.string.link_meterials_title)
            else -> ""
        }
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