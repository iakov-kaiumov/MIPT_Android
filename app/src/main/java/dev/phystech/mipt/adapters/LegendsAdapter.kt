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

class LegendsAdapter() :
        RecyclerView.Adapter<LegendsAdapter.ViewHolder>() {

    var items: ArrayList<LegendModel> = ArrayList()

    interface Delegate {
        fun onSelect(view: View, withModel: LegendModel)
    }

    var delegate: Delegate? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_legend, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.transitionName = "legend$position"
        viewHolder.bind(items[position])
    }

    override fun getItemCount() = items.size


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private lateinit var model: LegendModel

        val tvSubTitle: TextView = view.findViewById(R.id.tvSubTitle)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val ivBackground: ImageView = view.findViewById(R.id.ivBackground)

        init {
            view.setOnClickListener {
                delegate?.onSelect(view, model)
            }
        }


        fun bind(model: LegendModel) {
            this.model = model

            tvSubTitle.text = model.place ?: ""
            tvTitle.text = model.title ?: ""

            model.imageUrl?.let { image ->
                Picasso.get().load(image)
                    .into(ivBackground)
            }

        }

    }

}

