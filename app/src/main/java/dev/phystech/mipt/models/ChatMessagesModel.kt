package dev.phystech.mipt.models

import dev.phystech.mipt.R
import dev.phystech.mipt.models.api.Member
import dev.phystech.mipt.models.api.Message
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ChatMessagesModel: RealmObject() {

    @PrimaryKey
    var id: Long? = null
    var messages: RealmList<Message> = RealmList()
}