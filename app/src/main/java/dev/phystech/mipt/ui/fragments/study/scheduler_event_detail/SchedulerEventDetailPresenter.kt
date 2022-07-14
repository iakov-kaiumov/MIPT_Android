package dev.phystech.mipt.ui.fragments.study.scheduler_event_detail

import dev.phystech.mipt.R
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.repositories.*
import dev.phystech.mipt.ui.fragments.navigator.place_detail.PlaceDetailFragment
import dev.phystech.mipt.ui.fragments.study.scheduler_event_edit.SchedulerEventEditFragment
import dev.phystech.mipt.ui.fragments.study.teacher_detail.TeacherDetailFragment
import dev.phystech.mipt.ui.fragments.users_list.UsersListDataResource
import dev.phystech.mipt.ui.fragments.users_list.UsersListFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class SchedulerEventDetailPresenter(val id: Int): SchedulerEventDetailContract.Presenter() {

    private var model: EventModel? = null
    private val notes: BehaviorSubject<String> = BehaviorSubject.create()

    override fun attach(view: SchedulerEventDetailContract.View?) {
        super.attach(view)
        view?.showProgress()
        ScheduleEventRepository.shared.getById(id) {
            view?.hideProgress()
            it?.let { model ->
                view?.setContent(model)
                this.model = model
            }
        }

        notes.debounce(4, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ newNote ->
                model?.let {
                    if (it.notes == newNote) return@let

                    it.notes = newNote
                    ScheduleEventRepository.shared.updateModel(it, false) {
                        if (it.not()) {
                            view?.showMessage(R.string.message_some_error_try_late)
                        }
                    }
                }
            })
    }


    /** MVP PRESENTER
     * @see SchedulerEventDetailContract.Presenter
     */
    override fun sharePressed() {
        model?.let { model ->
            if (ScheduleEventRepository.shared.containsInMyEvents(model.id.toString())) {
                model.urls?.share?.let {
                    view.sendLink(NetworkUtils.SERVER_ADDRESS + it)
                    view?.showStudy()
                }
            } else {
                view.showProgress()
                val secret = model.urls?.findSecret()
                ScheduleEventRepository.shared.subscribe(model.id.toString(), secret) {
                    view.hideProgress()

                    if (it) {
                        view?.toNews()
                    } else {
                        view?.showMessage(R.string.message_some_error_try_late)
                    }
                }
            }
        }
    }

    override fun editPressed() {
//        view?.showEditAlert(model?.canEdit ?: false)
        view?.showEditAlert(true)
    }

    override fun locationPressed() {
        val auditoryId = model?.auditorium?.firstOrNull()?.id ?: return

        view.showProgress()
        SchedulePlaceRepository.shared.getById(auditoryId) {
            view.hideProgress()
            it?.building?.id.let { schedulerModelID ->
                PlacesRepository.shared.getById(schedulerModelID.toString()) {
                    it?.let {
                        val fragment = PlaceDetailFragment(it)
                        view.navigate(fragment)
                    }
                }
            }
        }
    }

    override fun lectorPressed() {
        model?.teachers?.firstOrNull()?.let {teacher ->
            TeachersRepository.shared.addTeacher(teacher)

            teacher.id?.toIntOrNull()?.let { teacherId ->
                val teacherFragment = TeacherDetailFragment.newInstance(teacherId, teacher.name)
                view.navigate(teacherFragment)
            }
        }


    }

    override fun translationPressed() {
//        TODO("Not yet implemented")
    }

    override fun confirmEditPersonal() {
        model?.let {
            val editFragment = SchedulerEventEditFragment.newInstance(it.id, false)
            view.navigate(editFragment)
        }

    }

    override fun confirmEditCommon() {
        model?.let {
            val editFragment = SchedulerEventEditFragment.newInstance(it.id, true)
            view.navigate(editFragment)
        }
    }

    override fun confirmDelete() {
        view?.showProgress()
        model?.id?.let {
            ScheduleEventRepository.shared.delete(it) {
                view?.hideProgress()
                if (it) {
                    view?.showMessage(R.string.success)
                    view.back()
                } else {
                    view?.showMessage(R.string.message_some_error_try_late)
                }
            }
        }

    }

    override fun updateNote(value: String) {
        notes.onNext(value)
    }

    override fun usersClick() {
        model?.id?.let { modelID ->
            val usersFragment = UsersListFragment.newInstance(UsersListDataResource.Event, modelID)
            view?.navigate(usersFragment)
        }

    }

}