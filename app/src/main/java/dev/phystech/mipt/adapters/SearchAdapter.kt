package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.LegendModel

class SearchAdapter: RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    var items: ArrayList<LegendModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_search, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private lateinit var model: LegendModel

        private val ivImage: ImageView = view.findViewById(R.id.ivImage)
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvPlaceType: TextView = view.findViewById(R.id.tvPlaceType)
        private val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        fun bind(model: LegendModel) {
            tvTitle.text = model.title
            tvPlaceType.text = model.place
//            tvDescription.text = model.

            model.imageUrl?.let {
                Picasso.get()
                    .load(it)
                    .into(ivImage)
            }


        }
    }

}