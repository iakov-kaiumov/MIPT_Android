package dev.phystech.mipt.models

data class SchedulerType(
    val type: String,
    var title: String? = null
) {

    override fun toString(): String {
        return title ?: "-"
    }
}