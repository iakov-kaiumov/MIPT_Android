package dev.phystech.mipt.models

import com.google.gson.annotations.SerializedName

class MeetItem {
    var name: String? = null
    var meetName: String? = null
    var dates: String? = null
    var url: String? = null
    var fileType: String? = null
    var id: String? = null
    var status: Int = -1
    var file: FileModel? = null


    var startDate: DateModel? = null
    var endDate: DateModel? = null
    var created: DateModel? = null
    var updated: DateModel? = null



    class DateModel {
        var date: String? = null
        var timezone: String? = null
        @SerializedName("timezone_type")
        var timezoneType: Int? = -1
    }

    class FileModel {
        var id: String? = null
        var signatures: ArrayList<Signature> = ArrayList()
    }

    class Signature {
        var id: String? = null
        var path: String? = null
        var dir: String? = null
        var name: String? = null
        var type: String? = null
        var size: Int = -1
    }

    fun getMeetItemType(): MeetItemType {
        return if (file?.signatures?.firstOrNull() != null) MeetItemType.Model
        else MeetItemType.Link
    }

    enum class MeetItemType {
        Model,
        Link
    }

}