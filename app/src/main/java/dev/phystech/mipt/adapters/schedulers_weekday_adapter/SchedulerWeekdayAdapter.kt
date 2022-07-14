package dev.phystech.mipt.adapters.schedulers_weekday_adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shuhart.stickyheader.StickyAdapter
import dev.phystech.mipt.R
import dev.phystech.mipt.adapters.scheduler_filtred.SchedulerAdapterModelHelper
import dev.phystech.mipt.adapters.schedulers_weekday_adapter.SchedulerWeekdayAdapterItemType.*
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.utils.exception.UnsupportedViewTypeException

class SchedulerWeekdayAdapter(val resources: Resources): StickyAdapter<SchedulerWeekdayAdapter.HeaderViewHolder, SchedulerWeekdayAdapter.ViewHolder>() {

    private val items: ArrayList<SchedulerWeekdayAdapterItem> = arrayListOf()
    private val headers: ArrayList<SchedulerWeekdayAdapterItem> = arrayListOf()

    interface Delegate {
        fun onSelect(model: ScheduleItem)
    }

    var delegate: Delegate? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val type = SchedulerWeekdayAdapterItemType.getByOrdinal(viewType) ?: throw UnsupportedViewTypeException()
        when (type) {
            Header -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_event_header, parent, false)
                return HeaderViewHolder(view)
            }
            Item -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_scheduler, parent, false)
                return ItemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position].getType().ordinal


    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        val item = items[itemPosition]
        if (item is SchedulerWeekdayAdapterItem.Header) return itemPosition
        else {
            val header = items.take(itemPosition).lastOrNull { it is SchedulerWeekdayAdapterItem.Header } ?: return 0
            return items.indexOf(header)
        }
    }

    override fun onBindHeaderViewHolder(holder: HeaderViewHolder?, headerPosition: Int) {
        holder?.bind(items[headerPosition])
        holder?.tvTitle?.text = (items[headerPosition] as? SchedulerWeekdayAdapterItem.Header)?.title
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup?): HeaderViewHolder {
        val view = LayoutInflater.from(parent?.context)
            .inflate(R.layout.item_event_header, parent, false)
        return HeaderViewHolder(view)
    }


    fun setItems(items: List<ScheduleItem>) {
        val weekdayItems = items.sortedBy { v -> v.day }.map { v -> SchedulerWeekdayAdapterItem.Item(v) }

        this.items.clear()
        this.items.addAll(weekdayItems)

        val weekday = resources.getStringArray(R.array.weeks)

        var i = 0
        while (i < this.items.size) {
            val item = this.items[i]
            if (i == 0) {
                item as SchedulerWeekdayAdapterItem.Item
                val header = SchedulerWeekdayAdapterItem.Header(weekday[item.model.day])
                this.items.add(i++, header)
                headers.add(header)

            } else {
                val prev = this.items[i - 1]
                if (prev is SchedulerWeekdayAdapterItem.Item && item is SchedulerWeekdayAdapterItem.Item) {
                    if (prev.model.day != item.model.day) {
                        val header = SchedulerWeekdayAdapterItem.Header(weekday[item.model.day])
                        this.items.add(i++, header)
                        headers.add(header)
                    }
                }
            }


            ++i
        }

        notifyDataSetChanged()
    }





    abstract class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(model: SchedulerWeekdayAdapterItem)
    }

    inner class ItemViewHolder(view: View): ViewHolder(view) {
        lateinit var model: SchedulerWeekdayAdapterItem.Item

        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvAuditory: TextView = view.findViewById(R.id.tvAuditory)
        val scheduleType: TextView = view.findViewById(R.id.ScheduleType)
        val tvLector: TextView = view.findViewById(R.id.tvLector)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvSchedulerTime: TextView = view.findViewById(R.id.tvSchedulerTime)

        init {
            view.setOnClickListener {
                delegate?.onSelect(model.model)
            }
        }

        override fun bind(model: SchedulerWeekdayAdapterItem) {
            model as SchedulerWeekdayAdapterItem.Item

            this.model = model

            model.model.let { viewModel ->
                tvTitle.text = viewModel.name
                tvAuditory.text = viewModel.auditorium.mapNotNull { v -> v.name }.joinToString(separator = "\n")
                scheduleType.text = viewModel.type
                tvLector.text = viewModel.teachers?.mapNotNull { v -> v.name }?.joinToString("\n")
                tvSchedulerTime.text = viewModel.buildTime()
            }
        }
    }

    inner class HeaderViewHolder(view: View): ViewHolder(view) {
        lateinit var model: SchedulerWeekdayAdapterItem.Header
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)

        override fun bind(model: SchedulerWeekdayAdapterItem) {
            model as SchedulerWeekdayAdapterItem.Header
            this.model = model

            tvTitle.text = model.title
        }
    }


    private fun ScheduleItem.buildTime(): CharSequence? = "$startTime - $endTime"

}
