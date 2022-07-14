package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.EventModel

class EventResponse: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var paginator: ArrayList<EventModel> = arrayListOf()
    }

}