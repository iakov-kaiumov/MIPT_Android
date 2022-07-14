package dev.phystech.mipt.models

enum class EventType(val typeValue: String) {
    Exam("exam"),
    Conference("conference"),
    Quiz("quiz"),
    Assign("assign"),
    Other("other");


    companion object {
        fun getByTypeValue(typeValue: String): EventType? {
            return EventType.values().firstOrNull{ v -> v.typeValue == typeValue}
        }
    }
}