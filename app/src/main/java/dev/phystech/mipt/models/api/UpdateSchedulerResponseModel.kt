package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.ScheduleItem

class UpdateSchedulerResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var schedule: ScheduleItem? = null
        var message: String? = null
        var similar: List<ScheduleItem> = listOf()
    }
}
