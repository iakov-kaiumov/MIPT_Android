package dev.phystech.mipt.utils

import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.BotMessagesModel
import dev.phystech.mipt.models.ChatMessagesModel
import dev.phystech.mipt.models.Lastactive
import dev.phystech.mipt.models.User
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.repositories.ChatRepository
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object ChatUtils {

    const val WEB_SOCKET_URL = "wss://appadmin.mipt.ru/websocket/?token="

    var userId: String = ""
    val perPage: Int = 30

    val singleChatsLoader: BehaviorSubject<Boolean> = BehaviorSubject.create()

    val singleChats: BehaviorSubject<ArrayList<Conversation>> = BehaviorSubject.create()
    val groupsChats: BehaviorSubject<ArrayList<Conversation>> = BehaviorSubject.create()

    val chatsMessages: ArrayList<ChatMessagesModel> = ArrayList()
    val botMessages: ArrayList<BotMessagesModel> = ArrayList()

    val newMessage: BehaviorSubject<Message> = BehaviorSubject.create()

    internal fun getStatus(isonline: Boolean, lastactive: Lastactive?): String {

        if (isonline) {
            return Application.context.getString(R.string.online)
        } else {
            if (lastactive != null && lastactive.date != null) {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("GMT+03:00")
                val date: Date? = sdf.parse(lastactive.date)
                try {
                    if (date != null) {
                        return Application.context.getString(R.string.was_time, TimeUtils.getUserLastActive(date))
                    } else {
                        return Application.context.getString(R.string.offline)
                    }
                }catch (e: Exception) {
                    return Application.context.getString(R.string.offline)
                }
            } else {
                return Application.context.getString(R.string.not_logged_to_app)
            }
        }
    }

    internal fun getStatusGroup(usersCount: Long): String {
        val num: Long = usersCount % 10
        return if (usersCount > 10 && usersCount < 21) {
            Application.context.getString(R.string.participants_1, usersCount.toString())
        } else if (num == 1L) {
            Application.context.getString(R.string.participants_2, usersCount.toString())
        } else if (num > 1L && num < 5L) {
            Application.context.getString(R.string.participants_3, usersCount.toString())
        } else Application.context.getString(R.string.participants_1, usersCount.toString())
    }

    internal fun createNewMessage(useridfrom: String, text: String): Message {
        val timecreated: Long = (Date().time / 1000L)
        return Message().apply {
            id = null
            this.useridfrom = useridfrom
            this.text = text
            this.timecreated = timecreated
            user = null
            status = "waiting"
            localeId = Date().time
        }
    }

    internal fun setHeaderDateToMessages(messages: ArrayList<Message>){
        if (messages.isNotEmpty()){
            for(index in 0 until messages.size) {
                if (index == 0) {
                    messages[index].showDate = true
                } else {
                    if (TimeUtils.getMessageDayOfYear(messages[index].timecreated) != TimeUtils.getMessageDayOfYear(messages[index-1].timecreated)) {
                        messages[index].showDate = true
                    } else {
                        messages[index].showDate = false
                    }
                }
            }
        }
    }

    internal fun getUserRoles(roles: List<String>): String {
        var result: String = ""

        val separator: String = ", "
        val rolesRes: Array<String> = Application.context.resources.getStringArray(R.array.roles)

        for (role in roles) {
            when(role) {
                "admin"         -> result += rolesRes[0] + separator
                "manager"       -> result += rolesRes[1] + separator
                "tester"        -> result += rolesRes[2] + separator
                "student"       -> result += rolesRes[3] + separator
                "academ"        -> result += rolesRes[4] + separator
                "alumnus"       -> result += rolesRes[5] + separator
                "teacher"       -> result += rolesRes[6] + separator
                "staff"         -> result += rolesRes[7] + separator
                "undergraduate" -> result += rolesRes[8] + separator
                "graduate"      -> result += rolesRes[9] + separator
                "postgraduate"  -> result += rolesRes[10] + separator
                "psychologist"  -> result += rolesRes[11] + separator
                "expelled"      -> result += rolesRes[12] + separator
            }
        }
        if (result.isNotEmpty()) result = result.dropLast(2)
        return result
    }

    internal fun getSortArrayByDate(array: ArrayList<Conversation>): ArrayList<Conversation> {

        val arrayMessages: ArrayList<Conversation> = ArrayList(array.filter { list -> list.messages.isNotEmpty() })
        val arrayNoMessages: ArrayList<Conversation> = ArrayList(array.filter { list -> list.messages.isNullOrEmpty() })

        arrayMessages.sortByDescending { list -> list.messages.last()?.timecreated }
        arrayNoMessages.sortByDescending { list -> list.id }

        val newConversations: ArrayList<Conversation> = ArrayList()
        newConversations.addAll(arrayMessages)
        newConversations.addAll(arrayNoMessages)

        return ArrayList(newConversations)
    }

    internal fun clearChats() {

        ChatRepository.shared.realm.beginTransaction()
        ChatRepository.shared.realm.deleteAll()
        ChatRepository.shared.realm.commitTransaction()

        userId = ""
        singleChats.value?.let { chat -> chat.clear() }
        groupsChats.value?.let { chat -> chat.clear() }
        chatsMessages.clear()
        botMessages.clear()
    }
}