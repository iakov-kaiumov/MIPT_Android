package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.BaseApiEntity
import dev.phystech.mipt.models.api.Creator

class SportSectionModel {
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var description: String? = null
    var image: BaseApiEntity.SummaryImage? = null
    var url: String? = null
    var idLms: String? = null
    var semesters: ArrayList<BaseApiEntity.SimpleField> = arrayListOf()
    var creator: Creator? = null
    var status: Int? = null
    var schedules: ArrayList<SchedulerIndex> = arrayListOf()
}

