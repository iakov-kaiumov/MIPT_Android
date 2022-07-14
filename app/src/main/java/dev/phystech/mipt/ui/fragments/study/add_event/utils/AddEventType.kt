package dev.phystech.mipt.ui.fragments.study.add_event.utils

enum class AddEventType(val typeValue: Int) {
    SimpleEvent(1),
    Deadline(2),
    DeadlineEdit(3);

    companion object {
        fun getByTypeValue(value: Int): AddEventType? {
            return values().firstOrNull { v -> v.typeValue == value}
        }
    }
}