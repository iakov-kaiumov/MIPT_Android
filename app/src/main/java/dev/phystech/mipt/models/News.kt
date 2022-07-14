package dev.phystech.mipt.models

import dev.phystech.mipt.models.api.EventDataResponseModel
import dev.phystech.mipt.network.NetworkUtils
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

open class News: RealmObject() {
    @PrimaryKey
    var id: String? = null
    var title: String? = null
    var subtitle: String? = null
    var content: String? = null
    var image: String? = null
    var date: String? = null
    var chair: String? = null
    var typeId: Int = 0
    var chairType: String? = null
    var serverDate: String? = null
    var attachedEventId: String? = null

    @Ignore
    var imageWidth: String? = null
    @Ignore
    var imageHeight: String? = null

    enum class Type(val index: Int) {
        News(0),        //  Новости
        Chair(1),       //  Внутренние
        Events(2);      //  События

        companion object {
            fun getByOrdinal(value: Int): Type? {
                return when (value) {
                    0 -> News
                    1 -> Chair
                    2 -> Events
                    else -> null
                }
            }
        }
    }


    companion object {
        fun mapFromPaginatorItem(model: EventDataResponseModel.PaginatorItem, withChair: Boolean): News {
            val cDate = serverDateFormat.parse(model.timeStart ?: "-")
            val news = News().apply {
                id = model.id
                title = model.chairs.firstOrNull()?.name ?: title
                content = model.content
                subtitle = model.title
                chair
                cDate?.let {
                    val cDay = TimeUnit.MILLISECONDS.toDays(Date().time)
                    val nDay = TimeUnit.MILLISECONDS.toDays(it.time)
                    if (nDay == cDay) {
                        date = "Сегодня в ${shortDisplayDateFormat.format(it)}"
                    } else if (nDay == cDay - 1) {
                        date = "Вчера в ${shortDisplayDateFormat.format(it)}"
                    } else {
                        date = displayDateFormat.format(it)
                    }
                }
                chairType = model.type
                if (withChair) {
                    chair = model.chairs.firstOrNull()?.name
                }

                serverDate = model.timeStart

                model.summaryImage?.signatures?.firstOrNull().let {
                    if (it != null) {
                        image = NetworkUtils.getImageUrl(it.id, it.dir, it.path)
                    }
                }

                model.scheduleEvents.firstOrNull()?.let {
                    attachedEventId = it.id
                }
            }

            return news
        }

        val serverDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val displayDateFormat = SimpleDateFormat("dd MMMM в HH:mm")
        val shortDisplayDateFormat = SimpleDateFormat("HH:mm")
    }

}