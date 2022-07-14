package dev.phystech.mipt.adapters.scheduler_event

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import dev.phystech.mipt.R
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.utils.visibility
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class SchedulerEventsAdapter: RecyclerView.Adapter<SchedulerEventsAdapter.ViewHolderEvents>() {

    interface Delegate {
        fun onSelectEvent(id: Int)
    }

    var delegate: Delegate? = null
    var context: Context? = null
    val items: ArrayList<SchedulerEventVHModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedulerEventsAdapter.ViewHolderEvents {
        context = parent.context
        return when (viewType) {
            SchedulerEventType.Item.typeValue -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_event, parent, false)
                ViewHolderItem(view)
            }

            SchedulerEventType.Title.typeValue -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_event_header, parent, false)

                ViewHolderTitle(view)
            }

            else -> throw Exception("Unknown model")
        }
    }

    override fun onBindViewHolder(holder: SchedulerEventsAdapter.ViewHolderEvents, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SchedulerEventTitleModel -> SchedulerEventType.Title.typeValue
            is SchedulerEventModel -> SchedulerEventType.Item.typeValue
        }
    }


    fun setItems(map: Map<Date?, List<EventModel>>) {
        items.clear()
        map.forEach { date, models ->
            date?.let { items.add(SchedulerEventTitleModel(date)) }
            models.forEachIndexed { i, model ->
                items.add(SchedulerEventModel(model, i == models.size - 1))
            }
        }
        notifyDataSetChanged()
    }



    abstract class ViewHolderEvents(view: View): RecyclerView.ViewHolder(view){
        abstract fun bind(model: SchedulerEventVHModel)
    }

    /** @see R.layout.item_event_header */
    class ViewHolderTitle(view: View): ViewHolderEvents(view) {

        lateinit var model: SchedulerEventTitleModel

        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)

        override fun bind(model: SchedulerEventVHModel) {
            this.model = model as SchedulerEventTitleModel

            tvTitle.text = model.text
        }
    }

    /** @see R.layout.item_event */
    inner class ViewHolderItem(@LayoutRes() view: View): ViewHolderEvents(view) {

        lateinit var model: SchedulerEventModel

        val typeLine: View = view.findViewById(R.id.typeLine)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvTeacher: TextView = view.findViewById(R.id.tvTeacher)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvAuditory: TextView = view.findViewById(R.id.tvAuditory)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val bottomLine: View = view.findViewById(R.id.bottomLine)

        init {
            view.setOnClickListener {
                delegate?.onSelectEvent(model.id)
            }
        }

        override fun bind(model: SchedulerEventVHModel) {
            this.model = model as SchedulerEventModel

            var type: String? = null
            model.type?.let {
                type = context?.resources?.getString(it)
            }

            tvTitle.text = model.title
            tvTeacher.text = model.teacher
            tvType.text = type
            tvAuditory.text = model.auditory
            tvTime.text = model.time

            tvTitle.visibility = model.title.isNullOrEmpty().not().visibility(false)
            tvTeacher.visibility = model.teacher.isNullOrEmpty().not().visibility(false)
            tvType.visibility = type.isNullOrEmpty().not().visibility(false)
            tvAuditory.visibility = model.auditory.isNullOrEmpty().not().visibility(false)
            tvTime.visibility = model.time.isNullOrEmpty().not().visibility(false)
            bottomLine.visibility = model.isLast.not().visibility(false)

            Picasso.get()
        }
    }

}