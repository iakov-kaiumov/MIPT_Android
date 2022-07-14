package dev.phystech.mipt.ui.fragments.support.search_users

import android.content.res.Resources
import dev.phystech.mipt.adapters.ChatsUsersAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.ChatRepository
import java.text.SimpleDateFormat

class SearchUsersPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): SearchUsersContract.Presenter() {

    private val adapter: ChatsUsersAdapter = ChatsUsersAdapter()

    override fun attach(view: SearchUsersContract.View?) {
        super.attach(view)
        getBlockedUsers()
    }

    override fun searchUsers(filter: String) {
        view?.showLoader(true)
        ChatRepository.shared.loadUsers(filter) { users ->
            view?.showLoader(false)
            adapter.items.clear()
            adapter.items.addAll(users)
            adapter.notifyDataSetChanged()
            view?.setAdapter(adapter)
        }
    }

    override fun getBlockedUsers() {
        ChatRepository.shared.getBlockedUsers() { users ->
            view?.showBlockedUsersButton(!users.isNullOrEmpty())
        }
    }

    companion object {
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy")
        val displayTimeFormat = SimpleDateFormat("HH:mm")
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}