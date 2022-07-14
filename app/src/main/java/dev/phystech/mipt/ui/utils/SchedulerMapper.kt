package dev.phystech.mipt.ui.utils

import dev.phystech.mipt.models.ScheduleItem
import dev.phystech.mipt.models.SchedulerIndex
import dev.phystech.mipt.models.Teacher
import dev.phystech.mipt.models.UrlsWrap

class SchedulerMapper {
    companion object {
        fun mapItemToIndex(itemModel: ScheduleItem): SchedulerIndex {
            val indexModel = SchedulerIndex().apply {
                id = itemModel.id
                name = itemModel.name
                prof = itemModel.prof
                place = itemModel.place
                day = itemModel.day
                type = itemModel.type
                startTime = itemModel.startTime
                endTime = itemModel.endTime
                notes = itemModel.notes
                synchronized = itemModel.synchronized
                evenodd = itemModel.evenodd
                updated = itemModel.updated
                teachers = arrayListOf()
                auditorium = ArrayList(itemModel.auditorium)
                canEdit = itemModel.canEdit
                urls = itemModel.urls
//                personStatus = itemModel.personStatus
//                chatId = itemModel.chatId
//                course = itemModel.course
            }

            return indexModel
        }

        fun mapIndexToItem(indexModel: SchedulerIndex): ScheduleItem {
            val itemModel = ScheduleItem(
                indexModel.name ?: "",
                indexModel.prof ?: "",
                indexModel.place ?: "",
                indexModel.day ?: 1,
                indexModel.type ?: "",
                indexModel.startTime ?: "",
                indexModel.endTime ?: "",
                indexModel.notes
            ).apply {
                id = indexModel.id
                synchronized = indexModel.synchronized
                updated = indexModel.updated
                canEdit = indexModel.canEdit ?: true
                evenodd = indexModel.evenodd ?: 0
                auditorium = indexModel.auditorium
                urls = indexModel.urls ?: UrlsWrap()
            }

            return itemModel
        }
    }
}