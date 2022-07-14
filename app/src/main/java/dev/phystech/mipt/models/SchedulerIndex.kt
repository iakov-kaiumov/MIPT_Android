package dev.phystech.mipt.models

import android.content.res.Resources
import dev.phystech.mipt.R

class SchedulerIndex {
    var id: Int = -1
    var name: String? = null
    var prof: String? = null
    var place: String? = null
    var day: Int? = null
    var type: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var color: String? = null
    var notes: String? = null
    var synchronized: Int? = null
    var evenodd: Int? = null
    var updated: String? = null
    var origin: String? = null
    var teachers: ArrayList<Teacher> = arrayListOf()
    var auditorium: ArrayList<SchedulePlace> = arrayListOf()
    var canEdit: Boolean? = null
    var urls: UrlsWrap? = null
    var personStatus: String? = null
    var chatId: String? = null
    var course: String? = null


    fun buildTime(resource: Resources): String? {
        val dayIndex = day?.minus(1) ?: 0
        val dayShort = resource.getStringArray(R.array.week_short).getOrNull(dayIndex) ?: ""

        return "${dayShort} $startTime - $endTime".trim()
    }
}