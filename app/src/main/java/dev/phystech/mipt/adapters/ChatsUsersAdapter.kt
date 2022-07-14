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
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils

class ChatsUsersAdapter: RecyclerView.Adapter<ChatsUsersAdapter.ViewHolder>() {

    var items: ArrayList<UsersResponseModel.UserInfoModel> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_user, parent, false)

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

        fun bind(model: UsersResponseModel.UserInfoModel) {

            val img = if (model.image != null && !model.image!!.signatures.isNullOrEmpty()) {
                model.image!!.signatures.firstOrNull()
            } else null
            if (img != null) {
                llAvatar.visibility = View.GONE
                Picasso.get()
                    .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                    // .error(R.drawable.ic_filter_dield_example)
                    .into(ivAvatar)
            } else {

                val colorRandomPosition = model.name.hashCode() % 6
                val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))

//                llAvatar.getBackground().setColorFilter(
//                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
//                        ColorUtil.getColorFromHash(model.name!!), BlendModeCompat.SRC_ATOP))

                tvAvatar.text = model.getAvatarName()
                llAvatar.visibility = View.VISIBLE
            }

            tvName.text = model.getTrimName()

            tvDescription.text = if (!model.roles.isNullOrEmpty()) ChatUtils.getUserRoles(model.roles!!) else ""

            mainLayout.setOnClickListener {
                onClickListener?.onClick(model)
            }
        }
    }

    class OnClickListener(val clickListener: (item: UsersResponseModel.UserInfoModel) -> Unit) {
        fun onClick(item: UsersResponseModel.UserInfoModel) = clickListener(item)
    }
}