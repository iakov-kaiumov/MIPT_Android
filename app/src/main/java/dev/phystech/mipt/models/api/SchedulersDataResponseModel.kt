package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.ScheduleItem
import java.io.Serializable

class SchedulersDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data: Serializable {
        var version: String? = null
        var timetable: HashMap<String, ArrayList<ScheduleItem>> = HashMap()
    }

    class TimeTableItem {
        var id: Int = -1
        var synchronized: Int? = null
        var day: Int? = null
        var name: String? = null
        var prof: String? = null
        var place: String? = null
        var type: String? = null
        var startTime: String? = null
        var endTime: String? = null
        var color: String? = null
        var notes: String? = null
        var updated: String? = null
    }
}
