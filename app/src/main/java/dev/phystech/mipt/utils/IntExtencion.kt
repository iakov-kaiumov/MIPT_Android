package dev.phystech.mipt.utils

import android.util.DisplayMetrics
import dev.phystech.mipt.Application

fun Int.dpToPx(displayMetrics: DisplayMetrics = Application.context.resources.displayMetrics): Int = (this * displayMetrics.density).toInt()

fun Int.pxToDp(displayMetrics: DisplayMetrics = Application.context.resources.displayMetrics): Int = (this / displayMetrics.density).toInt()