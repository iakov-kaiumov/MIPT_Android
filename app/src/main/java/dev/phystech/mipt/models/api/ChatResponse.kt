package dev.phystech.mipt.models.api

import android.app.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.User
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

data class ChatResponse (
    val status: String,
    val data: ChatData?
)

data class ChatData (
    val conversations: ArrayList<Conversation> = ArrayList<Conversation>()
)

open class Conversation: RealmObject() {

    @PrimaryKey
    var id: Long? = null
    var name: String? = ""
    var subname: String? = ""
    var imageurl: String? = null
    var type: Long? = null
    var membercount: Long? = null
    var isfavourite: Boolean? = null
    var isread: Boolean? = null
    var unreadcount: Int = 0 // Непрочитанные сообщения
    var members: RealmList<Member> = RealmList()
    var messages: RealmList<Message> = RealmList()

    var baseChatType: Int = 0
    var groupsCount: Long = 0

    fun getBaseName(): String {
        when(baseChatType) {
            1 -> {
                return dev.phystech.mipt.Application.context.getString(R.string.toolbar_support)
            }
            2 -> {
                return dev.phystech.mipt.Application.context.getString(R.string.lessons_messages_title)
            }
            else -> {
                return if (!members.isNullOrEmpty() && members.first()!!.user != null) {
                    members.first()!!.user!!.getTrimName()
                } else ""
            }
        }
    }

    fun getAvatarName(): String {
        var result: String = ""
        if(!name.isNullOrEmpty())
            for (char in name!!.split(' '))
                result += char.get(0)
        return result
    }
}

open class Member: Serializable, RealmObject() {

    @PrimaryKey
    var id: String = ""
    var fullname: String = ""
    var profileurl: String = ""
    var profileimageurl: String = ""
    var profileimageurlsmall: String = ""
    @Ignore
    var isonline: Boolean = false
    var showonlinestatus: Boolean = false
    var isblocked: Boolean = false
    var iscontact: Boolean = false
    var isdeleted: Boolean = false
    var canmessage: Boolean? = null
//    val requirescontact: Any? = null,
//    val contactrequests: List<Any?>,
    var user: User? = null

    fun getAvatarName(): String {
        var result: String = ""
        user?.let { user ->
            if(!user.getTrimName().isNullOrEmpty())
                for (char in user.getTrimName().split(' '))
                    result += char.get(0)
        }
        return result
    }

}

open class Message: RealmObject() {

    @PrimaryKey
    var id: Long? = null
    var useridfrom: String = ""
    var text: String = ""
    var timecreated: Long = 0
    var user: User? = null

    var msgid: Long? = null
    var conversationid: Long? = null

    var status: String? = null
    var showDate: Boolean = false
    var localeId: Long? = null

    init {
        if (status == null) status = "successful"
    }

    fun getAvatarName(): String {
        var result: String = ""
        if(user != null && !user?.getTrimName().isNullOrEmpty())
            for (char in user?.getTrimName()!!.split(' '))
                result += char.get(0)
        return result
    }
}