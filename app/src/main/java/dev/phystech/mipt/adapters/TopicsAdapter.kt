package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R


class TopicsAdapter(val arrayList: ArrayList<String>) : RecyclerView.Adapter<TopicsAdapter.ViewHolder>() {

    interface Delegate {
        fun onTopicSelect(topic: String)
    }

    var delegate: Delegate? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text = arrayList[position]
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvTitle)

        init {
            itemView.setOnClickListener {
                delegate?.onTopicSelect(title.text.toString())
            }
        }

    }


}
