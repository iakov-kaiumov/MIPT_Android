package dev.phystech.mipt.ui.fragments.support.chats

import android.content.res.Resources
import dev.phystech.mipt.adapters.ChatsAdapter
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.utils.ChatUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm
import java.lang.Exception
import java.text.SimpleDateFormat

class ChatsPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): ChatsContract.Presenter() {

    private val adapter: ChatsAdapter = ChatsAdapter()
    private var countGroups: Long = 0
    private var groupsNoReadMessagesCount: Int = 0

    private var singleChatsDisposable: Disposable? = null
    private var groupsChatsDisposable: Disposable? = null

    private var loaderDisposable: Disposable? = null

    override fun attach(view: ChatsContract.View?) {
        super.attach(view)
        load()
    }

    override fun detach() {
        singleChatsDisposable?.dispose()
        groupsChatsDisposable?.dispose()
        loaderDisposable?.dispose()
        super.detach()
    }

    override fun load() {

//        ChatRepository.shared.loadChatsCount { chatCountData ->
//            chatCountData?.types?.let { types ->
//                val countGroups: Long? = types.get("2")
//                countGroups?.let {
//                    this.countGroups = it
//                    adapter.items.let { items ->
//                        if (items.isNotEmpty()) {
//                            items.get(1).groupsCount = this.countGroups
//                            adapter.notifyDataSetChanged()
//                        }
//                    }
//                }
//            }
//        }

        loaderDisposable = ChatUtils.singleChatsLoader
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { loaderValue ->
                view?.showLoader(loaderValue)
            }

        groupsChatsDisposable = ChatUtils.groupsChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->
                if (conversations.isNotEmpty()) {
                    this.countGroups = conversations.size.toLong()
                    groupsNoReadMessagesCount = 0
                    for(item in conversations)
                        if (item.unreadcount > 0)
                            groupsNoReadMessagesCount += item.unreadcount
                    adapter.items.let { items ->
                        if (items.isNotEmpty()) {
                            items.get(1).groupsCount = this.countGroups
                            items.get(1).unreadcount = groupsNoReadMessagesCount
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

        singleChatsDisposable = ChatUtils.singleChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->
                if (conversations.isNotEmpty()) {
                    conversations.get(1).groupsCount = countGroups
                    conversations.get(1).unreadcount = groupsNoReadMessagesCount
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