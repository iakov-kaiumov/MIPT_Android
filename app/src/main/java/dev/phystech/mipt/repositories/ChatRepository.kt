package dev.phystech.mipt.repositories

import dev.phystech.mipt.models.api.*
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.isSuccess
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmResults
import java.util.concurrent.TimeUnit
import dev.phystech.mipt.models.BotMessagesModel
import dev.phystech.mipt.models.ChatMessagesModel


class ChatRepository private constructor() {

    val realm = Realm.getDefaultInstance()

    // REALM OPERATIONS

    fun getConversationsDB() {
        val single: Int? = 1
        val groups: Int? = 2
        val base: Int? = 3

        // Загрузка чатов
        realm.where(ChatMessagesModel::class.java).findAll().let { chatsMessages ->
            ChatUtils.chatsMessages.clear()
            ChatUtils.chatsMessages.addAll(realm.copyFromRealm(chatsMessages))

            // Загрузка чата с ботом
            realm.where(BotMessagesModel::class.java).findAll().let { botChatMessages ->
                val arrayBot: ArrayList<BotMessagesModel> = ArrayList()
                arrayBot.addAll(realm.copyFromRealm(botChatMessages))
                arrayBot.sortBy { list -> list.id }
                ChatUtils.botMessages.clear()
                ChatUtils.botMessages.addAll(arrayBot)

                realm.where(Conversation::class.java).equalTo("type", base).findAll().let { baseChats ->
                    realm.where(Conversation::class.java).equalTo("type", single).findAll().let { singleChats ->

                        val arrayTotal: ArrayList<Conversation> = ArrayList()

                        if (ArrayList(baseChats).isNotEmpty()) {
                            val array: ArrayList<Conversation> = ArrayList()
                            array.addAll(realm.copyFromRealm(baseChats))
                            array.sortBy { list -> list.baseChatType }
                            arrayTotal.addAll(array)
                        } else {
                            val newBaseConversations = ArrayList<Conversation>()
                            newBaseConversations.add(Conversation().apply {
                                id = -1
                                baseChatType = 1
                                type = 3
                            })
                            newBaseConversations.add(Conversation().apply {
                                id = -2
                                baseChatType = 2
                                type = 3
                            })
                            arrayTotal.addAll(newBaseConversations)
                        }

                        if (ArrayList(singleChats).isNotEmpty()) {
                            val array: ArrayList<Conversation> = ArrayList()
                            array.addAll(realm.copyFromRealm(singleChats))

                            for (item in array) {
                                if (item.messages.isEmpty()) {
                                    val foundConv = ChatUtils.chatsMessages.find { list -> list.id == item.id }
                                    foundConv?.messages?.let { foundMessages ->
                                        if (foundMessages.isNotEmpty()) {
                                            item.messages.add(foundMessages.last())
                                        }
                                    }
                                }
                            }

                            arrayTotal.addAll(ChatUtils.getSortArrayByDate(array))
                        }

                        ChatUtils.singleChats.onNext(arrayTotal)
                    }
                }

                realm.where(Conversation::class.java).equalTo("type", groups).findAll().let { groupsChats ->
                    val array: ArrayList<Conversation> = ArrayList()
                    array.addAll(realm.copyFromRealm(groupsChats))

                    if (array.isNotEmpty())
                        for (item in array) {
                            if (item.messages.isEmpty()) {
                                val foundConv = ChatUtils.chatsMessages.find { list -> list.id == item.id }
                                foundConv?.messages?.let { foundMessages ->
                                    if (foundMessages.isNotEmpty()) {
                                        item.messages.add(foundMessages.last())
                                    }
                                }
                            }
                        }

                    ChatUtils.groupsChats.onNext(ChatUtils.getSortArrayByDate(array))
                }
            }
        }
    }

    fun deleteConversation(id: Long) {
        val conversations: RealmResults<Conversation> = realm.where(Conversation::class.java).equalTo("id", id).findAll();
        realm.beginTransaction()
        if(!conversations.isEmpty()) {
            conversations.first()?.deleteFromRealm();
        }
        realm.commitTransaction()
    }

    fun deleteAllConversations() {
        realm.beginTransaction()
        val conversations: RealmResults<Conversation> = realm.where(Conversation::class.java).findAll();
        if(!conversations.isEmpty()) {
            for (i in conversations.size - 1 downTo 0) {
                conversations.get(i)?.deleteFromRealm()
            }
        }
        realm.commitTransaction()
    }

    fun saveModelsInRealm(models: ArrayList<Conversation>) {
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(models)
        }
    }

    fun saveMessagesOfDialog(convid: Long, items: ArrayList<Message>, chatMode: Int) {  // chatMode: 1 - личные, 2 - групповые, 3 - бот

        val newChat: ChatMessagesModel = ChatMessagesModel().apply {
            id = convid
            messages.addAll(items)
        }

        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(arrayListOf(newChat))
        }

        val foundChat = ChatUtils.chatsMessages.find { list -> list.id == convid }
        if (foundChat != null) {
            foundChat.messages.clear()
            foundChat.messages.addAll(items)
        } else {
            ChatUtils.chatsMessages.add(newChat)
        }
    }

    fun deleteMessagesOfDialog(convid: Long) {
        val chat: ChatMessagesModel? = realm.where(ChatMessagesModel::class.java).equalTo("id", convid).findFirst();
        realm.beginTransaction()
        chat?.let {
            chat.deleteFromRealm()
        }
        realm.commitTransaction();
    }

    fun deleteMessageOfDialog(convid: Long, messageId: Long) {
        val chat: ChatMessagesModel? = realm.where(ChatMessagesModel::class.java).equalTo("id", convid).findFirst();
        realm.beginTransaction()
        chat?.let {
            val message = chat.messages.find { messages -> messages.id == messageId }
            message?.let {
                message.deleteFromRealm()
            }
        }
        realm.commitTransaction();
    }

    fun saveMessageOfBotDialog(message: BotMessagesModel) {
        realm.executeTransactionAsync { realmTransaction ->
            realmTransaction.insertOrUpdate(message)
        }
    }

    // SERVER OPERATIONS

    fun loadChatsCount(callback: ((ChatCountData?) -> Unit)? = null) {
        ApiClient.shared.getConversationsCount()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess && it.data != null) {
                    callback?.invoke(it.data)
                } else {
                    callback?.invoke(null)
                }
            }, {
                callback?.invoke(null)
            })
    }

    fun loadChats() {
        ApiClient.shared.getConversations(type = 1)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({

                val newConversations = ArrayList<Conversation>()
                newConversations.add(Conversation().apply {
                    id = -1
                    baseChatType = 1
                    type = 3
                })
                newConversations.add(Conversation().apply {
                    id = -2
                    baseChatType = 2
                    type = 3
                })

                if (it.status.isSuccess && it.data != null) {
                    newConversations.addAll(ChatUtils.getSortArrayByDate(it.data.conversations))
                }

                ChatUtils.singleChatsLoader.onNext(false)
                ChatUtils.singleChats.onNext(newConversations)
            }, {
                ChatUtils.singleChatsLoader.onNext(false)
//                val newConversations = ArrayList<Conversation>()
//                newConversations.add(Conversation().apply {
//                    id = -1
//                    baseChatType = 1
//                    type = 3
//                })
//                newConversations.add(Conversation().apply {
//                    id = -2
//                    baseChatType = 2
//                    type = 3
//                })
//                ChatUtils.singleChats.onNext(newConversations)
            })
    }

    fun loadGroupChats() {
        ApiClient.shared.getConversations(type = 2)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                if (it.status.isSuccess && it.data != null && !it.data.conversations.isNullOrEmpty()) {
                    val newConversations = ChatUtils.getSortArrayByDate(it.data.conversations)
                    ChatUtils.groupsChats.onNext(newConversations)
                }
            }, {
//                ChatUtils.groupsChats.onNext(ArrayList<Conversation>())
            })
    }

    fun loadUsers(filter: String, callback: ((ArrayList<UsersResponseModel.UserInfoModel>) -> Unit)? = null) {
        ApiClient.shared.getUsers(filter, perPage = 30)
            .debounce(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data: ArrayList<UsersResponseModel.UserInfoModel> = ArrayList()
                it.data?.paginator?.map { v -> v.value }?.let {
                    data.addAll(it)
                }
                callback?.invoke(data)
            }, {
                callback?.invoke(ArrayList<UsersResponseModel.UserInfoModel>())
            })
    }

    fun getBlockedUsers(callback: ((ArrayList<BlockUserElement>) -> Unit)? = null) {
        ApiClient.shared.getBlockedUsers()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data: ArrayList<BlockUserElement> = ArrayList()
                it.data?.users?.let {
                    data.addAll(it)
                }
                callback?.invoke(data)
            }, {
                callback?.invoke(ArrayList<BlockUserElement>())
            })
    }

    fun blockUser(id: String, callback: ((Void?) -> Unit)? = null) {
        ApiClient.shared.blockUser(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(null)
            }, {
                callback?.invoke(null)
            })
    }

    fun unblockUser(id: String, callback: ((Void?) -> Unit)? = null) {
        ApiClient.shared.unblockUser(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(null)
            }, {
                callback?.invoke(null)
            })
    }

    fun getChat(convid: Long, limitnum: Int, limitfrom: Int, callback: ((ChatDetailData?) -> Unit)? = null) {
        ApiClient.shared.getChat(convid, limitnum = limitnum, limitfrom = limitfrom)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ chatData ->
                if (chatData != null && chatData.data != null && !chatData.data.messages.isNullOrEmpty()) {
                    chatData.data.messages.reverse()
                }
                callback?.invoke(chatData.data)
            }, {
                callback?.invoke(null)
            })
    }

    fun markAllMessagesAsRead(convid: Long, callback: ((Void?) -> Unit)? = null) {
        ApiClient.shared.markAllMessagesAsRead(convid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                callback?.invoke(null)
            }, {
                callback?.invoke(null)
            })
    }

    fun sendMessage(convid: Long, message: String, callback: ((Boolean, Message?) -> Unit)? = null) {
        val textModel = ChatSendMessageRequest(messages = arrayListOf<ChatMessageRequest>(ChatMessageRequest(text = message)))
        ApiClient.shared.sendMessage(convid, textModel)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ sendMessageData ->
                if (sendMessageData.status.isSuccess && !sendMessageData.data.isNullOrEmpty())
                    callback?.invoke(true, sendMessageData.data.first())
                else
                    callback?.invoke(true, null)
            }, {
                callback?.invoke(false, null)
            })
    }

    fun sendMessageNewChat(userId: String, message: String, callback: ((Boolean, Message?) -> Unit)? = null) {
        val textModel = ChatSendMessageRequest(messages = arrayListOf<ChatMessageRequest>(ChatMessageRequest(text = message, touserid = userId)))
        ApiClient.shared.sendMessageNewChat(textModel)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ sendMessageData ->
                if (sendMessageData.status.isSuccess && !sendMessageData.data.isNullOrEmpty()){
                    val sendedMessage = sendMessageData.data.first()
                    if (sendedMessage.msgid != null) sendedMessage.id = sendedMessage.msgid
                    callback?.invoke(true, sendedMessage)
                }
                else
                    callback?.invoke(true, null)
            }, {
                callback?.invoke(false, null)
            })
    }

    fun deleteMessage(id: Long, callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.deleteMessage(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                if (data.status.isSuccess)
                    callback?.invoke(true)
                else
                    callback?.invoke(false)
            }, {
                callback?.invoke(false)
            })
    }

    fun deleteDialog(convid: Long, callback: ((Boolean) -> Unit)? = null) {
        ApiClient.shared.deleteDialog(convid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ data ->
                if (data.status.isSuccess)
                    callback?.invoke(true)
                else
                    callback?.invoke(false)
            }, {
                callback?.invoke(false)
            })
    }

    fun getUserInfo(id: String, callback: ((ArrayList<Member>) -> Unit)? = null) {
        ApiClient.shared.getUserInfo(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ userData ->
                callback?.invoke(userData.data)
            }, {
                callback?.invoke(ArrayList<Member>())
            })
    }

    fun getGroupChatUsers(convid: Long, callback: ((ArrayList<Member>) -> Unit)? = null) {
        ApiClient.shared.getGroupChatUsers(convid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ chatData ->
                callback?.invoke(chatData.data)
            }, {
                callback?.invoke(ArrayList<Member>())
            })
    }

    companion object {
        val shared: ChatRepository = ChatRepository()
    }
}
