package dev.phystech.mipt.adapters.schedulers_weekday_adapter

enum class SchedulerWeekdayAdapterItemType {
    Header,
    Item;

    companion object {
        fun getByOrdinal(value: Int): SchedulerWeekdayAdapterItemType? {
            return values().firstOrNull { v -> v.ordinal == value }
        }
    }
}