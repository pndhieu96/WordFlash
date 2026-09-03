package com.hieupnd.wordflash.domain.model

/**
 * Thang đánh giá mức độ thuộc bài: 5 cấp, 0 = quên hẳn → 4 = thuộc lòng.
 */
object MemorizationLevel {
    const val MIN = 0
    const val MAX = 4
    const val COUNT = 5

    /** Nhớ càng kém → càng xuất hiện nhiều trong phiên ôn tập. */
    fun weightOf(level: Int): Int = when (level.coerceIn(MIN, MAX)) {
        0 -> 10
        1 -> 7
        2 -> 5
        3 -> 3
        else -> 1
    }
}
