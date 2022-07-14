package dev.phystech.mipt.adapters

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.Message
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter2: RecyclerView.Adapter<ChatAdapter2.BaseViewHolder>() {

    var items: ArrayList<Message> = ArrayList()
    var onClickListener: OnClickListener? = null
    var onLoadData: OnLoadData? = null
    var loadNewMessages: Boolean = true

    //  ADAPTER METHODS
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = inflater.inflate(R.layout.item_chat_message_me, parent, false)
                MyMessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_chat_message_user, parent, false)
                UserMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position].useridfrom == ChatUtils.userId) 0 else 1
    }

    // OTHERS
    fun newMessage(item: Message) {
        items.add(item)
        ChatUtils.setHeaderDateToMessages(items)
        notifyItemInserted(items.size)
    }

    fun updateMessageStatus(localeId: Long?, message: Message?) {
        val item = items.firstOrNull { v -> v.localeId == localeId}
        val position = items.indexOf(item)
        if (message != null) {
            item?.let {
                message.useridfrom = it.useridfrom
                message.showDate = it.showDate
                message.user = it.user
                items[position] = message
            }
        } else {
            item?.status = "error"
        }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }

    fun deleteMessage(id: Long) {
        val item = items.firstOrNull { v -> v.id == id}
        val position = items.indexOf(item)
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun deleteMessageLocal(localeId: Long?) {
        val item = items.firstOrNull { v -> v.localeId == localeId}
        val position = items.indexOf(item)
        if (position >= 0) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getReplacedText(str: String): String {
        var result: String = ""
        if (!str.isNullOrEmpty()) {
            result = str
            result = result.replace("\n", "<br>")
            result = result.replace("<p>", "")
            result = result.replace("</p>", "")
        }
        return result
    }

    //  VIEW HOLDERS
    inner class MyMessageViewHolder(view: View): BaseViewHolder(view) {
        lateinit var model: Message

        private val clMessage: ConstraintLayout = view.findViewById(R.id.clMessage)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivMessageStatus: ImageView = view.findViewById(R.id.ivMessageStatus)

        override fun bind(model: Message) {
            this.model = model

            if (adapterPosition == 0 && loadNewMessages) {
                onLoadData?.load()
            }

            tvDate.visibility = model.showDate.visibility()
            if (model.showDate) tvDate.text = TimeUtils.getMessageDate(model.timecreated)

            tvTime.text = TimeUtils.getMessageTime(model.timecreated)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvMessage.text = Html.fromHtml(getReplacedText(model.text), Html.FROM_HTML_MODE_COMPACT)
            } else {
                tvMessage.text = Html.fromHtml(getReplacedText(model.text))
            }

            tvMessage.movementMethod = LinkMovementMethod()

            when(model.status) {
                "waiting" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_waiting);
                }
                "error" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_warning_2);
                }
                else  -> {
                    ivMessageStatus.visibility = View.GONE
                }
            }

            clMessage.setOnClickListener{
                onClickListener?.showOptionsmenu(Pair(clMessage, model))
//                return@setOnLongClickListener true
            }
        }
    }

    inner class UserMessageViewHolder(view: View): BaseViewHolder(view) {
        lateinit var model: Message

        private val clMessage: ConstraintLayout = view.findViewById(R.id.clMessage)
        private val tvDate: TextView = view.findViewById(R.id.tvDate)
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)

        override fun bind(model: Message) {
            this.model = model

            if (adapterPosition == 0 && loadNewMessages) {
                onLoadData?.load()
            }

            tvDate.visibility = model.showDate.visibility()
            if (model.showDate) tvDate.text = TimeUtils.getMessageDate(model.timecreated)

            tvTime.text = TimeUtils.getMessageTime(model.timecreated)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvMessage.text = Html.fromHtml(getReplacedText(model.text), Html.FROM_HTML_MODE_COMPACT)
            } else {
                tvMessage.text = Html.fromHtml(getReplacedText(model.text))
            }

            tvMessage.movementMethod = LinkMovementMethod()

            clMessage.setOnClickListener{
                onClickListener?.showOptionsmenu(Pair(clMessage, model))
//                return@setOnLongClickListener true
            }
        }
    }

    abstract inner class BaseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(model: Message)
    }

    class OnClickListener(val clickListener: (item: Pair<View, Message>) -> Unit) {
        fun showOptionsmenu(item: Pair<View, Message>) = clickListener(item)
    }

    class OnLoadData(val onLoadData: () -> Unit) {
        fun load() = onLoadData()
    }
}

