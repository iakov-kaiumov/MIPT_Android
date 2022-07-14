package dev.phystech.mipt.models.api

class TagsDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    inner class Data {
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var name: String? = null
        var type: String? = null
        var image: SummaryImage? = null



        val tagType: TagType?
            get() {
                return when (type) {
                    "info" -> TagType.Info
                    "use" -> TagType.Use
                    else -> null
                }
            }

    }

    enum class TagType {
        Info,
        Use
    }

}