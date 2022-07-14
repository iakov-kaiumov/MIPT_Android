package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.BaseApiEntity
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.io.Serializable

open class Teacher: RealmObject(), UserAbstract, Serializable {
    @PrimaryKey
    override var id: String? = null
    override var name: String? = null
    override var post: String? = null
    @Ignore
    override var image: BaseApiEntity.SummaryImage? = null
    override var socialUrl: String? = null
    override var miptUrl: String? = null
    override var wikiUrl: String? = null

    fun copy(): Teacher {
        return Teacher().apply {
            this.id = this@Teacher.id
            this.name = this@Teacher.name
            this.post = this@Teacher.post
            this.image = this@Teacher.image
            this.socialUrl = this@Teacher.socialUrl
            this.miptUrl = this@Teacher.miptUrl
            this.wikiUrl = this@Teacher.wikiUrl
        }
    }
}