package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.utils.visibility

class ChatAnswersAdapter(val items: ArrayList<Pair<String, String>>): RecyclerView.Adapter<ChatAnswersAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: Pair<String, String>)
    }

    var delegate: Delegate? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_multiply_message_variant, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.bottomLine.visibility = (position != items.size - 1).visibility()
    }

    override fun getItemCount(): Int = items.size



    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val tvValue: TextView = view.findViewById(R.id.tvValue)
        val bottomLine: View = view.findViewById(R.id.bottomLine)

        lateinit var model: Pair<String, String>

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: Pair<String, String>) {
            this.model = model

            tvValue.text = model.first
        }
    }

}