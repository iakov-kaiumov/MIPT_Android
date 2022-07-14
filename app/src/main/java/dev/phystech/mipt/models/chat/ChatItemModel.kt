package dev.phystech.mipt.models.chat

class ChatItemModel {
    var type: ChatItemType = ChatItemType.UserTextMessage
    var id: Int? = null

    var userMessageModel: UserMessageModel? = null
    var botMessageModel: BotMessageModel? = null
    var botMultiplyAnswer: BotMultiplyAnswer? = null
}