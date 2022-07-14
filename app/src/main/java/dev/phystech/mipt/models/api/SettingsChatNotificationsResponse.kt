package dev.phystech.mipt.models.api

data class SettingsChatNotificationsResponse (
    val status: String,
    val data: SettingsChatNotificationsData?
)

data class SettingsChatNotificationsData (
    val blocknoncontacts: Int,
    val entertosend: Boolean,
    val warnings: List<Any>,
    val emailNotify: Boolean
)
