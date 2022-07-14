package dev.phystech.mipt.models.api.request

import dev.phystech.mipt.models.ScheduleItem

class UpdateSchedulerRequestModel {
    var data: ScheduleItem? = null
    var group: Int? = null

    fun setGroupEdit(isEnabled: Boolean) {
        group = if (isEnabled) 1 else 0
    }
}