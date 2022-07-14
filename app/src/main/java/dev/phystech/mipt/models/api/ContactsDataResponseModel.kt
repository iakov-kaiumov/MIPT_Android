package dev.phystech.mipt.models.api

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

class ContactsDataResponseModel: BaseApiEntity() {
    var data: Data? = null

    inner class Data: BaseApiEntity.Data() {
        var paginator: HashMap<String, ContactsPaginatorItem> = HashMap()
    }

}

open class ContactsPaginatorItem: RealmObject() {
    @PrimaryKey
    var id: String? = null
    var name: String? = null
    var phoneInt: String? = null
    var phoneExt: String? = null
    var email: String? = null
    var order: Int = 1000
    var url: String? = null
    var occupation: String? = null
    var location: String? = null
    @Ignore
    var departments: ArrayList<Tag> = ArrayList()

    @Ignore
    var created: BaseApiEntity.TimeDetail? = null
    @Ignore
    var updated: BaseApiEntity.TimeDetail? = null
}