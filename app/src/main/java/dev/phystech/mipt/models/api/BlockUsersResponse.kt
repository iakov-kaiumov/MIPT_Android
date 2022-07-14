package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.User

data class BlockUsersResponse (
    val status: String,
    val data: BlockUsersData?
)

data class BlockUsersData (
    val users: ArrayList<BlockUserElement>,
    val warnings: List<Any?>
)

data class BlockUserElement (
    val id: String,
    val fullname: String,
    val profileimageurl: String,
    val user: User
) {
    fun getAvatarName(): String {
        var result: String = ""
        if(!user.getTrimName().isNullOrEmpty())
            for (char in user.getTrimName().split(' '))
                result += char.get(0)
        return result
    }
}