package dev.phystech.mipt.models.api

data class ChatCountsResponse (
    val status: String,
    val data: ChatCountData?
)

data class ChatCountData (
    val favourites: Long,
    val types: Map<String, Long>?
)
