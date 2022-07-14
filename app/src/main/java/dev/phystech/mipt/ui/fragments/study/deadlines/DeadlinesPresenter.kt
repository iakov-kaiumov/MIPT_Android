package dev.phystech.mipt.ui.fragments.study.deadlines

import android.content.res.Resources
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.DeadlinesAdapter
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerType
import dev.phystech.mipt.repositories.EventRepository
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulersRepository
import dev.phystech.mipt.ui.fragments.study.add_event.EventAddFragment
import dev.phystech.mipt.ui.fragments.study.add_event.utils.AddEventType
import dev.phystech.mipt.ui.fragments.study.scheduler_event_edit.SchedulerEventEditFragment

class DeadlinesPresenter(
    private val schedulerId: Int
): DeadlinesContract.Presenter(),
    DeadlinesAdapter.Delegate {

    private val adapter = DeadlinesAdapter()
    private var model: ScheduleItem? = null

    override fun attach(view: DeadlinesContract.View?) {
        super.attach(view)

        adapter.delegate = this
        view?.setAdapter(adapter)
        view?.showProgress()

        SchedulersRepository.shared.getSchedulerItemById(schedulerId) {
            if (it == null) {
                view?.hideProgress()
                view?.showMessage(R.string.message_some_error_try_late)
                return@getSchedulerItemById
            }

            this.model = it
            ScheduleEventRepository.shared.getForCourse(it.course!!) {
                view?.hideProgress()
                adapter.items.clear()
                adapter.items.addAll(it)
                adapter.notifyItemRangeInserted(0, it.size)
                view?.showEmptyListVisibility(it.size == 0)
            }
        }

    }


    override fun confirmEditForSelf(model: EventModel) {
        val fragment = SchedulerEventEditFragment.newInstance(model.id, false)
        view?.navigate(fragment)
    }

    override fun confirmEditForGroup(model: EventModel) {
        val fragment = SchedulerEventEditFragment.newInstance(model.id, true)
        view?.navigate(fragment)
    }

    override fun confirmAddForSelf() {
        val fragment = EventAddFragment.newInstance(AddEventType.Deadline, schedulerId)
        view?.navigate(fragment)
    }

    override fun confirmAddForGroup() {
        val fragment = EventAddFragment.newInstance(AddEventType.Deadline, schedulerId)
        view?.navigate(fragment)
    }

    override fun confirmDelete(model: EventModel) {
        ScheduleEventRepository.shared.delete(model.id) {
            if (it) {
                val position = adapter.items.indexOf(model)
                adapter.items.remove(model)
                adapter.notifyItemRemoved(position)
            } else {
                view?.showMessage(R.string.message_some_error_try_late)
            }
        }

    }

    override fun newDeadline() {
        view?.showAddAlert()
    }


    //  ADAPTER DELEGATE
    override fun onSelect(model: EventModel) {
        view?.showEditAlert(model)
    }
}