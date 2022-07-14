package dev.phystech.mipt.models.api

import com.google.gson.annotations.SerializedName

class UserLoginResponseModel: BaseApiEntity() {
    var data: Data? = null

    class Data {
        @SerializedName("user_id")
        var userId: String? = null
        @SerializedName("access_token")
        var accessToken: String? = null
        @SerializedName("expires_in")
        var expiresIn: String? = null
    }
}