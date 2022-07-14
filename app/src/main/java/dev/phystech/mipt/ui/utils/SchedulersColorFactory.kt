package dev.phystech.mipt.ui.utils

import android.graphics.Color
import androidx.annotation.ColorRes
import dev.phystech.mipt.R

abstract class SchedulersColorFactory {
    abstract fun getLectionColor(): Int
    abstract fun getSeminarColor(): Int
    abstract fun getLaboratoryColor(): Int
    abstract fun getConsultationColor(): Int
    abstract fun getEventColor(): Int
    abstract fun getExamColor(): Int
    abstract fun getHomeworkColor(): Int

    @ColorRes abstract fun getLectionColorId(): Int
    @ColorRes abstract fun getSeminarColorId(): Int
    @ColorRes abstract fun getLaboratoryColorId(): Int
    @ColorRes abstract fun getConsultationColorId(): Int
    @ColorRes abstract fun getEventColorId(): Int
    @ColorRes abstract fun getExamColorId(): Int
    @ColorRes abstract fun getHomeworkColorId(): Int

    fun getColorByType(type: String): Int {
        return when (type.toLowerCase().trim()) {
            "lab" -> getLaboratoryColor()
            "sem" -> getSeminarColor()
            "lec" -> getLectionColor()
            "consultation", "conference" -> getConsultationColor()
            "event" -> getEventColor()
            "exam", "assign" -> getExamColor()
            else -> getHomeworkColor()
        }
    }

    fun getColorIdByType(type: String): Int {
        return when (type.toLowerCase()) {
            "lab" -> getLaboratoryColorId()
            "sem" -> getSeminarColorId()
            "lec" -> getLectionColorId()
            "consultation", "conference" -> getConsultationColorId()
            "event" -> getEventColorId()
            "exam", "assign" -> getExamColorId()
            else -> getHomeworkColorId()
        }
    }
}