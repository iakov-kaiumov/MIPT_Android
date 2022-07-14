package dev.phystech.mipt.models

import android.net.Uri
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class UrlsWrap: RealmObject(), Serializable {
    @PrimaryKey
    var id: Int? = null

    var broadcast: String? = null
    var records: String? = null
    var materials: String? = null
    var share: String? = null
    var course: String? = null

    fun findSecret(): String? {
        return Uri.parse(share).getQueryParameter("secret")
    }
}