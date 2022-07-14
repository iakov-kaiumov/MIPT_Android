package dev.phystech.mipt.models.api

data class ChatSendMessageRequest (
    val messages: ArrayList<ChatMessageRequest>
)

data class ChatMessageRequest (
    val text: String,
    val touserid: String? = null
)