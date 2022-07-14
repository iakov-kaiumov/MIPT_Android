package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.SchedulePlace

class SchedulePlacesResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var paginator: HashMap<String, SchedulePlace> = hashMapOf()
    }
}