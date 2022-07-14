package dev.phystech.mipt.utils

import android.view.View

fun Boolean.visibility(falseGone: Boolean = true): Int {
    return when (this) {
        true -> View.VISIBLE
        false -> if (falseGone) View.GONE else View.INVISIBLE
    }
}