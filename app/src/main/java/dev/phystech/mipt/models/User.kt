package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.BaseApiEntity
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class User: RealmObject() {
    @PrimaryKey
    var id: String = ""
    var name: String? = ""
    var post: String? = ""
    @Ignore
    var image: BaseApiEntity.SummaryImage? = null
    var socialURL: String? = null
    var miptURL: String? = null
    var wikiURL: String? = null
    @Ignore
    var updated: Lastactive? = null
    @Ignore
    var lastactive: Lastactive? = null
    var roles: RealmList<String>? = null

    fun getTrimName(): String {
        var result: String = ""
        if (!name.isNullOrEmpty()) {
            val strList = name!!.split(" ")
            if (strList.size > 2) {
                result = "${strList[0]} ${strList[1]}"
            } else {
                result = name!!
            }
        }
        return result
    }
}

open class Lastactive: RealmObject() {
    var date: String? = null
    var timezoneType: Long? = null
    var timezone: String? = null
}