package dev.phystech.mipt.ui.fragments.study.scheduler_edit

import android.content.res.Resources
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.repositories.SchedulePlaceRepository
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.repositories.TeachersRepository
import dev.phystech.mipt.repositories.TimeSlotsRepository
import dev.phystech.mipt.ui.fragments.study.scheduler_edit_simulars.SchedulerEditSimularsFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.Serializable

class SchedulerEditPresenter(
    val resources: Resources,
    val bottomSheetController: BottomSheetController,
    val type: SchedulerEditFragment.Type,
    val schedulerId: Int? = null
) : SchedulerEditContract.Presenter() {

    var newScheduler: SchedulerIndex? = SchedulerIndex()

    init {
        if (type == SchedulerEditFragment.Type.Create) {
            newScheduler = SchedulerIndex()
        }
    }

    private val links = mutableMapOf(
        LINK_RECORDS to false,
        LINK_LIVE to false,
        LINK_MATERIALS to false
    )

    private val linkAdapter: LinksAdapter = LinksAdapter()

    override fun attach(view: SchedulerEditContract.View?) {
        super.attach(view)
        if (view == null) return

        TeachersRepository.shared.teachers
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ view.setLectors(it.mapNotNull { v -> v.name }) }, {})

        TimeSlotsRepository.shared.slots
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.setBeginTimes(it.mapNotNull { v -> v.start })
                view.setEndTimes(it.mapNotNull { v -> v.end })
            }, {})

        SchedulePlaceRepository.shared.places
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.setAuditories(it.mapNotNull { v -> v.name })
            }, {})

        SchedulersRepository.shared.categories
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.setCategories(it)
            }, {
            })

        view.setCourses(arrayListOf("Ин.яз.", "Математика", "Русский"))
        view.setDayOfWeeks(resources.getStringArray(R.array.weeks).toList())
        view.setRepeatValues(resources.getStringArray(R.array.scheduler_repeat).toList())
        view.setTypesOfScheduler(
            arrayListOf(
                SchedulerType("lec", resources.getString(R.string.schedule_item_lec)),
                SchedulerType("sem", resources.getString(R.string.schedule_item_sem)),
                SchedulerType("lab", resources.getString(R.string.schedule_item_lab))
            )
        )

        if (type == SchedulerEditFragment.Type.Create || true) {
            newScheduler?.name?.let { view.setSchedulerName(it) }
            newScheduler?.auditorium?.firstOrNull()?.name?.let { view.setAuitoryName(it) }
            newScheduler?.teachers?.firstOrNull()?.name?.let { view.setTeacherName(it) }
        }

        schedulerId?.let {
            SchedulersRepository.shared.getSchedulerItemById(it)?.let { scheduleItem ->
                scheduleItem.urls.materials?.let { link ->
                    if (link.isEmpty() || linkAdapter.items.firstOrNull { v -> v.id == LINK_MATERIALS } != null) return@let
                    links[LINK_MATERIALS] = true;
                    linkAdapter.items.add(
                        LinksAdapter.LinkModel(
                            LINK_MATERIALS,
                            getLinkTitle(LINK_MATERIALS),
                            link
                        )
                    )
                }
                scheduleItem.urls.records?.let { link ->
                    if (link.isEmpty() || linkAdapter.items.firstOrNull { v -> v.id == LINK_RECORDS } != null) return@let
                    links[LINK_RECORDS] = true
                    linkAdapter.items.add(
                        LinksAdapter.LinkModel(
                            LINK_RECORDS,
                            getLinkTitle(LINK_RECORDS),
                            link
                        )
                    )
                }
                scheduleItem.urls.broadcast?.let { link ->
                    if (link.isEmpty() || linkAdapter.items.firstOrNull { v -> v.id == LINK_LIVE } != null) return@let
                    links[LINK_LIVE] = true
                    linkAdapter.items.add(
                        LinksAdapter.LinkModel(
                            LINK_LIVE,
                            getLinkTitle(LINK_LIVE),
                            link
                        )
                    )
                }
            }
        }

        view.setLinkAdapter(linkAdapter)


    }


    override fun saveClick() {

    }

    override fun saveClick(model: ScheduleItem, forGroup: Boolean) {
        linkAdapter.items.forEach {
            when (it.id) {
                LINK_LIVE -> model.urls.broadcast = linkAdapter.getLinkById(it.id)
                LINK_RECORDS -> model.urls.records = linkAdapter.getLinkById(it.id)
                LINK_MATERIALS -> model.urls.materials = linkAdapter.getLinkById(it.id)
            }
        }

        newScheduler?.teachers?.firstOrNull()?.let {
            if (it.id == null) {
                model.teachers = arrayListOf()
            }
        }

        view.showProgress()
        SchedulersRepository.shared.newSchedule(model, forGroup) {
            view.hideProgress()
            if (it) {
                view.back()
            } else {
                view.showMessage(R.string.message_some_error_try_late)
            }
        }
    }

    override fun cleatChangesClick() {

    }

    override fun deleteClick() {

    }

    override fun backClick() {

    }

    override fun addLinkClick() {
        val emptyLinks =
            links.filter { v -> v.value.not() }.map { v -> StringID(getLinkTitle(v.key), v.key) }
        bottomSheetController.showSelector(emptyLinks) {
            links[it.id] = true
            linkAdapter.items.add(LinksAdapter.LinkModel(it.id, it.value))
            linkAdapter.notifyItemInserted(linkAdapter.items.size - 1)
        }
    }

    fun setAuditoryById(id: String) {
        SchedulePlaceRepository.shared.getById(id) {
            if (it == null) return@getById

            if (type == SchedulerEditFragment.Type.Create || true) {
                newScheduler?.auditorium = arrayListOf(it)
                view.setAuitoryName(it.name)
            }

        }

    }

    fun setTeacherById(id: String) {
        if (TeachersRepository.shared.teachers.hasValue()) {
            val teacher =
                TeachersRepository.shared.teachers.value.firstOrNull { v -> v.id == id } ?: return

            if (type == SchedulerEditFragment.Type.Create || true) {
                newScheduler?.teachers = arrayListOf(teacher)
                newScheduler?.prof = teacher.name
            }
        }
    }

    fun setTeacherByName(name: String) {
        if (type == SchedulerEditFragment.Type.Create || true) {
            newScheduler?.teachers = arrayListOf(Teacher().apply {
                this.name = name
                id = null
            })
        }
    }


    override fun editClick(model: ScheduleItem, forGroup: Boolean) {
        linkAdapter.items.forEach {
            when (it.id) {
                LINK_LIVE -> model.urls.broadcast = linkAdapter.getLinkById(it.id)
                LINK_RECORDS -> model.urls.records = linkAdapter.getLinkById(it.id)
                LINK_MATERIALS -> model.urls.materials = linkAdapter.getLinkById(it.id)
            }
        }

        newScheduler?.teachers?.firstOrNull()?.let {
            if (it.id == null) {
                model.teachers = arrayListOf()
            }
        }

        view.showProgress()
        SchedulersRepository.shared.updateSchedule(
            model,
            forGroup
        ) { success, newId, message, simularList ->
            view.hideProgress()
            if (success) {
                view?.sendSchedulerEditedBroadcast(newId)

                if (message != null) {
                    view?.showCallbackMessage(message) {
                        view?.back()
                    }
                } else if (simularList?.size ?: 0 > 0) {
                    if (schedulerId == null || simularList == null) return@updateSchedule

                    view?.navigate(
                        SchedulerEditSimularsFragment.newInstance(
                            requireNotNull(schedulerId),
                            ArrayList(requireNotNull(simularList))
                        )
                    )
                } else {
                    view?.back()
                }


            } else {
                view.showMessage(R.string.message_some_error_try_late)
            }
        }
        //  TODO: for group
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
    }

    data class StringID(
        val value: String,
        val id: Int
    ) {
        override fun toString(): String = value.toString()
    }
}