package dev.phystech.mipt.ui.fragments.users_list

import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.UsersAdapter
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.repositories.ScheduleEventRepository

class UsersListEventPresenter(val eventID: Int): UsersListContract.Presenter() {

    var adapter: UsersAdapter = UsersAdapter()
    var allUsers: MutableList<Teacher> = mutableListOf()

    override fun attach(view: UsersListContract.View?) {
        super.attach(view)
        ScheduleEventRepository.shared.getById(eventID) {
            if (it == null) {
                view?.showMessage(R.string.event_not_found)
                return@getById
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
        allUsers.filter { v -> v.name?.contains(value) == true }.let {
            val removed = adapter.items.size

            adapter.items.clear()
            adapter.items.addAll(it)

            adapter.notifyItemRangeRemoved(0, removed)
            adapter.notifyItemRangeInserted(0, it.size)
        }
    }
}