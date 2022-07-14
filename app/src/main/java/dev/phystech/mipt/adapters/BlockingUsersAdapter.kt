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
import dev.phystech.mipt.models.api.BlockUserElement
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.ChatUtils
import io.realm.RealmList
import java.util.*
import kotlin.collections.ArrayList

class BlockingUsersAdapter: RecyclerView.Adapter<BlockingUsersAdapter.ViewHolder>() {

    var items: ArrayList<BlockUserElement> = arrayListOf()
    var filteredItems: ArrayList<BlockUserElement> = arrayListOf()
    var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position])
    }

    override fun getItemCount(): Int = filteredItems.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val mainLayout: ConstraintLayout = view.findViewById(R.id.mainLayout)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val llAvatar: LinearLayout = view.findViewById(R.id.llAvatar)
        val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val ivUnblockUser: ImageView = view.findViewById(R.id.ivUnblockUser)

        fun bind(model: BlockUserElement) {

            val img = if (model.user.image != null && !model.user.image?.signatures.isNullOrEmpty()) {
                model.user.image?.signatures?.firstOrNull()
            } else null
            if (img != null) {
                llAvatar.visibility = View.GONE
                Picasso.get()
                    .load(NetworkUtils.getImageUrl(img.id, img.dir, img.path))
                    // .error(R.drawable.ic_filter_dield_example)
                    .into(ivAvatar)
            } else {

                val colorRandomPosition = model.user.name.hashCode() % 6
                val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                llAvatar.getBackground().setColorFilter(BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                    Application.context.getColor(avatarColor.colorResId), BlendModeCompat.SRC_ATOP))

//                llAvatar.getBackground().setColorFilter(
//                    BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
//                        ColorUtil.getColorFromHash(model.user.name!!), BlendModeCompat.SRC_ATOP))

                tvAvatar.text = model.getAvatarName()
                llAvatar.visibility = View.VISIBLE
            }

            tvName.text = model.user.getTrimName()

            tvDescription.text = if (!model.user.roles.isNullOrEmpty()) ChatUtils.getUserRoles(model.user.roles?: RealmList<String>()) else ""

            mainLayout.setOnClickListener {
                onClickListener?.onClick(Pair(model, 0))
            }

            ivUnblockUser.visibility = View.VISIBLE
            ivUnblockUser.setOnClickListener {
                onClickListener?.onClick(Pair(model, 1))
            }
        }
    }

    fun updateFilter(filterValue: String) {
        val fv = items.filter { t -> t.user.getTrimName().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase(Locale.ROOT))}
        filteredItems.clear()
        filteredItems.addAll(fv)
        notifyDataSetChanged()
    }

    class OnClickListener(val clickListener: (item: Pair<BlockUserElement, Int>) -> Unit) {
        fun onClick(item: Pair<BlockUserElement, Int>) = clickListener(item)
        // mode: 0 - open, 1 - delete
    }
}