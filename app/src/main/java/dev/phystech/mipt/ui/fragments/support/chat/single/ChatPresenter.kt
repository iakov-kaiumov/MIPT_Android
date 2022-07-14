package dev.phystech.mipt.ui.fragments.support.chat.single

import android.content.res.Resources
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.base.interfaces.BottomSheetController
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.repositories.ChatRepository
import dev.phystech.mipt.utils.ChatUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ChatPresenter(
    private val resources: Resources,
    private val bottomSheetController: BottomSheetController
): ChatContract.Presenter() {

    private val adapter: ChatAdapter2 = ChatAdapter2()
    private var singleChatsDisposable: Disposable? = null
    private var newMessageDisposable: Disposable? = null

    override fun attach(view: ChatContract.View?) {
        super.attach(view)
    }

    override fun detach() {
        singleChatsDisposable?.dispose()
        newMessageDisposable?.dispose()
        super.detach()
    }

    override fun initUserData(convid: Long) {
        singleChatsDisposable = ChatUtils.singleChats
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { conversations ->
                if (conversations.isNotEmpty()) {
                    var conversation = conversations.firstOrNull { v -> v.id == convid}
                    conversation?.members?.let { members ->
                        if (members.isNotEmpty())
                            view?.setContent(members.first()!!)
                    }
                }
            }

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
        view?.connect(false)
        ChatRepository.shared.getChat(convid, limitnum, limitfrom) { chatData ->
            view?.showLoader(false)
            if (chatData != null) {

                view?.connect(true)
                // Раскомментировать когда данные о собеседнике будут правильно приходить с сервера, если захотят
//                chatData.members?.let {
//                    if (it.isNotEmpty())
//                        view?.setContent(it.get(0))
//                }

                if (adapter.items.isEmpty()) {
                    chatData.messages?.let {
                        adapter.items.clear()
                        adapter.items.addAll(it)
                        ChatUtils.setHeaderDateToMessages(adapter.items)
                        adapter.notifyDataSetChanged()
                        view?.setAdapter(adapter)
                    }

//                    view?.setupToolbarMenu(true)
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

                ChatRepository.shared.saveMessagesOfDialog(convid, adapter.items, 1)
            }
        }
    }

    override fun sendMessage(convid: Long, message: String, localeId: Long?) {
        view?.showLoader(true)
        ChatRepository.shared.sendMessage(convid, message) { succsess, message ->
            view?.showLoader(false)
            view?.connect(succsess)
            view?.updateMessageStatus(localeId, message)
        }
    }

    override fun sendMessageNewChat(userId: String, message: String, localeId: Long?) {
        view?.showLoader(true)
        ChatRepository.shared.sendMessageNewChat(userId, message) { succsess, message ->
            message?.conversationid?.let { conversationid ->
                view?.updateConversationId(conversationid)
            }
            view?.showLoader(false)
            view?.connect(succsess)
            view?.updateMessageStatus(localeId, message)
        }
    }

    override fun blockUser(id: String) {
        view?.showLoader(true)
        ChatRepository.shared.blockUser(id) {
            view?.showLoader(false)
            view?.showBlockUserView(true)
        }
    }

    override fun unblockUser(id: String) {
        view?.showLoader(true)
        ChatRepository.shared.unblockUser(id) {
            view?.showLoader(false)
            view?.showBlockUserView(false)
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

    override fun getAdapter():ChatAdapter2 {
        return adapter
    }
}