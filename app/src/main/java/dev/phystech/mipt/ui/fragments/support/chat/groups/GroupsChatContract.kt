package dev.phystech.mipt.ui.fragments.support.chat.groups

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.adapters.ChatAdapter2Groups
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.models.api.Message

interface GroupsChatContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun setContent(conversation: Conversation)
        fun updateMessageStatus(localeId: Long?, message: Message?)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun setupToolbarMenu(intentet: Boolean)
        fun deleteMessage(id: Long)
        fun chatIsDeleted()
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
        fun scrollToBottom()
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun initUserData(convid: Long)
        abstract fun loadChat(convid: Long, limitnum: Int, limitfrom: Int)
        abstract fun sendMessage(convid: Long, message: String, localeId: Long?)
        abstract fun deleteChat(convid: Long)
        abstract fun deleteMessage(convid: Long, messageId: Long)
        abstract fun markAllMessagesAsRead(convid: Long)
        abstract fun getGroupChatMembers(convid: Long)
        abstract fun getMembers(): ArrayList<Member>
        abstract fun getAdapter(): ChatAdapter2Groups
//        abstract val receiver: BroadcastReceiver
    }
}