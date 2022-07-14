package dev.phystech.mipt.models

import android.os.Parcel
import android.os.Parcelable
import dev.phystech.mipt.models.api.Creator
import dev.phystech.mipt.models.api.UserDataResponseModel
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

open class EventModel: RealmObject(), SchedulerListShortItem, Serializable, Parcelable {
    @PrimaryKey
    override var id: Int = -1
    var synchronized: Int? = null
    override var allDay: Boolean = false
    var canEdit: Boolean = false
    override var name: String = ""
    override var prof: String? = null
    override var place: String? = null
    override var type: String = ""
    override var startTime: String = ""
    override var endTime: String = ""
    var notes: String? = null
    var updated: String? = null
    var origin: String? = null
    var personStatus: String? = null
    var course: String? = null
    var teachers: RealmList<Teacher> = RealmList()
    var auditorium: RealmList<SchedulePlace> = RealmList()
    var users: RealmList<Teacher> = RealmList()
    var urls: UrlsWrap? = null
    var updater: Creator? = null




    override val day: Int get() {
        df.parse(startTime)?.let {
            val cal = GregorianCalendar().apply { time = it }
            return (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
        }

        return 1
    }

    override val dayCorrect: Int get() {
        df.parse(startTime)?.let {
            val cal = GregorianCalendar().apply { time = it }
            val day = (cal.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7
            if (day == 0) return 7

            return day
        }

        return 1
    }

    override fun length(): Double = TimeUtils.length(startTime, endTime)

    override fun getStartTimeDisplay(): String {
        df.parse(startTime)?.let { return dfDisplay.format(it) }
        return startTime
    }

    override fun getEndTimeDisplay(): String {
        df.parse(endTime)?.let { return dfDisplay.format(it) }
        return endTime
    }


    fun getEventType(): EventType? {
        return EventType.getByTypeValue(type)
    }



    companion object {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val dfDisplay = SimpleDateFormat("HH:mm")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {

    }

}
