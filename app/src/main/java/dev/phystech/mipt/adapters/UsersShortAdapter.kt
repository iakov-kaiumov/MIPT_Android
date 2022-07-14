package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.models.api.UsersResponseModel
import dev.phystech.mipt.ui.utils.AvatarColor
import dev.phystech.mipt.utils.visibility

class UsersShortAdapter : RecyclerView.Adapter<UsersShortAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: UsersResponseModel.UserInfoModel)
    }

    var delegate: Delegate? = null
    private val items: MutableList<UsersResponseModel.UserInfoModel> = mutableListOf()
    private val allItems: MutableList<UsersResponseModel.UserInfoModel> = mutableListOf()
    var lastFilter: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setFilter(filter: String?) {
        lastFilter = filter
        val oldCount = items.size

        if (filter == null) {
            items.clear()
            items.addAll(allItems)
        } else {

            val filtered = allItems.filter { v ->
                v.name?.contains(filter, true) == true || v.email?.contains(
                    filter,
                    true
                ) == true
            }

            items.clear()
            items.addAll(filtered)
        }

        notifyItemRangeRemoved(0, oldCount)
        notifyItemRangeInserted(0, items.size)
    }

    fun setItems(newItems: List<UsersResponseModel.UserInfoModel>) {
        allItems.clear()
        allItems.addAll(newItems)
        setFilter(lastFilter)
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvAvatarDescription: TextView = view.findViewById(R.id.tvAvatarDescription)
        val image: ImageView = view.findViewById(R.id.image)

        private lateinit var model: UsersResponseModel.UserInfoModel

        init {
            tvEmail.visibility = true.visibility()
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: UsersResponseModel.UserInfoModel) {
            this.model = model

            tvName.text = model.name
            tvEmail.text = model.email
            tvAvatarDescription.text =
                model.name?.split(" ")?.mapNotNull { v -> v.firstOrNull() }?.take(2)
                    ?.joinToString(separator = "") { v -> v.toString() }
            model.image?.signatures?.firstOrNull().let { img ->
                if (img == null) {
                    val colorRandomPosition = model.name.hashCode() % 6
                    val avatarColor = AvatarColor.getByPosition(colorRandomPosition)
                    image.setImageResource(avatarColor.colorResId)
                } else {
                    val imgUrl = NetworkUtils.getImageUrl(img.id, img.dir, img.path)
                    Picasso.get()
                        .load(imgUrl)
                        .into(image)
                }
            }
        }
    }

}