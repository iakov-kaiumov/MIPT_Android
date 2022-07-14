package dev.phystech.mipt.adapters.schedulers_weekday_adapter

import dev.phystech.mipt.models.ScheduleItem

sealed class SchedulerWeekdayAdapterItem {

    abstract fun getType(): SchedulerWeekdayAdapterItemType

    class Item(val model: ScheduleItem): SchedulerWeekdayAdapterItem() {
        override fun getType(): SchedulerWeekdayAdapterItemType = SchedulerWeekdayAdapterItemType.Item

    }

    class Header(val title: String): SchedulerWeekdayAdapterItem() {
        override fun getType(): SchedulerWeekdayAdapterItemType = SchedulerWeekdayAdapterItemType.Header
    }
}
