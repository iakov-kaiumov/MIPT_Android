package dev.phystech.mipt.models.api

import com.squareup.moshi.JsonClass
import dev.phystech.mipt.models.User

@JsonClass(generateAdapter = true)
data class SocketConversatiomMessageModel(
    val courseid: Long? = null,
    val modulename: Any? = null,
    val component: String? = null,
    val name: String? = null,
    val userfrom: User? = null,
    val convid: Long? = null,
    val userto: SocketUserModel? = null,
    val smallmessage: String? = null,
    val notification: Long? = null,
    val replyto: Any? = null,
    val replytoname: Any? = null,
    val savedmessageid: Long? = null,
    val timecreated: Long? = null
)

data class SocketUserModel (
    val id: String? = null,
    val email: String? = null,
    val lastname: String? = null,
    val firstname: String? = null,
    val middlename: String? = null
)