package dev.phystech.mipt.models.api

data class ChatDetailResponse (
    val status: String,
    val data: ChatDetailData?
)

data class ChatDetailData (
    val id: Int,
    val members: ArrayList<Member>? = null,
    val messages: ArrayList<Message>? = null
)