package dev.phystech.mipt.ui.fragments.support.blocking_users

import android.content.res.Resources
import dev.phystech.mipt.adapters.BlockingUsersAdapter
import dev.phystech.mipt.adapters.ChatsAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.ChatRepository
import java.text.SimpleDateFormat

class BlockingUsersPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): BlockingUsersContract.Presenter() {

    private val adapter: BlockingUsersAdapter = BlockingUsersAdapter()

    override fun attach(view: BlockingUsersContract.View?) {
        super.attach(view)
        loadBlockingUsers()
    }

    override fun loadBlockingUsers() {
        view?.showLoader(true)
        ChatRepository.shared.getBlockedUsers() { blockedUsers ->
            view?.showLoader(false)
            adapter.items.clear()
            adapter.items.addAll(blockedUsers)
            adapter.filteredItems.clear()
            adapter.filteredItems.addAll(blockedUsers)
            adapter.notifyDataSetChanged()
            view?.setAdapter(adapter)
        }
    }

    override fun unblockUser(id: String) {
        view?.showLoader(true)
        ChatRepository.shared.unblockUser(id) {
            view?.showLoader(false)
            loadBlockingUsers()
        }
    }

    companion object {
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy")
        val displayTimeFormat = SimpleDateFormat("HH:mm")
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}