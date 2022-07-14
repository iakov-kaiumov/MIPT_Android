package edu.phystech.iag.kaiumov.shedule.utils

import android.R.attr
import android.graphics.Color
import dev.phystech.mipt.R
import android.R.attr.opacity

object ColorUtil {
    internal fun getBackgroundDrawable(type: String): Int {
        return when (type) {
            "LAB" -> R.drawable.bg_item_lab
            "SEM" -> R.drawable.bg_item_sem
            "LEC" -> R.drawable.bg_item_lec
            "BOT" -> R.drawable.bg_item_bot
            else -> R.drawable.bg_item_rst
        }
    }

    internal fun getBackgroundColor(type: String): Int {
        return when (type) {
            "LAB" -> R.color.color_practice_bg
            "SEM" -> R.color.color_seminar_bg
            "LEC" -> R.color.color_lecture_bg
            "BOT" -> R.color.color_botay_bg
            else -> R.color.color_rest_bg
        }
    }

    internal fun getTextColor(type: String): Int {
        return when (type) {
            "LAB" -> R.color.color_practice_text
            "SEM" -> R.color.color_seminar_text
            "LEC" -> R.color.color_lecture_text
            "BOT" -> R.color.color_botay_text
            else -> R.color.color_rest_text
        }
    }

    internal fun getColorFromHash(str: String): Int {
        val hexColor = java.lang.String.format("#FF" + "%06X", 0xFFFFFF and str.hashCode())
        return Color.parseColor(hexColor)
    }
}