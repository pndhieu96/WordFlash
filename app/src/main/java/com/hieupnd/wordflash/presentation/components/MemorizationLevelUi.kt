package com.hieupnd.wordflash.presentation.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.hieupnd.wordflash.R

@get:StringRes
val memorizationLabelRes = listOf(
    R.string.level_0,
    R.string.level_1,
    R.string.level_2,
    R.string.level_3,
    R.string.level_4,
)

/** Dải màu đỏ → xanh, index trùng với cấp độ. */
val memorizationColors = listOf(
    Color(0xFFD32F2F),  // red 700
    Color(0xFFEF6C00),  // orange 800
    Color(0xFFF9A825),  // yellow 800
    Color(0xFF7CB342),  // light green 600
    Color(0xFF388E3C),  // green 700
)

/** Nền vàng cần chữ đen; các cấp còn lại dùng chữ trắng. */
fun contentColorFor(level: Int): Color = if (level == 2) Color.Black else Color.White
