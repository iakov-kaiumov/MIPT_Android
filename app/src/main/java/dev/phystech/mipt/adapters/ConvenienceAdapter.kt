package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.models.api.Tag
import dev.phystech.mipt.utils.visibility

class ConvenienceAdapter(var items: ArrayList<Tag> = ArrayList()): RecyclerView.Adapter<ConvenienceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_convenience, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(items[position])
        holder.setRightSeparator(position != itemCount - 1)
        holder.itemIsFirst(position == 0)
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private lateinit var model: Tag

        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val image: ImageView = view.findViewById(R.id.image)
        private val rightSeparator: View = view.findViewById(R.id.rightSeparator)
        private val marginLeft: View = view.findViewById(R.id.marginLeft)
        private val marginRight: View = view.findViewById(R.id.marginRight)

        fun bindView(model: Tag) {
            this.model = model

            tvTitle.text = model.name
            model.imageUrl?.let { imageUrl ->
                Picasso.get()
                    .load(imageUrl)
                    .into(image)
            }
        }

        fun itemIsFirst(isFirst: Boolean) {
            marginLeft.visibility = isFirst.visibility()
        }

        fun setRightSeparator(visibility: Boolean) {
            rightSeparator.visibility = visibility.visibility()
            marginRight.visibility = (!visibility).visibility()
        }
    }

}