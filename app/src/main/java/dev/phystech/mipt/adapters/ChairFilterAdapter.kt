package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.ChairDataResponseModel
import dev.phystech.mipt.network.ApiClient
import dev.phystech.mipt.network.NetworkUtils
import io.reactivex.rxjava3.annotations.Nullable

class ChairFilterAdapter(val type: Type): RecyclerView.Adapter<ChairFilterAdapter.ViewHolder>() {

    var disabled: MutableSet<String>? = null
    var items: MutableList<ChairDataResponseModel.PaginatorItem> = mutableListOf()
    var delegate: Delegate? = null

    interface Delegate {
        fun onChecked(item: ChairDataResponseModel.PaginatorItem, isChecked: Boolean, type: Type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter_field, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val switch: SwitchMaterial = view.findViewById(R.id.switchBreak)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)

        var item: ChairDataResponseModel.PaginatorItem? = null

        init {
            switch.setOnCheckedChangeListener { _, isChecked ->
                item?.let { item ->
                    delegate?.onChecked(item, isChecked, type)
                }
            }
        }

        fun bind(item: ChairDataResponseModel.PaginatorItem) {
            this.item = item

            tvValue.text = item.name
            val img = item.cover?.signatures?.firstOrNull()

            val imageUrl = NetworkUtils.getImageUrl(img?.id, img?.dir, img?.path)
            Picasso.get()
                .load(imageUrl)
                .error(R.drawable.ic_filter_dield_example)
                .into(ivIcon)

            if (disabled?.contains(item.id) == true) {
                switch.isChecked = false
                disabled?.remove(item.id)
            }
        }
    }


    enum class Type {
        Chair,
        Others,
        Official
    }
}