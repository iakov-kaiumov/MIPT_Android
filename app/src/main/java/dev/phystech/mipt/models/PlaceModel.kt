package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.PlacePaginatorItem
import dev.phystech.mipt.models.api.Tag
import dev.phystech.mipt.network.NetworkUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class PlaceModel: RealmObject() {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var description: String? = null
    var tags: RealmList<Tag> = RealmList()
    var avatarImageUrl: String? = null
    var allImagesUrls: RealmList<String> = RealmList()
    var address: String? = null
    var type: String? = null
    var postsID: RealmList<String> = RealmList()
    @Ignore
    var fromModel: PlacePaginatorItem? = null
    var lat: Double? = null
    var lng: Double? = null
    var needPermit: Boolean = false

    companion object {
        fun fromPaginatorItem(apiModel: PlacePaginatorItem): PlaceModel {
            val model = PlaceModel().apply {
                id = apiModel.id
                name = apiModel.name ?: ""
                description = apiModel.description
                address = apiModel.place?.address
                val images = apiModel.image?.signatures?.mapNotNull { v ->
                    NetworkUtils.getImageUrl(v.id, v.dir, v.path)
                } ?: ArrayList()

                avatarImageUrl = images.firstOrNull()
                allImagesUrls.addAll(images)

                lat = apiModel.place?.latitude
                lng = apiModel.place?.longitude
                needPermit = apiModel.needPermit

                tags.addAll(apiModel.tags)
                postsID.addAll(apiModel.posts.map { v -> v.id})

            }


            return model
        }
    }
}