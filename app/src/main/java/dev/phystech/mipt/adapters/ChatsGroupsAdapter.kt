package dev.phystech.mipt.adapters

import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
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
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.Conversation
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import java.util.*
import kotlin.collections.ArrayList

class ChatsGroupsAdapter: RecyclerView.Adapter<ChatsGroupsAdapter.ViewHolder>() {

    var items: ArrayList<Conversation> = arrayListOf()
    var filteredItems: ArrayList<Conversation> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_group, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position], if (position == 0) true else false)
    }

    override fun getItemCount(): Int = filteredItems.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        val llAvatar: LinearLayout = view.findViewById(R.id.llAvatar)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val tvDateTime: TextView = view.findViewById(R.id.tvDateTime)
        val tvNoReadCount: TextView = view.findViewById(R.id.tvNoReadCount)

        val vPaddingTop: View = view.findViewById(R.id.vPaddingTop)

        fun bind(model: Conversation, firstItem: Boolean) {


            val colorRandomPosition = model.name.hashCode() % 6
            val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
//            llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ColorUtil.getColorFromHash(model.name), BlendModeCompat.SRC_ATOP))

            tvAvatar.text = model.getAvatarName()

            tvName.text = model.name

            tvMessage.text = if (!model.messages.isNullOrEmpty()) {
                HtmlCompat.fromHtml(model.messages.last()!!.text, HtmlCompat.FROM_HTML_MODE_LEGACY)
            } else ""

            tvDateTime.text = if (!model.messages.isNullOrEmpty()) {
                TimeUtils.getLastMessageTime(model.messages.last()!!.timecreated)
            } else ""

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
        }
    }

    fun updateFilter(filterValue: String) {
        val fv = items.filter { t -> t.name?.toLowerCase(Locale.ROOT)!!.contains(filterValue.toLowerCase(Locale.ROOT))}
        filteredItems.clear()
        filteredItems.addAll(fv)
        notifyDataSetChanged()
    }

    class OnClickListener(val clickListener: (item: Triple<Int, Conversation, Int>) -> Unit) {
        fun onClick(item: Triple<Int, Conversation, Int>) = clickListener(item)
    }
}