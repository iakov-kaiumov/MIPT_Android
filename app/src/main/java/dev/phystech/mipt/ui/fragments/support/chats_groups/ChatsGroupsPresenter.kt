package dev.phystech.mipt.ui.fragments.support.chats_groups

import android.content.res.Resources
import dev.phystech.mipt.adapters.ChatsGroupsAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.ui.fragments.support.chats.ChatsContract
import dev.phystech.mipt.utils.ChatUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

class ChatsGroupsPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): ChatsContract.Presenter() {

    private val adapter: ChatsGroupsAdapter = ChatsGroupsAdapter()
    private var countGroups: Long = 0

    override fun attach(view: ChatsContract.View?) {
        super.attach(view)
        load()
    }

    override fun load() {
        view?.showLoader(true)

        ChatUtils.groupsChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->
                view?.showLoader(false)
                if (conversations.isNotEmpty()) {
                    adapter.items.clear()
                    adapter.items.addAll(conversations)
                    adapter.filteredItems.clear()
                    adapter.filteredItems.addAll(conversations)
                    adapter.notifyDataSetChanged()
                    view?.setAdapter(adapter)
                }
            }
    }

    companion object {
        val displayDateFormat = SimpleDateFormat("dd MMM yyyy")
        val displayTimeFormat = SimpleDateFormat("HH:mm")
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}