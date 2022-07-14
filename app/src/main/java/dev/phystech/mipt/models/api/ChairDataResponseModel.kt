package dev.phystech.mipt.models.api

import io.realm.RealmObject
import java.io.Serializable

class ChairDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    inner class Data: BaseApiEntity.Data() {
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var id: String? = null
        var name: String? = null
        var type: String? = null
        var cover: SummaryImage? = null
        var posts: ArrayList<Post> = ArrayList()
        var creator: Creator? = null
        var status: Int? = null
        var created: TimeDetail? = null
        var updated: TimeDetail? = null
    }


}

open class Creator: RealmObject(), Serializable {
    var id: String? = null
    var name: String? = null
}

open class Post {
    var id: String? = null
    var name: String? = null

}