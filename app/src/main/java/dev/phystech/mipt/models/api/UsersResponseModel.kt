package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.Lastactive
import dev.phystech.mipt.models.UserAbstract

class UsersResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        val paginator: HashMap<String, UserInfoModel> = hashMapOf()
    }

    class UserInfoModel : UserAbstract {
        override var id: String? = null
        override var name: String? = null
        var email: String? = null
        override var post: String? = null
        override var image: SummaryImage? = null
        override var socialUrl: String? = null
        override var miptUrl: String? = null
        override var wikiUrl: String? = null

        var roles: List<String>? = null
        var lastactive: Lastactive? = null

        fun getAvatarName(): String {
            var result: String = ""
            if(!name.isNullOrEmpty())
                for (char in name!!.split(' '))
                    result += char.get(0)
            return result
        }

        fun getTrimName(): String {
            var result: String = ""
            if (!name.isNullOrEmpty()) {
                val strList = name!!.split(" ")
                if (strList.size > 2) {
                    result = "${strList[0]} ${strList[1]}"
                } else {
                    result = name!!
                }
            }
            return result
        }
    }
}