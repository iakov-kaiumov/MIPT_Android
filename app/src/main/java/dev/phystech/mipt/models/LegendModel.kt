package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.HistoryDataResponseModel
import dev.phystech.mipt.models.api.HistoryPaginatorItem
import dev.phystech.mipt.models.api.PlacePaginatorItem
import dev.phystech.mipt.models.api.PlacesDataResponseModel
import dev.phystech.mipt.network.NetworkUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore

open class LegendModel: RealmObject() {
    var id: String? = null
    var date: String? = null
    var imageUrl: String? = null
    var title: String? = null
    var place: String? = null
    var content: String? = null
    var typeIndex: Int = 0
    @Ignore
    var type: Type = Type.getByOrdinal(typeIndex)
    var chair: String? = null
    var placesID: RealmList<String> = RealmList()

    @Ignore
    var placeSource: PlaceModel? = null
    @Ignore
    var legendSource: HistoryPaginatorItem? = null


    enum class Type(val index: Int) {
        None(0),
        News(1),
        Event(2),
        Chair(3),
        History(4);


        companion object {
            fun getByOrdinal(value: Int): Type {
                return when (value) {
                    1 -> News
                    2 -> Event
                    3 -> Chair
                    4 -> History
                    else -> None
                }
            }
        }
    }

    companion object {
        fun fromPaginatorItem(paginatorItem: HistoryPaginatorItem): LegendModel {
            val model = LegendModel()
            model.apply {
                content = paginatorItem.content
                title = paginatorItem.title
                paginatorItem.summaryImage?.signatures?.firstOrNull()?.let { image ->
                    imageUrl = NetworkUtils.getImageUrl(image.id, image.dir, image.path)
                }
                placesID.addAll(ArrayList(paginatorItem.navigatePlaces.mapNotNull { v -> v.id }))
            }

            return model
        }
    }


}