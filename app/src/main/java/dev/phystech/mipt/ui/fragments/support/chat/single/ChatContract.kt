package dev.phystech.mipt.ui.fragments.support.chat.single

import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.adapters.ChatAdapter2
import dev.phystech.mipt.base.BaseFragment
import dev.phystech.mipt.base.mvp.BasePresenter
import dev.phystech.mipt.base.mvp.BaseView
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.models.api.Message

interface ChatContract {
    interface View: BaseView {
        fun navigate(fragment: BaseFragment)
        fun setContent(model: Member)
        fun updateMessageStatus(localeId: Long?, message: Message?)
        fun showLoader(visible: Boolean)
        fun showError(message: String)
        fun setupToolbarMenu(userIsBlock: Boolean)
        fun deleteMessage(id: Long)
        fun chatIsDeleted()
        fun connect(value: Boolean)
        fun updateConversationId(conversationId: Long)
        fun showBlockUserView(value: Boolean)
        fun <VH : RecyclerView.ViewHolder>setAdapter(adapter: RecyclerView.Adapter<VH>)
        fun scrollToBottom()
    }

    abstract class Presenter: BasePresenter<View>() {
        abstract fun initUserData(convid: Long)
        abstract fun loadChat(convid: Long, limitnum: Int, limitfrom: Int)
        abstract fun sendMessage(convid: Long, message: String, localeId: Long?)
        abstract fun sendMessageNewChat(userId: String, message: String, localeId: Long?)
        abstract fun blockUser(id: String)
        abstract fun unblockUser(id: String)
        abstract fun deleteChat(convid: Long)
        abstract fun deleteMessage(convid: Long, messageId: Long)
        abstract fun markAllMessagesAsRead(convid: Long)
        abstract fun getAdapter(): ChatAdapter2
//        abstract val receiver: BroadcastReceiver
    }
}