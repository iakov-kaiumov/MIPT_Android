package dev.phystech.mipt.models.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class BaseApiEntity {
    var status: String = "error"

    class Filter {
        var status: ArrayList<Int> = ArrayList()
    }

    class Department {
        var id: String? = null
        var name: String? = null
    }

    class Chair {
        var id: String? = null
        var name: String? = null
    }

    open class Data {
        //        var filter: Filter? = null
        var count: Int? = null
        var itemsPerPage: Int? = null
        var page: Int? = null
    }

    class Place {
        var id: String? = null
        var address: String? = null
        var latitude: Double? = null
        var longitude: Double? = null
    }

    class TimeDetail  {
        @Expose @SerializedName("timezone_type")
        var timezoneType: Int? = null
        var date: String? = null
        var timezone: String? = null
    }

    class Signatures: Serializable {
        var id: String? = null
        var path: String? = null
        var dir: String? = null
        var name: String? = null
        var size: Int? = null
        var type: String? = null
    }

    class SummaryImage: Serializable {
        var id: String? = null
        var signatures: ArrayList<Signatures> = ArrayList()

    }

    class ContentImage {
        var id: String? = null
        @Ignore
        var signatures: ArrayList<Int> = ArrayList()
    }

    class NavigationClass {
        @PrimaryKey
        var id: String? = null
        var name: String? = null
    }

    class Language {
        var id: String? = null
        var name: String? = null
    }

    class SimpleField {
        var id: String? = null
        var name: String? = null
    }

}

open class Tag: RealmObject() {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var imageUrl: String? = null
}