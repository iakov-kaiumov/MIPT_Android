package dev.phystech.mipt.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import edu.phystech.iag.kaiumov.shedule.utils.ColorUtil
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import dev.phystech.mipt.R
import dev.phystech.mipt.base.utils.empty
import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerListShortItem
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.ui.utils.ContextMenuRegistrator
import dev.phystech.mipt.utils.dpToPx
import dev.phystech.mipt.utils.visibility
import dev.phystech.mipt.view.LinearLayoutTyped
import kotlin.math.min


class ClassesAdapter(
    private val day: Int,
    private val showSpaces: Boolean,
    classes: List<SchedulerListShortItem>,
    val contextMenuRegistrator: ContextMenuRegistrator
) : RecyclerView.Adapter<ClassesAdapter.ViewHolder>() {

    interface Delegate {
        fun onClick(item: ScheduleItem)
        fun onClick(item: EventModel)
    }

    var delegate: Delegate? = null
    val factory = UserRepository.shared.createSchedulersColorFactory()

    /**
     * Filtered list with only the classes for the given day
     */
    private val data: List<SchedulerListShortItem> = classes.filter { it.day == day || it.day + 7 == day }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflater.inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(v)
    }

    inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set item view
        val item = data[position]
        val context = holder.view.context

        holder.scheduleMainLayout.tag = item.id
        holder.scheduleMainLayout.model = item

        val minHeight = 90.dpToPx(context.resources.displayMetrics)
        val maxHeight = (90 * 2).dpToPx(context.resources.displayMetrics)

        // Set time layout background
        val dr = ContextCompat.getDrawable(context, ColorUtil.getBackgroundDrawable(item.type)) as GradientDrawable

        // Set time field text color
        if (item is EventModel) {
            val color = ColorStateList.valueOf(factory.getColorByType(item.type))
            dr.setStroke(2.dpToPx(), ColorStateList.valueOf(factory.getColorByType(item.type)))
            dr.setColor(android.R.color.white)
            holder.startTime.setTextColor(color)
            holder.endTime.setTextColor(color)
            holder.dot.imageTintList = color
            holder.timeLine.backgroundTintList = color
        } else {
            val textColor = ContextCompat.getColor(context, ColorUtil.getTextColor(item.type))
            holder.startTime.setTextColor(textColor)
            holder.endTime.setTextColor(textColor)
            holder.timeLine.backgroundTintList = ColorStateList.valueOf(textColor)
            holder.dot.setColorFilter(textColor)

            dr.setStroke(2.dpToPx(), ColorStateList.valueOf(Color.WHITE))

        }


        holder.time_layout.background = dr
        holder.time_layout.backgroundTintList = ColorStateList.valueOf(factory.getColorByType(item.type))

//        holder.time_layout.backgroundTintList = ColorStateList.valueOf()
        // Set text fields
        holder.name.text = item.name
        holder.prof.text = item.prof
        holder.place.text = item.place
        holder.place.visibility = (item.place?.isNullOrEmpty() ?: true).not().visibility()

        holder.startTime.text = item.getStartTimeDisplay()
        holder.endTime.text = item.getEndTimeDisplay()

        holder.startTime.visibility = item.allDay.not().visibility()
        holder.endTime.visibility = item.allDay.not().visibility()
        holder.timeLine.visibility = item.allDay.not().visibility()
        holder.constraintLayout.visibility = item.allDay.not().visibility()

        // Dot and selection layout
        holder.dot.visibility = View.INVISIBLE
        holder.selectionLayout.background = null
        holder.tvType.text = typeName(context, item.type)
        holder.tvType.visibility = holder.tvType.text.isNotEmpty().visibility()

        holder.scheduleMainLayout.measure(0, 0)

        holder.scheduleMainLayout.setOnClickListener {
            when (item) {
                is EventModel -> delegate?.onClick(item)
                is ScheduleItem -> delegate?.onClick(item)
            }
        }

        // Spaces
        if (showSpaces && position + 1 < data.size) {
            val length = maxOf(TimeUtils.length(item.getEndTimeDisplay(), data[position + 1].getStartTimeDisplay()) * 60, 0.0).toInt()
            if (length > 0) {
                val breakPrefix = context.resources.getString(R.string.break_prefix)
                holder.breakFrame.visibility = true.visibility()
                val minVal = length % 60
                val hourVal = length / 60
                var timeValue = ""

                if (hourVal > 0) {
                    context.resources.getQuantityString(R.plurals.hours, hourVal, hourVal).also {
                        timeValue = "$timeValue $it".trim()
                    }
                }

                if (minVal > 0) {
                    context.resources.getQuantityString(R.plurals.minutes, minVal, minVal).also {
                        timeValue = "$timeValue $it".trim()
                    }
                }

                if (timeValue.isNotBlank()) {
                    timeValue = "$breakPrefix $timeValue"
                }

                holder.tvBreakTitle.text = timeValue
                holder.tvBreakSubtitle.text = breakText(context, length)
                holder.ivBreakIcon.setImageResource(breakDrawable(length))
            } else {
                holder.breakFrame.visibility = false.visibility()
            }

        } else {
            holder.breakFrame.visibility = false.visibility()
        }

        if (item is EventModel) {
            holder.time_layout
        }

        // Set up actual item
        val day = TimeUtils.getCurrentDay()
        val time = TimeUtils.getCurrentTime()
        if (day == item.day && TimeUtils.compareTime(time, item.getStartTimeDisplay()) >= 0 &&
                TimeUtils.compareTime(time, item.getEndTimeDisplay()) <= 0) {
            // Change font
            holder.place.typeface = ResourcesCompat.getFont(context, R.font.sfprodisplay_regular)
            // Set selection color
//            holder.selectionLayout.setBackgroundColor(ContextCompat.getColor(context, ColorUtil.getBackgroundColor(item.type)))

            val typedValue = TypedValue()
            context.resources.getValue(R.dimen.selection_alpha, typedValue, true)
            holder.selectionLayout.alpha = typedValue.float
            // Move dot
            val dotPosition = TimeUtils.length(item.getStartTimeDisplay(), time) / item.length()
            val constraintSet = ConstraintSet()
            holder.dot.visibility = View.VISIBLE
            constraintSet.clone(holder.constraintLayout)
            constraintSet.setVerticalBias(R.id.dot, dotPosition.toFloat())
            constraintSet.applyTo(holder.constraintLayout)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long = position.toLong()



    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var rootLayout: LinearLayout  = view.findViewById(R.id.rootLayout)
        var selectionLayout: LinearLayout = view.findViewById(R.id.selectionLayout)
        var scheduleMainLayout: LinearLayoutTyped = view.findViewById(R.id.scheduleMainLayout)
        var time_layout: FrameLayout = view.findViewById(R.id.time_layout)
        var startTime: TextView = view.findViewById(R.id.startTime)
        var constraintLayout: ConstraintLayout = view.findViewById(R.id.constraintLayout)
        var timeLine: LinearLayout = view.findViewById(R.id.timeLine)
        var dot: ImageView = view.findViewById(R.id.dot)
        var endTime: TextView = view.findViewById(R.id.endTime)
        var prof: TextView = view.findViewById(R.id.prof)
        var name: TextView = view.findViewById(R.id.name)
        var place: TextView = view.findViewById(R.id.place)
//        var breakTextView: TextView = view.findViewById(R.id.breakTextView)
        var breakFrame: FrameLayout = view.findViewById(R.id.breakFrame)
        var ivBreakIcon: ImageView = view.findViewById(R.id.ivBreakIcon)
        var tvBreakTitle: TextView = view.findViewById(R.id.tvBreakTitle)
        var tvBreakSubtitle: TextView = view.findViewById(R.id.tvBreakSubtitle)
        var tvType: TextView = view.findViewById(R.id.tvType)

        init {
            contextMenuRegistrator.registerForContextMenu(scheduleMainLayout)
        }


    }



    private fun breakText(context: Context, length: Int): String {
        return when (length) {
            in 0..10 -> context.resources.getString(R.string.break_extra_short_comment)
            in 11..20 -> context.resources.getString(R.string.break_short_comment)
            in 21..40 -> context.resources.getString(R.string.break_medium_comment)
            else -> context.resources.getString(R.string.break_long_comment)
        }
    }

    private fun typeName(context: Context, type: String): String {
        return when (type) {
            "LAB" -> context.resources.getString(R.string.schedule_item_lab)
            "SEM" -> context.resources.getString(R.string.schedule_item_sem)
            "LEC" -> context.resources.getString(R.string.schedule_item_lec)

            "exam" -> context.resources.getString(R.string.schedulter_event_exam)
            "conference" -> context.resources.getString(R.string.schedulter_event_conference)
            "quiz" -> context.resources.getString(R.string.schedulter_event_quiz)
            "assign" -> context.resources.getString(R.string.schedulter_event_assign)
            "other" -> context.resources.getString(R.string.schedulter_event_other)

            else -> String.empty()
        }
    }

    private fun breakDrawable(length: Int): Int {
        return when (length) {
            in 0..10 -> R.drawable.ic_run_24px
            in 11..20 -> R.drawable.ic_local_cafe_24px
            in 21..40 -> R.drawable.ic_restaurant_24px
            else -> R.drawable.ic_hotel_24px
        }
    }
}