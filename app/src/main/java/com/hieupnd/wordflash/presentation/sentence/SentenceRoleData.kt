package com.hieupnd.wordflash.presentation.sentence

data class SentenceRole(
    val key: String,
    val enName: String,
    val viName: String,
    val description: String
)

object EnglishSentenceRoles {
    val ALL = listOf(
        SentenceRole(
            "subject", "Subject", "Chủ ngữ",
            "Người, vật hoặc ý niệm thực hiện hành động. Thường đứng đầu câu. VD: The cat runs. / She sings."
        ),
        SentenceRole(
            "predicate", "Predicate", "Vị ngữ",
            "Phần câu chứa động từ, mô tả hành động hoặc trạng thái của chủ ngữ. VD: runs quickly / is very happy."
        ),
        SentenceRole(
            "direct_object", "Direct Object", "Tân ngữ trực tiếp",
            "Người/vật nhận trực tiếp hành động, trả lời câu hỏi 'What?' hoặc 'Who?'. VD: She reads a book."
        ),
        SentenceRole(
            "indirect_object", "Indirect Object", "Tân ngữ gián tiếp",
            "Người/vật được hưởng lợi, thường đứng trước tân ngữ trực tiếp. VD: She gave him a gift."
        ),
        SentenceRole(
            "complement", "Complement", "Bổ ngữ",
            "Bổ sung ý nghĩa cho chủ ngữ/tân ngữ qua linking verb (be, seem, become). VD: She is a teacher. / He looks tired."
        ),
        SentenceRole(
            "adverbial", "Adverbial", "Trạng ngữ",
            "Cung cấp thông tin về thời gian, nơi chốn, cách thức, lý do. Vị trí linh hoạt. VD: In the morning, she runs."
        ),
        SentenceRole(
            "modifier", "Modifier", "Định ngữ",
            "Bổ nghĩa cho danh từ, đứng trước hoặc sau danh từ đó. VD: the big red dog / a cup of coffee."
        ),
        SentenceRole(
            "appositive", "Appositive", "Đồng vị ngữ",
            "Cụm từ đứng cạnh và giải thích thêm cho danh từ đứng trước. VD: My friend, a doctor, arrived."
        ),
        SentenceRole(
            "relative_clause", "Relative Clause", "Mệnh đề quan hệ",
            "Mệnh đề bắt đầu bằng who, which, that bổ nghĩa cho danh từ. VD: The book that I read was great."
        ),
        SentenceRole(
            "noun_clause", "Noun Clause", "Mệnh đề danh từ",
            "Mệnh đề giữ chức năng của danh từ (chủ ngữ hoặc tân ngữ). VD: What she said was true."
        ),
        SentenceRole(
            "adverb_clause", "Adverb Clause", "Mệnh đề trạng từ",
            "Mệnh đề bắt đầu bằng because, when, if, although. VD: Because it rained, we stayed home."
        ),
    )
}
