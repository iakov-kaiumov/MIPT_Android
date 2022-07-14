package dev.phystech.mipt.models.api

import com.google.gson.annotations.SerializedName
import dev.phystech.mipt.models.EventModel

class UpdateSchedulerEventResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        @SerializedName("schedule-event")
        var scheduleEvent: EventModel? = null
    }
}