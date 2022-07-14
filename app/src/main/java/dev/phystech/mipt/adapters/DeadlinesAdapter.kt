package dev.phystech.mipt.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import java.text.SimpleDateFormat

class DeadlinesAdapter: RecyclerView.Adapter<DeadlinesAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: EventModel)
    }

    var delegate: Delegate? = null
    val items: ArrayList<EventModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deadline, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

        lateinit var model: EventModel

        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: EventModel) {
            this.model = model

            var timeValue = ""
            serverDateFormat.parse(model.startTime)?.let {
                timeValue = displayDateFormat.format(it)
            }


            tvName.text = model.name
            tvDate.text = timeValue
            tvDescription.text = model.notes
        }
    }

    companion object {
        val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val displayDateFormat = SimpleDateFormat("dd MMM, HH:mm")
    }

}