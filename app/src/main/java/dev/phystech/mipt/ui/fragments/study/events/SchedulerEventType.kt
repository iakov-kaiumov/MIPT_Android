package dev.phystech.mipt.ui.fragments.study.events

import dev.phystech.mipt.R

enum class SchedulerEventType(val typeValue: String) {
    Exam("exam"),
    Conference("conference"),
    Quiz("quiz"),
    Assign("assign"),
    Other("other");

    fun getDysplayedResId(): Int {
        return when (this) {
            Exam -> R.string.schedulter_event_exam
            Conference -> R.string.schedulter_event_conference
            Quiz -> R.string.schedulter_event_quiz
            Assign -> R.string.schedulter_event_assign
            Other -> R.string.schedulter_event_other
        }
    }

    companion object {
        fun getByType(type: String): SchedulerEventType? {
            return SchedulerEventType.values().firstOrNull { v -> v.typeValue == type }
        }
    }
}