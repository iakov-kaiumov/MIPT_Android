package dev.phystech.mipt.models

import io.realm.RealmList
import io.realm.RealmObject

open class NewsCategory: RealmObject() {
    var id: Int? = null
    var title: String? = null
    var news : RealmList<News> = RealmList()
}