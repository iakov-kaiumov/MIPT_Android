package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.EventModel

class MyEventResponse: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var version: String? = null
        var timetable: HashMap<String, ArrayList<EventModel>> = hashMapOf()
    }
}
