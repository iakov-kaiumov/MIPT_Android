package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.models.SchedulerIndexModel

class SchedulerIndexResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var paginator: ArrayList<SchedulerIndex> = arrayListOf()
    }
}