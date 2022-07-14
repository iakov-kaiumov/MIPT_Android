package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.LegendModel
import dev.phystech.mipt.network.NetworkUtils
import io.realm.RealmObject
import io.realm.annotations.Ignore

class HistoryDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    inner class Data: BaseApiEntity.Data() {
        var paginator: HashMap<String, HistoryPaginatorItem> = HashMap()
    }


    companion object {
        fun mapToLegendModel(paginatorModel: HistoryPaginatorItem): LegendModel {
            val model = LegendModel().apply {
                id = paginatorModel.id
                content = paginatorModel.content
                title = paginatorModel.title
                paginatorModel.summaryImage?.signatures?.firstOrNull()?.let { image ->
                    imageUrl = NetworkUtils.getImageUrl(image.id, image.dir, image.path)
                }
            }

            return model
        }
    }
}
open class HistoryPaginatorItem {
    var id: String? = null
    var userId: String? = null
    var parent: String? = null
    var type: String? = null
    var route: String? = null
    var template: String? = null
    var timeStart: String? = null
    var timeEnd: String? = null
    var timePublish: String? = null
    var free: String? = null
    var forChildren: Boolean = false
    var eventCategory: String? = null
    var title: String? = null
    var subtitle: String? = null
    var language: BaseApiEntity.Language? = null
    var blockText: String? = null
    var summary: String? = null
    var content: String? = null
    var phone: String? = null
    var formattedContent: String? = null
    var place: String? = null
    var summaryImage: BaseApiEntity.SummaryImage? = null
    var contentImage: BaseApiEntity.ContentImage? = null
    var commentStatus: String? = null
    var views: Int? = null
    var featured: Boolean = false
    var fullPicture: Boolean = false
    var coverView: Boolean = false
    var useTermsFilter: Boolean = false
    var useTypographFilter: Boolean = false
    var keywords: String? = null
    var navigatePlaces: ArrayList<BaseApiEntity.NavigationClass> = ArrayList()
    @Ignore
    var chairs: ArrayList<Any> = ArrayList()
    @Ignore
    var recentComments: ArrayList<Any> = ArrayList()
    var isFavorite: Boolean = false
    var twoPerRow: Boolean = false
    var recentCommentsCount: Int? = null
    var status: Int? = null
    var created: BaseApiEntity.TimeDetail? = null
    var updated: BaseApiEntity.TimeDetail? = null

}