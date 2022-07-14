package dev.phystech.mipt.ui.fragments.study

import android.graphics.Color
import androidx.annotation.ColorRes
import dev.phystech.mipt.R
import dev.phystech.mipt.ui.utils.SchedulersColorFactory

class TeacherSchedulersColorFactory: SchedulersColorFactory() {
    override fun getLectionColor(): Int = Color.parseColor("#FF7BBA")
    override fun getSeminarColor(): Int = Color.parseColor("#4D68DA")
    override fun getLaboratoryColor(): Int = Color.parseColor("#FFAB52")
    override fun getConsultationColor(): Int = Color.parseColor("#A282FF")
    override fun getEventColor(): Int = Color.parseColor("#3E89E4")
    override fun getExamColor(): Int = Color.parseColor("#FF785C")
    override fun getHomeworkColor(): Int = Color.parseColor("#52A0FF")

    @ColorRes override fun getLectionColorId(): Int = R.color.teacher_lection
    @ColorRes override fun getSeminarColorId(): Int = R.color.teacher_seminar
    @ColorRes override fun getLaboratoryColorId(): Int = R.color.teacher_laboratory
    @ColorRes override fun getConsultationColorId(): Int = R.color.teacher_consultation
    @ColorRes override fun getEventColorId(): Int = R.color.teacher_event
    @ColorRes override fun getExamColorId(): Int = R.color.teacher_exam
    @ColorRes override fun getHomeworkColorId(): Int = R.color.teacher_homework
}