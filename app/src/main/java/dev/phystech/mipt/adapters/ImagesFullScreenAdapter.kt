package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.network.NetworkUtils
import dev.phystech.mipt.utils.visibility

class ImagesFullScreenAdapter(var items: ArrayList<String> = ArrayList()): RecyclerView.Adapter<ImagesFullScreenAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fullscreen_image, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        lateinit var model: String

        private val image: ImageView = view.findViewById(R.id.image)

        fun bindView(model: String) {
            this.model = model

//            val url = NetworkUtils.getImageUrl(model.id, model.dir, model.path)
            Picasso.get()
                .load(model)
                .into(image)
        }

    }

}