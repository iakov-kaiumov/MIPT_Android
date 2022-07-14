package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.UserAbstract
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.ui.utils.AvatarColor
import io.realm.mongodb.User

class UsersAdapter: RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: UserAbstract)
    }

    var delegate: Delegate? = null
    val items: MutableList<UserAbstract> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size



    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAvatarDescription: TextView = view.findViewById(R.id.tvAvatarDescription)
        val image: ImageView = view.findViewById(R.id.image)

        private lateinit var model: UserAbstract

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: UserAbstract) {
            this.model = model

            tvName.text = model.name
            tvAvatarDescription.text = model.name?.split(" ")?.mapNotNull { v -> v.firstOrNull() }?.take(2)?.joinToString(separator = "") { v -> v.toString() }
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