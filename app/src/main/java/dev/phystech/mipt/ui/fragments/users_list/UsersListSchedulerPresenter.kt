package dev.phystech.mipt.ui.fragments.users_list

import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.UsersAdapter
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.UserAbstract
import dev.phystech.mipt.repositories.ScheduleEventRepository
import dev.phystech.mipt.repositories.SchedulersRepository

class UsersListSchedulerPresenter(val schedulerID: Int): UsersListContract.Presenter() {

    var adapter: UsersAdapter = UsersAdapter()
    var allUsers: MutableList<UserAbstract> = mutableListOf()

    override fun attach(view: UsersListContract.View?) {
        super.attach(view)
        SchedulersRepository.shared.getSchedulerItemById(schedulerID) {
            if (it == null) {
                view?.showMessage(R.string.scheduler_not_found)
                return@getSchedulerItemById
            }

            allUsers.clear()
            allUsers.addAll(it.users)

            adapter.items.clear()
            adapter.items.addAll(it.users)
            adapter.notifyItemRangeInserted(0, it.users.size)
        }

        view?.setAdapter(adapter)
    }


    override fun updateSearchValue(value: String) {
        allUsers.filter { v -> v.name?.toLowerCase()?.contains(value.toLowerCase()) == true }.let {
            val removed = adapter.items.size

            adapter.items.clear()
            adapter.items.addAll(it)

            adapter.notifyItemRangeRemoved(0, removed)
            adapter.notifyItemRangeInserted(0, it.size)
        }
    }
}