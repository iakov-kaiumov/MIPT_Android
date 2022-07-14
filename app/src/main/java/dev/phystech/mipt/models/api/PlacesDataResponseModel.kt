package dev.phystech.mipt.models.api

import io.realm.RealmObject

class PlacesDataResponseModel: BaseApiEntity() {
    var data: Data? = null

    inner class Data: BaseApiEntity.Data() {
        var paginator: HashMap<String, PlacePaginatorItem> = HashMap()
    }

}


class PlacePaginatorItem {
    var name: String? = null
    var language: BaseApiEntity.Language? = null
    var number: Int? = null
    var description: String? = null
    var needPermit: Boolean = false
    var cover: BaseApiEntity.SummaryImage? = null
    var image: BaseApiEntity.SummaryImage? = null
    var tags: ArrayList<Tag> = ArrayList()
    var place: BaseApiEntity.Place? = null
    var posts: ArrayList<Post> = ArrayList()
    var creator: Creator? = null
    var id: String? = null
    var status: Int? = null
    var created: BaseApiEntity.TimeDetail? = null
    var updated: BaseApiEntity.TimeDetail? = null
}