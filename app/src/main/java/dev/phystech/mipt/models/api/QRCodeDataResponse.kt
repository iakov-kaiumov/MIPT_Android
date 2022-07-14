package dev.phystech.mipt.models.api

class QRCodeDataResponse: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var code: String? = null
        var places: ArrayList<EntityValue> = ArrayList()
        var posts: ArrayList<EntityValue> = ArrayList()


    }

    class EntityValue {
        var id: String? = null
        var name: String? = null
    }

}