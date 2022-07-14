package dev.phystech.mipt.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.models.SchedulerIndex

class SectionSchedulerAdapter(resources: Resources): RecyclerView.Adapter<SectionSchedulerAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(model: SchedulerIndex)
    }
    var delegate: Delegate? = null

    val items: ArrayList<SchedulerIndex> = arrayListOf()
    private val weeks = resources.getStringArray(R.array.week_short)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section_scheduler, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size




    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        lateinit var model: SchedulerIndex

        private val tvScheduler: TextView = view.findViewById(R.id.tvScheduler)
        private val tvPlace: TextView = view.findViewById(R.id.tvPlace)

        init {
            view.setOnClickListener {
                delegate?.onSelect(model)
            }
        }

        fun bind(model: SchedulerIndex) {
            this.model = model

            val weekDay = weeks[(model.day ?: 1) - 1]
            val start = model.startTime
            val end = model.endTime

            tvScheduler.text = "$weekDay $start - $end"
            tvPlace.text = model.place
        }
    }

}