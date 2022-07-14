package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R

class SelectorAdapter<T>(var items: List<T> = emptyList()): RecyclerView.Adapter<SelectorAdapter<T>.ViewHolder>() {

    var delegate: Delegate<T>? = null

    interface Delegate<T> {
        fun onClick(item: T)
    }

    inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var item: T? = null
        init {
            view.setOnClickListener {
                item?.let { delegate?.onClick(it) }
            }
        }

        fun bind(item: T) {
            this.item = item
            (view as? TextView)?.let { tv ->
                tv.text = item.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selector, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

}