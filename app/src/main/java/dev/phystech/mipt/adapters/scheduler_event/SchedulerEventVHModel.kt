package dev.phystech.mipt.adapters.scheduler_event

import dev.phystech.mipt.models.EventModel
import dev.phystech.mipt.ui.fragments.study.events.SchedulerEventType
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

sealed class SchedulerEventVHModel

class SchedulerEventTitleModel(val date: Date): SchedulerEventVHModel() {
    var text: String? = dateFormat.format(date)

    init {
        val cDay = TimeUnit.MILLISECONDS.toDays(Date().time)
        val mDay = TimeUnit.MILLISECONDS.toDays(date.time)

        if (cDay == mDay) {
            text = "Сегодня"
        } else if (cDay - 1 == mDay) {
            text = "Вчера"
        } else if (cDay + 1 == mDay) {
            text = "Завтра"
        }
    }

    companion object {
        var dateFormat: DateFormat = SimpleDateFormat("EEEE, dd MMMM")
    }
}

class SchedulerEventModel(model: EventModel, val isLast: Boolean = false): SchedulerEventVHModel() {
    var startTime: String? = null
    var endTime: String? = null

    init {
        dateFormatToDate.parse(model.startTime ?: "")?.let {
            startTime = dateFormatToText.format(it)
        }
        dateFormatToDate.parse(model.endTime ?: "")?.let {
            endTime = dateFormatToText.format(it)
        }
    }

    val id: Int = model.id
    val title = model.name
    val teacher = model.teachers.mapNotNull { v -> v.name }.joinToString(separator = "\n" )
    val auditory = model.auditorium.firstOrNull()?.name
    val type: Int? = SchedulerEventType.getByType(model.type ?: "")?.getDysplayedResId()
    val time: String? = "$startTime\n$endTime"

    companion object {
        val dateFormatToText: DateFormat = SimpleDateFormat("HH:mm")
        val dateFormatToDate: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}
