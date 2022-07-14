package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R

class NotificationTimeListAdapter(val items: ArrayList<String>, var selectedPosition: Int = 0) :
        RecyclerView.Adapter<NotificationTimeListAdapter.ViewHolder>() {

    interface Delegate {
        fun selectItem(withPosition: Int)
    }

    var delegate: Delegate? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCheck: ImageView = view.findViewById(R.id.ivCheck)
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val number: Int = 0

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_notification_time, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        if (selectedPosition == position) {
            viewHolder.itemView.isSelected = true
            viewHolder.ivCheck.visibility = View.VISIBLE
        } else {
            viewHolder.itemView.isSelected = false
            viewHolder.ivCheck.visibility = View.INVISIBLE
        }

        viewHolder.tvValue.text = items[position]

        viewHolder.itemView.setOnClickListener { v ->
            if (selectedPosition >= 0) notifyItemChanged(selectedPosition)
            selectedPosition = viewHolder.adapterPosition
            notifyItemChanged(selectedPosition)

            delegate?.selectItem(position)
        }
    }

    override fun getItemCount() = items.size
}

