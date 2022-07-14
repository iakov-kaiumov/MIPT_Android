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

class PlaceImagesAdapter(var items: ArrayList<String> = ArrayList()): RecyclerView.Adapter<PlaceImagesAdapter.ViewHolder>() {

    var width: Int? = null

    interface Delegate {
        fun onSelect(model: String)
    }

    var delegate: Delegate? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_image, parent, false)

        val v = RecyclerView.LayoutParams(view.layoutParams)
        width?.let {
            v.width = (it * 0.9).toInt()
        }
        view.layoutParams = v

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(items[position])
        holder.setStartSpace(position == 0)
        holder.setEndSpace(position == itemCount - 1)
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        lateinit var model: String

        private val viewLeftSpace: View = view.findViewById(R.id.viewLeftSpace)
        private val viewRightSpace: View = view.findViewById(R.id.viewRightSpace)
        private val image: ImageView = view.findViewById(R.id.image)

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bindView(model: String) {
            this.model = model

//            val url = NetworkUtils.getImageUrl(model.id, model.dir, model.path)
            Picasso.get()
                .load(model)
                .into(image)
        }

        fun setStartSpace(visibility: Boolean) {
            viewLeftSpace.visibility = visibility.visibility()
        }

        fun setEndSpace(visibility: Boolean) {
            viewRightSpace.visibility = visibility.visibility()
        }

    }

}