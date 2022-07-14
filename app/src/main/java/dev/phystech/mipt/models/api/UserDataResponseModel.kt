package dev.phystech.mipt.models.api

class UserDataResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        var user: UserInfoResponse? = null
    }

    class UserInfoResponse {
        var id: String? = null
        var firstname: String? = null
        var lastname: String? = null
        var secondname: String? = null
        var fio: String? = null
        var email: String? = null
        var groups: ArrayList<Group> = ArrayList()
        var roles: ArrayList<String> = ArrayList()
    }

    class Group {
        var id: String? = null
        var name: String? = null
    }
}