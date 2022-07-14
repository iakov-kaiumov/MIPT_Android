package dev.phystech.mipt.adapters

import android.content.res.Resources
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.models.chat.ChatItemModel
import dev.phystech.mipt.models.chat.ChatItemType
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.visibility
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import java.util.*
import kotlin.collections.ArrayList

class ChatsAdapter: RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    var items: ArrayList<Conversation> = arrayListOf()
    var filteredItems: ArrayList<Conversation> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position], if (position == 0) true else false)
    }

    override fun getItemCount(): Int = filteredItems.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        val ivAvatar: RoundedImageView = view.findViewById(R.id.ivAvatar)
        val llAvatar: LinearLayout = view.findViewById(R.id.llAvatar)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val vIsOnline: View = view.findViewById(R.id.vIsOnline)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvNoReadCount: TextView = view.findViewById(R.id.tvNoReadCount)

        val vPaddingTop: View = view.findViewById(R.id.vPaddingTop)
        val vDivider: View = view.findViewById(R.id.divider)

        fun bind(model: Conversation, firstItem: Boolean) {
            when (model.baseChatType) {
                1 -> {
                    llAvatar.visibility = View.GONE
                    ivAvatar.setImageResource(R.drawable.ic_support)
                    tvName.text = model.getBaseName()

                    tvMessage.text = ""
                    tvDateTime.text = ""

                    if (!ChatUtils.botMessages.isNullOrEmpty()) {

                        for (index in ChatUtils.botMessages.size - 1 downTo 1) {
                            val botChatMessage = Gson().fromJson(ChatUtils.botMessages[index].messageJson, ChatItemModel::class.java)

                            if (botChatMessage.type != ChatItemType.BotMultiplyAnswer) {

                                var botChatText = botChatMessage.userMessageModel?.text ?: botChatMessage.botMessageModel?.text
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    tvMessage.text = Html.fromHtml(botChatText ?: "", Html.FROM_HTML_MODE_LEGACY)
                                } else {
                                    tvMessage.text = Html.fromHtml(botChatText ?: "")
                                }

                                var botChatTime = botChatMessage.userMessageModel?.time ?: botChatMessage.botMessageModel?.time
                                tvDateTime.text = botChatTime ?: ""

                                break
                            }

                        }

                    }

                    vIsOnline.visibility = View.VISIBLE
                }
                2 -> {
                    llAvatar.visibility = View.GONE
                    ivAvatar.setImageResource(R.drawable.ic_lessons_messages)
                    tvName.text = model.getBaseName()
                    tvMessage.text = model.groupsCount.toString()
                    tvDateTime.text = ""
                    vIsOnline.visibility = View.GONE
                }
                else -> {
                    val img = if (!model.members.isNullOrEmpty()) {
                        model.members.first()?.user?.image?.signatures?.firstOrNull()
                    } else null
                    if (img != null) {
                        llAvatar.visibility = View.GONE
                        Picasso.get()
                            .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                            // .error(R.drawable.ic_filter_dield_example)
                            .into(ivAvatar)
                    } else {
                        if (!model.members.isNullOrEmpty()) {
                            val colorRandomPosition = model.members.first()?.user?.name.hashCode() % 6
                            val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
//                            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ColorUtil.getColorFromHash(model.members.first().user.name!!), BlendModeCompat.SRC_ATOP))

                            tvAvatar.text = model.members.first()?.getAvatarName()
                            llAvatar.visibility = View.VISIBLE
                        } else {
                            llAvatar.visibility = View.GONE
                        }
                    }

                    tvName.text = model.getBaseName()

                    tvMessage.text = if (!model.messages.isNullOrEmpty()) {
                        HtmlCompat.fromHtml(model.messages.last()!!.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    } else ""

                    tvDateTime.text = if (!model.messages.isNullOrEmpty()) {
                        TimeUtils.getLastMessageTime(model.messages.last()!!.timecreated)
                    } else ""

                    if (!model.members.isNullOrEmpty()) {
                        vIsOnline.visibility = model.members.first()!!.isonline.visibility()
                    } else {
                        vIsOnline.visibility = View.GONE
                    }
                }
            }

            if (model.unreadcount > 0) {
                tvNoReadCount.visibility = View.VISIBLE
                tvNoReadCount.text = model.unreadcount.toString()
            } else {
                tvNoReadCount.visibility = View.GONE
                tvNoReadCount.text = ""
            }

            mainLayout.setOnClickListener {
                onClickListener?.onClick(Triple(model.baseChatType, model, adapterPosition))
            }

            vPaddingTop.visibility = if (firstItem) View.VISIBLE else View.GONE
            vDivider.visibility = if (model.baseChatType == 2 && items.size == filteredItems.size) View.VISIBLE else View.GONE
        }
    }

    fun updateFilter(filterValue: String) {
        val fv = items.filter { t -> t.getBaseName().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase(Locale.ROOT))}
        filteredItems.clear()
        filteredItems.addAll(fv)

        notifyDataSetChanged()

//        delegate?.itemsUpdated(filteredItems)
    }

    class OnClickListener(val clickListener: (item: Triple<Int, Conversation, Int>) -> Unit) {
        fun onClick(item: Triple<Int, Conversation, Int>) = clickListener(item)
    }
}