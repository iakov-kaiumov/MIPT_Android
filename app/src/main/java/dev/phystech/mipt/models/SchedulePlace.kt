package dev.phystech.mipt.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class SchedulePlace: RealmObject(), Serializable {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var building: Building? = null
    var type: Building? = null
    var floor: String? = null

}

open class Building: RealmObject(), Serializable {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
}