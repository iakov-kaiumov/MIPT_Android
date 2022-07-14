package dev.phystech.mipt.utils

import android.view.View
import androidx.core.view.ViewCompat

typealias OnSystemBarsSizeChangedListener = (statusBarSize: Int, navigationBarSize: Int) -> Unit

object InsetUtils {
    fun removeSystemInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            ViewCompat.onApplyWindowInsets(
                view,
                insets.replaceSystemWindowInsets(0, 0, 0, insets.systemWindowInsetBottom)
            )
        }
    }
}


