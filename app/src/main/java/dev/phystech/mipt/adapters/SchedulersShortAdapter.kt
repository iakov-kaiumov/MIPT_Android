package dev.phystech.mipt.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_filtred.SchedulerAdapterModelHelper
import dev.phystech.mipt.adapters.scheduler_filtred.SchedulerFiltredListAdapterModel
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.ScheduleItem

class SchedulersShortAdapter: RecyclerView.Adapter<SchedulersShortAdapter.ViewHolder>() {

    interface Delegate {
        fun onSelect(item: ScheduleItem)
    }

    var delegate: Delegate? = null
    var items: List<ScheduleItem> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scheduler, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size



    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAuditory: TextView = view.findViewById(R.id.tvAuditory)
        val ScheduleType: TextView = view.findViewById(R.id.ScheduleType)
        val tvLector: TextView = view.findViewById(R.id.tvLector)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvSchedulerTime: TextView = view.findViewById(R.id.tvSchedulerTime)


        private var modelHelper: ScheduleItem? = null

        init {
            view.setOnClickListener {
                modelHelper?.let { model ->
                    delegate?.onSelect(model)
                }
            }
        }

        fun bind(item: ScheduleItem) {
            modelHelper = item
            val model = modelHelper ?: return

            tvTitle.text = model.name
            tvAuditory.text =
                model.auditorium.mapNotNull { v -> v.name }.joinToString(separator = "\n")
            tvLector.text = model.teachers?.mapNotNull { v -> v.name }?.joinToString("\n")
            tvSchedulerTime.text = buildTime(
                tvSchedulerTime.context.resources,
                model.day,
                model.startTime,
                model.endTime
            )
            ScheduleType.text = typeName(ScheduleType.context, model.type)

        }

        fun buildTime(resource: Resources, day: Int?, startTime: String, endTime: String): String? {
            val dayIndex = day?.minus(1) ?: 0
            val dayShort = resource.getStringArray(R.array.week_short).getOrNull(dayIndex) ?: ""

            return "${dayShort} $startTime - $endTime".trim()
        }

        private fun typeName(context: Context, type: String?): String {
            return when (type) {
                "LAB" -> context.resources.getString(R.string.schedule_item_lab)
                "SEM" -> context.resources.getString(R.string.schedule_item_sem)
                "LEC" -> context.resources.getString(R.string.schedule_item_lec)
                else -> String.empty()
            }
        }
    }


}