package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.BaseApiEntity
import io.realm.annotations.Ignore

interface UserAbstract {
    var id: String?
    var name: String?
    var post: String?
    var image: BaseApiEntity.SummaryImage?
    var socialUrl: String?
    var miptUrl: String?
    var wikiUrl: String?
}