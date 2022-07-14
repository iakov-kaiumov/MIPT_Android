package dev.phystech.mipt.adapters

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.BotMessagesModel
import dev.phystech.mipt.models.chat.ChatItemModel
import dev.phystech.mipt.models.chat.ChatItemType
import dev.phystech.mipt.models.chat.MessageDeliveryStatus.*
import dev.phystech.mipt.ui.utils.MultiplyChatAnswerWrap
import dev.phystech.mipt.utils.ChatUtils
import com.google.gson.Gson
import dev.phystech.mipt.repositories.ChatRepository


class ChatAdapter: RecyclerView.Adapter<ChatAdapter.BaseViewHolder>() {
    var items: ArrayList<ChatItemModel> = ArrayList()


    //  ADAPTER METHODS
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ChatItemType.BotMultiplyAnswer.ordinal -> {
                val view = inflater.inflate(R.layout.item_chat_multiply_message_bot, parent, false)
                BotMultiplyAnswerViewHolder(view)
            }
            ChatItemType.BotTextMessage.ordinal -> {
                val view = inflater.inflate(R.layout.item_chat_text_message_bot, parent, false)
                BotMessageViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_chat_text_message_user, parent, false)
                UserMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        val model = items[position]
        return model.type.ordinal
    }



    // OTHERS
    fun newMessage(item: ChatItemModel) {
        items.add(item)
        notifyItemInserted(items.size - 1)

        val message = BotMessagesModel().apply {
            id = item.id
            messageJson = Gson().toJson(item)
        }

        ChatUtils.botMessages.add(message)
        ChatRepository.shared.saveMessageOfBotDialog(message)
    }



    //  VIEW HOLDERS
    inner class UserMessageViewHolder(view: View): BaseViewHolder(view) {
        lateinit var model: ChatItemModel

        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivDeliveryStatus: ImageView = view.findViewById(R.id.ivDeliveryStatus)


        override fun bind(model: ChatItemModel) {
            this.model = model

            model.userMessageModel?.let { viewModel ->
                //  Text data
                tvTime.text = viewModel.time

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvMessage.text = Html.fromHtml(viewModel.text, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tvMessage.text = Html.fromHtml(viewModel.text)
                }

                tvMessage.movementMethod = LinkMovementMethod()

                //  Image
                val image = when (viewModel.deliveryStatus) {
                    Sent -> R.drawable.ic_message_status_sended
                    Delivered -> R.drawable.ic_message_status_delivered
                    null -> null
                }
                image?.let {
                    ivDeliveryStatus.setImageResource(image)
                }


            }
        }
    }

    inner class BotMessageViewHolder(view: View): BaseViewHolder(view) {
        lateinit var model: ChatItemModel

        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)


        override fun bind(model: ChatItemModel) {
            this.model = model

            model.botMessageModel?.let { viewModel ->
                tvTime.text = viewModel.time
                tvMessage.text = viewModel.text

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvMessage.text = Html.fromHtml(viewModel.text, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tvMessage.text = Html.fromHtml(viewModel.text)
                }

                tvMessage.movementMethod = LinkMovementMethod()
            }
        }
    }

    inner class BotMultiplyAnswerViewHolder(view: View): BaseViewHolder(view), ChatAnswersAdapter.Delegate {
        lateinit var model: ChatItemModel

        private val answersAdapter: ChatAnswersAdapter = ChatAnswersAdapter(ArrayList())

        private val multiplyChatItemViewWrap: MultiplyChatAnswerWrap = MultiplyChatAnswerWrap(view)

        init {
            answersAdapter.delegate = this
            multiplyChatItemViewWrap.page1.recycler.adapter = answersAdapter
        }

        override fun bind(model: ChatItemModel) {
            this.model = model

            answersAdapter.items.clear()
            answersAdapter.items.addAll(model.botMultiplyAnswer?.messages ?: ArrayList())
            answersAdapter.notifyDataSetChanged()
            multiplyChatItemViewWrap.showPage1()

        }

        //  Chat answers adapter
        override fun onSelect(model: Pair<String, String>) {
            multiplyChatItemViewWrap.showPage2(model)
        }
    }



    abstract inner class BaseViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(model: ChatItemModel)
    }

}

