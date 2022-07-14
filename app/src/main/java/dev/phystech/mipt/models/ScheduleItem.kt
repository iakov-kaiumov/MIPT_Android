package dev.phystech.mipt.models

import dev.phystech.mipt.adapters.LinksAdapter
import dev.phystech.mipt.models.api.Creator
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import io.realm.RealmList
import java.io.Serializable

class ScheduleItem (
    override val name: String,
    override val prof: String,
    override val place: String,
    override val day: Int,
    override val type: String,
    override val startTime: String,
    override val endTime: String,
    val notes: String? = null,
    var shareUrl: String? = null,
    var broadcastUrl: String? = null,
    var recordsUrl: String? = null,
    var materialsUrl: String? = null,
    var teachers: ArrayList<TeacherModel>? = null
) : SchedulerListShortItem, Serializable {

    override var id: Int = -1
    override val dayCorrect: Int get() = day
    override val allDay: Boolean = false
    var synchronized: Int? = null
    var updated: String? = null
    var updater: Creator? = null
    var origin: String? = null
    var canEdit: Boolean = false
    var evenodd: Int = 0
    var auditorium: List<SchedulePlace> = emptyList()
    var urls: UrlsWrap = UrlsWrap()
    var users: MutableList<SchdulerUser> = mutableListOf()
    var course: String? = null


    override fun length(): Double = TimeUtils.length(startTime, endTime)
    override fun getStartTimeDisplay(): String = startTime
    override fun getEndTimeDisplay(): String = endTime

    override fun equals(other: Any?): Boolean {
        val that = other as ScheduleItem
        return (this.name == that.name) && (this.day == that.day) && (this.startTime == that.startTime)
    }

    override fun toString(): String {
        return "name: ${this.name}, prof: ${this.prof}, place: ${this.place}\n, day: ${this.day}"
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + prof.hashCode()
        result = 31 * result + place.hashCode()
        result = 31 * result + day
        result = 31 * result + type.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}

class SchedulerItemAlarmModel(
    val name: String,
    val prof: String,
    val place: String,
    val day: Int,
    val type: String,
    val startTime: String,
    val endTime: String,
    val notes: String? = null,
    var shareUrl: String? = null,
    var broadcastUrl: String? = null,
    var recordsUrl: String? = null,
    var materialsUrl: String? = null,
    var teachers: ArrayList<TeacherModel>? = null): Serializable {

    companion object {
        fun fromSchedulerItem(item: ScheduleItem): SchedulerItemAlarmModel {
            return SchedulerItemAlarmModel(
                item.name,
                item.prof,
                item.place,
                item.day,
                item.type,
                item.startTime,
                item.endTime,
                item.notes,
                item.shareUrl,
                item.broadcastUrl,
                item.recordsUrl,
                item.materialsUrl,
                item.teachers
            )
        }
    }
}

interface SchedulerListShortItem {
    val id: Int
    val type: String
    val name: String
    val prof: String?
    val place: String?
    val startTime: String
    val endTime: String
    val day: Int
    val dayCorrect: Int
    val allDay: Boolean

    fun length(): Double

    fun getStartTimeDisplay(): String
    fun getEndTimeDisplay(): String
}