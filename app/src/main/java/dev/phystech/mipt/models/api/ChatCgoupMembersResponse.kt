package dev.phystech.mipt.models.api

data class ChatCgoupMembersResponse (
    val status: String,
    val data: ArrayList<Member> = ArrayList()
)