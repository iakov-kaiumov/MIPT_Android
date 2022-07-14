package dev.phystech.mipt.models.api

import dev.phystech.mipt.repositories.ScheduleEventRepository
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PsyTimeResponseModel: BaseApiEntity() {

    var data: Data? = null

    class Data {
        val paginator: HashMap<String, PsyInfoModel> = hashMapOf()
    }

    class PsyInfoModel {
        var psy: Psy? = null
        var startTime: PsyTime? = null
        var endTime: PsyTime? = null
        var free: Boolean? = null
        var type: String? = null
        var location: String? = null
        var id: String? = null
        var status: Int? = null
        var user_psy: UsersResponseModel.UserInfoModel? = null
    }

    class Psy {
        var id: String? = null
        var name: String? = null
    }

    class PsyTime {
        var date: String? = null
        var timezone_type: Int? = null
        var timezone: String? = null

        fun getFormatDate(): Date? {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            try {
                return dateFormat.parse(date ?: "")
            } catch (e: Exception) {
                return null
            }
        }
    }
}