package dev.phystech.mipt.utils

import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import dev.phystech.mipt.adapters.ChatAdapter
import dev.phystech.mipt.models.chat.*
import dev.phystech.mipt.network.ApiClient

class ChatSession {

    interface Delegate {
        fun onNewMessage()
        fun scrollToDown()
        fun updateTopics(topics: ArrayList<String>)
    }

    var delegate: Delegate? = null

    private val adapter: ChatAdapter = ChatAdapter()
    private var idCounter: Int = 1

    fun sendMessage(content: String) {
        val messageModel = createUserMessage(content)
        adapter.newMessage(messageModel)

        ApiClient.shared.getChatAnswer(content)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                messageModel.userMessageModel?.deliveryStatus = MessageDeliveryStatus.Delivered
                adapter.notifyDataSetChanged()      // TODO: Change all notifying

                it.data?.paginator?.let { messages ->
                    if (messages.size == 1) {           //  Bot text message
                        val responseModel = messages.values.first()

                        responseModel.answer?.let { messageContent ->
                            val message = createBotMessage(messageContent)
                            adapter.newMessage(message)
                        }

                        if (responseModel.buttons.size != 0) {
                            val model = ArrayList(responseModel.buttons.map { v -> Pair(v.name ?: "", v.answer ?: "") })
                            val message = createBotMultiplyMessage(model)
                            adapter.newMessage(message)
                        }
                    }

                    if (messages.size > 1) {     //  Bot multiply answer
                        val topics = ArrayList(messages.values.map { v -> v.name ?: ""}.filter { v -> v.isNotEmpty() })
                        delegate?.updateTopics(topics)

                        return@subscribe
                        val model = ArrayList(messages.values.map { v -> Pair(v.name ?: "", v.answer ?: "") })
                        val message = createBotMultiplyMessage(model)
                        adapter.newMessage(message)
                    }
                }

                delegate?.onNewMessage()
            }, {

            })
    }

    private fun createUserMessage(value: String): ChatItemModel {
        val messageModel = ChatItemModel()
        messageModel.userMessageModel = UserMessageModel.Builder()
            .setText(value)
            .withDeliveryStatus(MessageDeliveryStatus.Sent)
            .build()
        messageModel.id = idCounter++

        return messageModel
    }

    private fun createBotMessage(value: String): ChatItemModel {
        val messageModel = ChatItemModel()
        messageModel.botMessageModel = BotMessageModel.Builder()
            .setText(value)
            .build()

        messageModel.id = idCounter++

        messageModel.type = ChatItemType.BotTextMessage
        return messageModel
    }

    private fun createBotMultiplyMessage(value: ArrayList<Pair<String, String>>): ChatItemModel {
        val messageModel = ChatItemModel()
        messageModel.botMultiplyAnswer = BotMultiplyAnswer().apply {
            messages = value
        }
        messageModel.id = idCounter++
        messageModel.type = ChatItemType.BotMultiplyAnswer

        return messageModel
    }


    fun getAdapter(): ChatAdapter {
        return adapter
    }

    fun getItemsCount(): Int = adapter.items.size

    fun loadDataFromDB() {

        if (ChatUtils.botMessages.isNotEmpty()) {

            for(botMessage in ChatUtils.botMessages) {
                val item = Gson().fromJson(botMessage.messageJson, ChatItemModel::class.java)
                item?.userMessageModel?.deliveryStatus = MessageDeliveryStatus.Delivered
                adapter.items.add(item)
            }

            ChatUtils.botMessages.last().id?.let { newId ->
                idCounter = newId + 1
            }

            delegate?.scrollToDown()
        }
    }
}