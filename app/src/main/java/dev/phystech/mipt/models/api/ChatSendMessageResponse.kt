package dev.phystech.mipt.models.api

data class ChatSendMessageResponse (
    val status: String,
    val data: ArrayList<Message>?
)