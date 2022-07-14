package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.BaseApiEntity
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.io.Serializable

class SchdulerUser: UserAbstract, Serializable {
    override var id: String? = null
    override var name: String? = null
    override var post: String? = null
    override var image: BaseApiEntity.SummaryImage? = null
    override var socialUrl: String? = null
    override var miptUrl: String? = null
    override var wikiUrl: String? = null
}