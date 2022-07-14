package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.TimeSlotModel

class TimeSlotsResponseModel: BaseApiEntity() {
    var data: ArrayList<TimeSlotModel> = arrayListOf()
}