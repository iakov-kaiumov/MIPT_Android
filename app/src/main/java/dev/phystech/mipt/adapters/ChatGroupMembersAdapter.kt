package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.Application
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils
import dev.phystech.mipt.utils.visibility

class ChatGroupMembersAdapter: RecyclerView.Adapter<ChatGroupMembersAdapter.ViewHolder>() {

    var items: ArrayList<Member> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val llAvatar: LinearLayout = view.findViewById(R.id.llAvatar)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        fun bind(model: Member) {

            val img = if (model.user?.image != null && !model.user?.image!!.signatures.isNullOrEmpty()) {
                model.user?.image!!.signatures.firstOrNull()
            } else null
            if (img != null) {
                llAvatar.visibility = View.GONE
                Picasso.get()
                    .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                    // .error(R.drawable.ic_filter_dield_example)
                    .into(ivAvatar)
            } else {

                val colorRandomPosition = model.user?.name.hashCode() % 6
                val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))
//                llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ColorUtil.getColorFromHash(model.user.name!!), BlendModeCompat.SRC_ATOP))

                tvAvatar.text = model.getAvatarName()
                llAvatar.visibility = View.VISIBLE
            }

            tvName.text = model.user?.getTrimName()

            tvDescription.text = if (!model.user?.roles.isNullOrEmpty()) ChatUtils.getUserRoles(model.user?.roles!!) else ""
            tvDescription.visibility = (!model.user?.roles.isNullOrEmpty()).visibility()

            mainLayout.setOnClickListener {
                onClickListener?.onClick(model)
            }
        }
    }

    class OnClickListener(val clickListener: (item: Member) -> Unit) {
        fun onClick(item: Member) = clickListener(item)
    }
}