package dev.phystech.mipt.models.api

class NewsDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    inner class Data: BaseApiEntity.Data() {
        var paginator: HashMap<String, PaginatorItem> = HashMap()
    }

    class PaginatorItem {
        var id: String? = null
//        var userId: String? = null
        var parent: String? = null
        var type: String? = null
//        var route: String? = null
//        var template: String? = null
//        var timeStart: String? = null
//        var timeEnd: String? = null
//        var timePublish: String? = null
//        var free: String? = null
//        var forChildren: Boolean = false
//        var eventCategory: String? = null
        var title: String? = null
//        var subtitle: String? = null
//        var language: Language? = null
//        var blockText: String? = null
//        var summary: String? = null
        var content: String? = null
//        var phone: String? = null
//        var formattedContent: String? = null
        var place: String? = null
//        var summaryImage: SummaryImage? = null
//        var contentImage: ContentImage? = null
//        var commentStatus: String? = null
//        var views: Int? = null
//        var featured: Boolean = false
//        var fullPicture: Boolean = false
//        var coverView: Boolean = false
//        var useTermsFilter: Boolean = false
//        var useTypographFilter: Boolean = false
//        var keywords: String? = null
//        var labels: ArrayList<Any> = ArrayList()
//        var crafts: ArrayList<Any> = ArrayList()
//        var navigatePlaces: ArrayList<NavigationClass> = ArrayList()
//        var companies: ArrayList<Any> = ArrayList()
//        var favoriteUsers: ArrayList<Any> = ArrayList()
//        var chairs: ArrayList<Any> = ArrayList()
//        var recentComments: ArrayList<Any> = ArrayList()
//        var isFavorite: Boolean = false
//        var twoPerRow: Boolean = false
//        var recentCommentsCount: Int? = null
        var status: Int? = null
//        var created: TimeDetail? = null
//        var updated: TimeDetail? = null

    }
}