package dev.phystech.mipt.ui.utils

import androidx.annotation.ColorRes
import dev.phystech.mipt.R

enum class AvatarColor(@ColorRes val colorResId: Int) {
    Color1(R.color.avatar_color_1),
    Color2(R.color.avatar_color_2),
    Color3(R.color.avatar_color_3),
    Color4(R.color.avatar_color_4),
    Color5(R.color.avatar_color_5),
    Color6(R.color.avatar_color_6);

    companion object {
        fun getByPosition(position: Int): AvatarColor {
            return when (position % 6) {
                0 -> Color1
                1 -> Color2
                2 -> Color3
                3 -> Color4
                4 -> Color5
                5 -> Color6
                else -> Color6
            }
        }
    }

}