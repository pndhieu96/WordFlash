package com.hieupnd.wordflash.presentation.sentence

data class WordType(
    val key: String,
    val enName: String,
    val viName: String,
    val description: String,
    val positionNote: String
)

object EnglishWordTypes {
    val ALL = listOf(
        WordType(
            "noun", "Noun", "Danh từ",
            "Người, vật, nơi chốn hoặc ý tưởng. VD: dog, city, love",
            "Vị trí: Đầu câu (chủ ngữ) hoặc sau động từ (tân ngữ). VD: The dog runs. / I love dogs."
        ),
        WordType(
            "verb", "Verb", "Động từ",
            "Hành động hoặc trạng thái. VD: run, is, think",
            "Vị trí: Sau chủ ngữ, là trung tâm của câu. VD: She runs. / He is happy."
        ),
        WordType(
            "adjective", "Adjective", "Tính từ",
            "Mô tả tính chất của danh từ. VD: big, happy, red",
            "Vị trí: Trước danh từ (big dog) hoặc sau động từ 'be' (He is big)."
        ),
        WordType(
            "adverb", "Adverb", "Trạng từ",
            "Bổ nghĩa cho động từ, tính từ hoặc câu. VD: quickly, very, often",
            "Vị trí: Linh hoạt — trước/sau động từ (runs quickly), trước tính từ (very big)."
        ),
        WordType(
            "pronoun", "Pronoun", "Đại từ",
            "Thay thế danh từ đã đề cập. VD: he, they, it, someone",
            "Vị trí: Tương tự danh từ — đầu câu (chủ ngữ) hoặc sau động từ (tân ngữ). VD: She runs."
        ),
        WordType(
            "preposition", "Preposition", "Giới từ",
            "Chỉ mối quan hệ vị trí, thời gian, cách thức. VD: in, on, after, with",
            "Vị trí: Trước danh từ, tạo thành cụm giới từ. VD: in the morning / at the park."
        ),
        WordType(
            "conjunction", "Conjunction", "Liên từ",
            "Nối từ, cụm từ hoặc mệnh đề. VD: and, but, because, although",
            "Vị trí: Giữa hai thành phần cần nối. VD: I run and she walks. / I stayed because it rained."
        ),
        WordType(
            "article", "Article", "Mạo từ",
            "Xác định danh từ là xác định (the) hay không xác định (a/an). VD: a, an, the",
            "Vị trí: Ngay trước danh từ hoặc trước tính từ + danh từ. VD: the dog / a big dog."
        ),
        WordType(
            "determiner", "Determiner", "Từ hạn định",
            "Đứng trước danh từ để giới hạn hoặc xác định nghĩa. VD: this, some, every, my",
            "Vị trí: Trước danh từ (thay thế hoặc bổ sung cho mạo từ). VD: this book / every day."
        ),
        WordType(
            "numeral", "Numeral", "Số từ",
            "Chỉ số lượng hoặc thứ tự. VD: one, three, first, twice",
            "Vị trí: Trước danh từ (three dogs) hoặc sau động từ (He arrived first)."
        ),
        WordType(
            "interjection", "Interjection", "Thán từ",
            "Biểu đạt cảm xúc hoặc phản ứng tức thì. VD: wow, oops, hey, oh",
            "Vị trí: Đứng độc lập đầu câu, thường cách câu chính bằng dấu phẩy hoặc chấm than. VD: Wow, that's great!"
        ),
        WordType(
            "particle", "Particle", "Tiểu từ",
            "Bổ nghĩa cho động từ trong phrasal verb, thay đổi nghĩa hoàn toàn. VD: up, off, out, on",
            "Vị trí: Sau động từ trong phrasal verb. VD: give up / turn off / run out of."
        ),
    )
}
