package dev.phystech.mipt.models.api

import com.google.gson.annotations.SerializedName

class AuthorizationModel {
    var status: String? = null
    @SerializedName("access_token") var token: String? = null
    var content: Content? = null

    inner class Content {
        var message: String? = null
    }
}