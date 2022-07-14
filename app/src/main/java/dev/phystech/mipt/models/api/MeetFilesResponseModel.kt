package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.MeetItem

class MeetFilesResponseModel {
    var status: String? = null
    var data: Data? = null


    class Data {
        var filter: FilterModel? = null
        var paginator: HashMap<String, MeetItem> = HashMap()
    }

    class FilterModel {
        var status: ArrayList<Int> = ArrayList()
    }
}