package dev.phystech.mipt.models.api

class ChatDataResponse: BaseApiEntity() {
    var data: Data? = null

    class Data {
        var query: String? = null
        var count: Int? = null
        var itemsPerPage: Int? = null
        var page: Int? = null
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var name: String? = null
        var keyword: String? = null
        var answer: String? = null
        var buttons: ArrayList<MessageButton> = ArrayList()
    }

    class MessageButton {
        var id: String? = null
        var name: String? = null
        var answer: String? = null
    }

}