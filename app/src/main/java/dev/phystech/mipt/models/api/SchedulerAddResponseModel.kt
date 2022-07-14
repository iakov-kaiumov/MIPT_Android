package dev.phystech.mipt.models.api

import com.google.gson.annotations.SerializedName
import dev.phystech.mipt.models.SchedulePlace

class SchedulerAddResponseModel: BaseApiEntity() {
    var data: Data? = null


    class Data {
        @SerializedName("schedule-place")
        var schedulerPlace: SchedulePlace? = null
    }
}