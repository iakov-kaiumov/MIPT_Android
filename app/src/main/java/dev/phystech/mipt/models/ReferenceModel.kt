package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.ContactsDataResponseModel
import dev.phystech.mipt.models.api.ContactsPaginatorItem
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ReferenceModel: RealmObject() {
    @PrimaryKey
    var id: String? = null
    var name: String = "-"
    var occupation: String = "-"
    var place: String = "-"
    var location: String = "-"
    var order: Int? = null
    var innerPhoneNumber: String = "-"
    var personalPhoneNumber: String = "-"
    var email: String = "-"

    private var model: ContactsPaginatorItem? = null


    fun getAlertTitle(): String {
        var title = name

        if (model == null) {
            title += ", $personalPhoneNumber, $email"
        } else {
            model?.phoneExt?.let { title += ", $it" }
            model?.email?.let { title += ", $it" }
        }

        return title
    }

    val containPhone: Boolean
        get() = personalPhoneNumber != "-" && personalPhoneNumber.isNotEmpty()
    val containEmail: Boolean
        get() = email != "-" && email.isNotEmpty()

    companion object {
        fun map(from: ContactsPaginatorItem): ReferenceModel {
            val model = ReferenceModel().apply {
                name = from.name ?: "-"
                occupation = from.occupation ?: "-"
                place = from.departments.firstOrNull()?.name ?: "-"
                innerPhoneNumber = from.phoneInt ?: "-"
                order = from.order
                personalPhoneNumber= from.phoneExt ?: "-"
                email = from.email ?: "-"
                location = from.location ?: ""
                id = from.id
            }

            return model
        }
    }
}