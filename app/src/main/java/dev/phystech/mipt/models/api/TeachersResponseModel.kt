package dev.phystech.mipt.models.api

import dev.phystech.mipt.models.Teacher
import java.util.*
import kotlin.collections.HashMap

class TeachersResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        var paginator: HashMap<String, Teacher> = HashMap()
    }

}