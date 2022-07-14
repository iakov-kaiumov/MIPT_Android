package dev.phystech.mipt.models.api

class ChairTopicDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var name: String? = null
        var language: Language? = null
        var id: String? = null
    }

}