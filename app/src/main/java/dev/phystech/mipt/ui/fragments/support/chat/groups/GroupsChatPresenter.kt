package dev.phystech.mipt.ui.fragments.support.chat.groups

import android.content.res.Resources
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.adapters.ChatAdapter2Groups
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.utils.ChatUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat

class GroupsChatPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): GroupsChatContract.Presenter() {

    private val adapter: ChatAdapter2Groups = ChatAdapter2Groups()

    public val groupMembers: ArrayList<Member> = ArrayList()

    private var newMessageDisposable: Disposable? = null

    override fun attach(view: GroupsChatContract.View?) {
        super.attach(view)
    }

    override fun detach() {
        newMessageDisposable?.dispose()
        super.detach()
    }

    override fun initUserData(convid: Long) {
        newMessageDisposable = ChatUtils.newMessage
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { newMessage ->
                if (newMessage.conversationid != null && convid == newMessage.conversationid && !adapter.items.isNullOrEmpty()) {
                    adapter.items.add(newMessage)
                    view?.scrollToBottom()
                    adapter.notifyItemInserted(adapter.items.size - 1)
                    markAllMessagesAsRead(convid)
                    ChatUtils.newMessage.onNext(Message())
                }
            }
    }

    override fun loadChat(convid: Long, limitnum: Int, limitfrom: Int) {

        view?.showLoader(true)
        ChatRepository.shared.getChat(convid, limitnum, limitfrom) { chatData ->
            view?.showLoader(false)
            if (chatData != null) {

                if (adapter.items.isEmpty()) {
                    chatData.messages?.let {
                        adapter.items.clear()
                        adapter.items.addAll(it)
                        ChatUtils.setHeaderDateToMessages(adapter.items)
                        adapter.notifyDataSetChanged()
                        view?.setAdapter(adapter)
                    }

                    view?.setupToolbarMenu(true)
                    markAllMessagesAsRead(convid)
                } else {
                    if (chatData.messages.isNullOrEmpty()) {
                        adapter.loadNewMessages = false
                    } else {
                        adapter.items.addAll(0, chatData.messages)
                        ChatUtils.setHeaderDateToMessages(adapter.items)
                        adapter.notifyItemRangeInserted(0, chatData.messages.size)
                        adapter.notifyItemChanged(chatData.messages.size)
                    }
                }

                ChatRepository.shared.saveMessagesOfDialog(convid, adapter.items, 2)
            }
        }
    }

    override fun sendMessage(convid: Long, message: String, localeId: Long?) {
        view?.showLoader(true)
        ChatRepository.shared.sendMessage(convid, message) { succsess, message ->
            view?.showLoader(false)
            view?.updateMessageStatus(localeId, message)
        }
    }

    override fun deleteChat(convid: Long) {
        view?.showLoader(true)
        ChatRepository.shared.deleteDialog(convid) {
            view?.showLoader(false)
            if (it) {
                ChatRepository.shared.deleteConversation(convid)
                ChatRepository.shared.deleteMessagesOfDialog(convid)
                view?.chatIsDeleted()
            }
        }
    }

    override fun deleteMessage(convid: Long, messageId: Long) {
        view?.showLoader(true)
        ChatRepository.shared.deleteMessage(messageId) {
            view?.showLoader(false)
            if (it) {
                ChatRepository.shared.deleteMessageOfDialog(convid, messageId)
                view?.deleteMessage(messageId)
            }
        }
    }

    override fun markAllMessagesAsRead(convid: Long) {
        ChatRepository.shared.markAllMessagesAsRead(convid) { data ->

        }
    }

    override fun getGroupChatMembers(convid: Long) {
        ChatRepository.shared.getGroupChatUsers(convid) {
            groupMembers.clear()
            groupMembers.addAll(it)
        }
    }

    override fun getMembers(): ArrayList<Member> {
        return groupMembers
    }

    override fun getAdapter(): ChatAdapter2Groups {
        return adapter
    }
}