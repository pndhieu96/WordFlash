# TÀI LIỆU YÊU CẦU KỸ THUẬT (PRD) - ỨNG DỤNG FLASHCARD

## 1. Tổng Quan Ứng Dụng

Ứng dụng hỗ trợ học và ghi nhớ từ vựng, mẫu câu thông qua phương pháp Flashcard. Ứng dụng ưu tiên lưu trữ dữ liệu cục bộ (Room Database) và được thiết kế sẵn cấu trúc để có thể đồng bộ hóa lên đám mây (Firebase) trong tương lai.

## 2. Kiến Trúc & Công Nghệ

| Tiêu chí | Chi tiết |
|---------|---------|
| **Platform** | Android (Kotlin) |
| **Architecture** | Clean Architecture + MVVM |
| **UI Framework** | Jetpack Compose + Material 3 |
| **Local Database** | Room Database |
| **Remote Database** | Firebase Firestore (Auth + Firestore — tích hợp Phase 3) |
| **External APIs** | Dictionary API (IPA/audio), Datamuse API (gợi ý từ), Gemini API (nghĩa VI + câu ví dụ; cấu trúc câu mô tả + ví dụ) |

## 3. Các Tính Năng Chính (Core Features)

### 3.1 Màn hình 1: Note Từ Vựng (Tra từ điển & Lưu Flashcard)

Tính năng này cho phép người dùng tìm kiếm từ vựng và chủ động thêm các từ cần học vào bộ sưu tập.

**Chức năng tra cứu:**
- Ô tìm kiếm (Search bar) kết nối với API Từ điển
- Hiển thị kết quả tìm kiếm từng từ
- **Khi không tìm thấy từ**: hiện card lỗi + gợi ý từ Datamuse + nút **"Tự nhập từ thủ công"**

**Nội dung hiển thị của từ:**
- **Từ gốc (Word)**: Từ tiếng Anh cần học
- **Phiên âm (IPA)**: Cách phát âm chuẩn
- **Phát âm (Audio)**: Nút bấm kích hoạt Text-to-Speech (TTS)
- **Nghĩa của từ (Meaning)**: Lấy từ Gemini API (nghĩa tiếng Việt ngắn gọn kèm từ loại, ví dụ: "Con mèo (danh từ)"); chữ đầu viết hoa
- **Ví dụ (Examples)**:
  - Tự động lấy 3 câu ví dụ song ngữ EN+VI từ Gemini API khi tìm từ (hiển thị trạng thái đang tải trong lúc chờ)
  - Người dùng có thể **tự thêm câu ví dụ thủ công** (EN + VI tùy chọn) ngay trong Search tab trước khi lưu
- **Hình ảnh minh họa**:
  - **Ô nhập URL ảnh** — người dùng tự tìm và paste URL ảnh bất kỳ; preview hiển thị ngay bên dưới

**Nhập từ thủ công (khi không có trong từ điển):**
- Nhấn "Tự nhập từ thủ công" → form xuất hiện với: ô Từ tiếng Anh (pre-filled từ search query, có thể sửa), IPA (tuỳ chọn), Nghĩa VI (tuỳ chọn), URL ảnh, thêm câu ví dụ
- Nút "Thêm Flashcard" — enabled khi tên từ không trống
- `VocabularyUiState.isManualEntry: Boolean` + `manualWord: String`; `VocabularyViewModel.enterManualMode()` pre-fill từ searchQuery; `saveVocabularyCard()` xử lý cả 2 nhánh dictionary và manual

**Hành động:**
- Nút "Thêm Flashcard" để lưu toàn bộ thông tin vào Room Database
- Nút "Sửa" để cập nhật:
  - Nghĩa tiếng Việt
  - IPA
  - Ảnh: **ô nhập URL ảnh** hiển thị URL hiện tại; người dùng tự thay URL mới; preview hiển thị bên dưới
  - **Câu ví dụ (Examples)**: Danh sách ví dụ song ngữ EN/VI với nút X để xoá từng ví dụ; input field "Câu tiếng Anh" + "Nghĩa tiếng Việt (tuỳ chọn)" + nút "Thêm ví dụ" để thêm mới
- Nút "Xoá" để xóa flashcard khỏi bộ sưu tập
- **Error handling**: Các thao tác thất bại hiển thị AlertDialog thông báo lỗi; không set trạng thái thành công khi thực ra thất bại

**Chip mức độ ghi nhớ trong bộ sưu tập:**
- Click chip để xoay vòng: Không nhớ (0) → Hơi nhớ (1) → Đã nhớ (2) → Không nhớ (0)

**Hiển thị trong bộ sưu tập:**
- Card từ vựng hiển thị: từ, IPA, nghĩa, và tối đa 3 câu ví dụ đầu tiên (EN italic + VI onSurfaceVariant)
- Phân tách bằng HorizontalDivider nếu có ví dụ

**Lưu ý kỹ thuật:**
- `VocabularyViewModel.searchWord()` kích hoạt 2 coroutine song song: `SearchWordUseCase` → DictionaryApi (IPA, audio, từ loại) và `GetWordInfoFromGeminiUseCase` → GeminiService (nghĩa VI, 3 câu ví dụ EN+VI)
- GeminiService inject `List<GenerativeModel>` (`@Named("gemini_models")`), rotate qua 4 model theo thứ tự: `gemini-2.5-flash-lite` → `gemini-2.5-flash` → `gemini-1.5-flash` → `gemini-1.5-flash-lite`; tất cả dùng `responseMimeType = "application/json"`
- Cơ chế retry + model rotation: mỗi model thử tối đa 3 lần với exponential backoff (1s → 2s → 4s) khi bị `QuotaExceededException`; hết retry thì rotate sang model tiếp; `currentModelIndex` (AtomicInteger) nhớ model đang hoạt động qua các lần gọi
- GEMINI_API_KEY lưu trong `local.properties` (gitignored), expose qua `BuildConfig.GEMINI_API_KEY`
- Khi không tìm thấy từ, Datamuse API trả về danh sách gợi ý từ (`/sug?s=<query>`)
- `SubcomposeAsyncImage` từ Coil library — `WordFlashAsyncImage` wrapper với retry tự động (tối đa 3 lần, 500ms delay); nền errorContainer khi lỗi
- Ảnh hiển thị với `aspectRatio(4f/3f)` và `ContentScale.Fit` xuyên suốt app
- Ảnh trong CollectionTab: `fillMaxWidth(0.67f)` + `aspectRatio(4f/3f)` + `ContentScale.Fit`
- `imageUrl` lưu URL do người dùng nhập thủ công (`customImageUrl` trong UiState); không có tự động lấy ảnh
- Chữ đầu của từ tiếng Anh tự động viết hoa (`replaceFirstChar { it.uppercaseChar() }`)
- Edit dialog có `verticalScroll` để cuộn khi nội dung dài (ví dụ nhiều)
- `Example` model: `enSentence: String`, `viSentence: String` — serialized JSON trong Room
- Tab "Bộ sưu tập" hiển thị trước (index 0), tab "Tìm kiếm" là index 1

### 3.2 Màn hình 2: Note Cấu Trúc Câu (Sắp xếp & Tạo câu)

Tính năng giúp người dùng hiểu cấu trúc ngữ pháp và cách đặt câu bằng cách tương tác trực quan.

**Tạo câu bằng Component Selector (3 tab):**

Phần chọn thành phần câu được chia thành 3 tab riêng biệt trong giao diện tạo cấu trúc:

#### Tab 1: Loại từ (Word Types)
- FlowRow chứa FilterChips cho 12 loại từ tiếng Anh:
  Noun, Verb, Adjective, Adverb, Pronoun, Preposition, Conjunction, Article, Determiner, Numeral, Interjection, Particle
- Nhấn chip → AnimatedVisibility panel hiển thị: tên EN, tên VI, định nghĩa, ghi chú vị trí
- Nút "+" trong panel để thêm loại từ vào cấu trúc

#### Tab 2: Thành phần câu (Sentence Roles)
- FlowRow chứa FilterChips cho 11 thành phần ngữ pháp:

| Key | Tên EN | Tên VI |
|-----|--------|--------|
| `subject` | Subject | Chủ ngữ |
| `predicate` | Predicate | Vị ngữ |
| `direct_object` | Direct Object | Tân ngữ trực tiếp |
| `indirect_object` | Indirect Object | Tân ngữ gián tiếp |
| `complement` | Complement | Bổ ngữ |
| `adverbial` | Adverbial | Trạng ngữ |
| `modifier` | Modifier | Định ngữ |
| `appositive` | Appositive | Đồng vị ngữ |
| `relative_clause` | Relative Clause | Mệnh đề quan hệ |
| `noun_clause` | Noun Clause | Mệnh đề danh từ |
| `adverb_clause` | Adverb Clause | Mệnh đề trạng từ |

- Nhấn chip → AnimatedVisibility panel hiển thị: tên EN, tên VI, mô tả chi tiết
- Nút "+" trong panel để thêm vào cấu trúc

#### Tab 3: Tùy chỉnh (Custom)
- Input field "Tên thành phần" (bắt buộc) — VD: Phrasal Verb, Cụm danh từ
- Input field "Mô tả (tuỳ chọn)"
- Nút "Thêm vào cấu trúc" — enabled khi tên không rỗng
- Thành phần tùy chỉnh hiển thị bằng tên người dùng nhập, không có viName trong chip

**Model `StructureItem`:**
```
displayName: String  — tên hiển thị trong chip và preview
category: String     — "wordtype" | "role" | "custom"
viName: String       — tên tiếng Việt (rỗng với custom)
description: String  — mô tả (từ data hoặc người dùng nhập)
```

**Cấu trúc đang xây dựng:**
- Hiển thị danh sách `StructureItem` đã chọn dưới dạng InputChip
- Chip label: `"displayName (viName)"` nếu có viName, ngược lại chỉ `displayName`
- Mỗi chip có nút X để xóa theo index
- Preview câu: join các `displayName` bằng " + "

**Lưu trữ & Thêm thông tin:**
- **Mô tả (Description)**: Giải thích về ngữ pháp, cấu trúc hoặc lưu ý
- **Ví dụ liên quan**: Danh sách các câu ví dụ thực tế
- **Nút "Tự động điền từ Gemini"**: Hiển thị khi đã có ít nhất 1 thành phần trong cấu trúc; gọi `GeminiService.getSentenceStructureInfo()` để tự động điền mô tả cách dùng + 3 câu ví dụ song ngữ EN+VI; hiển thị CircularProgressIndicator khi đang tải
- Nút "Lưu cấu trúc" để lưu vào Room Database

**Quản lý bộ sưu tập:**
- Tab "Bộ sưu tập" là tab đầu tiên (index 0): Hiển thị danh sách câu đã lưu
- Nút "Sửa" để cập nhật:
  - Mô tả (Description)
  - Ví dụ liên quan (RelatedExamples) — thêm/xoá từng ví dụ
- Nút "Xoá" để xóa câu
- Hiển thị mức độ ghi nhớ với **màu border** khác nhau (không đổi màu nền card)
- **Chip mức độ ghi nhớ clickable**: click để xoay vòng 0 → 1 → 2 → 0
- **Error handling**: Thao tác thất bại hiển thị thông báo lỗi trong `error` state

**Lưu ý kỹ thuật:**
- Tab ngoài: **Bộ sưu tập** (index 0) và **Tạo cấu trúc câu** (index 1); tab trong: 3 loại component (Word Types / Sentence Roles / Custom)
- `StructureItem` thay thế `List<String>` cũ — linh hoạt hơn, không phụ thuộc lookup từ static data
- `focusedWordType` và `focusedSentenceRole` là 2 state riêng biệt; chuyển tab sẽ clear cả 2
- `selectedComponentTab` reset focused state khi chuyển tab
- FlowRow với `@OptIn(ExperimentalLayoutApi::class)` trên từng composable riêng
- `CustomTabContent` có `LocalFocusManager` để clear focus khi submit
- `GeminiSentenceInfo` domain model: `description: String`, `examples: List<Example>`
- `GetSentenceInfoFromGeminiUseCase` → `SentenceRepository.getSentenceInfoFromGemini()` → `GeminiService.getSentenceStructureInfo()` — reuses `GeminiWordInfoDto` (field "meaning" → description, field "examples" → examples list)
- `SentenceViewModel.generateFromGemini()` sets `isLoadingGemini = true`, calls use case, updates `description` + `relatedExamples` trên success

### 3.3 Màn hình 3: Học & Ghi Nhớ (Spaced Repetition / Học thông minh)

Hệ thống quản lý việc hiển thị Flashcard (cả Từ và Câu) dựa trên mức độ thuộc bài của người dùng.

**Đánh giá mức độ thuộc (5 Cấp độ):**

Khi một Flashcard hiện lên, người dùng sẽ chọn 1 trong 5 trạng thái:

| Trạng thái | Mức độ | Mô tả |
|-----------|-------|-------|
| Quên hẳn | 0 | Chưa thuộc, cần học lại ngay |
| Rất khó | 1 | Nhớ mang máng, phải cố lắm mới ra |
| Khó | 2 | Nhớ được nhưng còn chậm |
| Dễ | 3 | Nhớ khá nhanh, chỉ cần nhắc lại |
| Thuộc lòng | 4 | Đã thuộc, chỉ ôn định kỳ |

Nguồn duy nhất của thang điểm: `domain/model/MemorizationLevel.kt` (MIN/MAX/COUNT +
`weightOf`) và `presentation/components/MemorizationLevelUi.kt` (nhãn + dải màu).

**Thuật toán hiển thị (Thuật toán ưu tiên):**

Hệ thống sẽ dựa vào cấp độ này để phân phối tần suất xuất hiện của Flashcard trong các phiên học tiếp theo:

`weight = baseWeight(level) + daysSinceLastReview`, với `baseWeight` giảm dần theo cấp độ:

| Mức độ | 0 | 1 | 2 | 3 | 4 |
|-------|---|---|---|---|---|
| baseWeight | 10 | 7 | 5 | 3 | 1 |

- **Ưu tiên cao nhất**: "Quên hẳn" → Xuất hiện liên tục và nhiều nhất
- **Ưu tiên thấp nhất**: "Thuộc lòng" → Xuất hiện rất ít (chỉ để nhắc nhở định kỳ)

**Progress bar:**
- Hiển thị `(currentIndex + 1) / totalItems` — khớp với text "1/20" ngay từ card đầu tiên

**Giữ session khi back/chuyển tab:**
- `ReviewViewModel` được scope ở cấp Activity (`hiltViewModel(LocalContext.current as ComponentActivity)`) thay vì NavBackStackEntry, nên ViewModel không bị clear khi user điều hướng sang tab khác rồi quay lại — session tiếp tục đúng card đang xem

**Thông báo nhắc học → điều hướng về màn hình Ôn tập:**
- `NotificationHelper.showReminder()` đính kèm `PendingIntent` với `action = ACTION_NAVIGATE_TO_REVIEW` trỏ về `MainActivity`
- `MainActivity` xử lý intent trong `onCreate` và `onNewIntent` (launchMode `singleTop`), truyền `pendingRoute` xuống `AppNavigation` qua `LaunchedEffect`

### 3.4 Màn hình 4: Thống Kê (Stats)

Tab thứ 4 trong bottom navigation, hiển thị tiến độ học tập theo ngày.

**Streak (Chuỗi ngày liên tục):**
- Card hiển thị: icon lửa + "X ngày liên tiếp" + "Kỷ lục: Y ngày"
- Logic: mỗi khi hoàn thành phiên ôn tập (`markStudiedToday()`), so sánh `lastStudyDate` với hôm qua
  - Nếu học hôm qua → `currentStreak + 1`
  - Nếu gián đoạn → reset về 1
  - Nếu đã đánh dấu hôm nay rồi → không thay đổi
- Lưu trong SharedPreferences: `KEY_CURRENT_STREAK`, `KEY_LONGEST_STREAK`
- Migration: khi app khởi động, nếu `KEY_LAST_STUDY_DATE == today` và `KEY_CURRENT_STREAK` chưa tồn tại → tự khởi tạo streak = 1

**Biểu đồ hoạt động 7 ngày:**
- Mỗi ngày hiển thị 3 thanh màu: **từ thêm** (xanh lá), **câu thêm** (xanh dương), **lượt ôn** (cam)
- Vẽ bằng Compose `Canvas` — không cần thư viện ngoài
- Label ngày dưới mỗi cột; ngày hôm nay dùng màu primary + label "Hôm nay"
- Nguồn dữ liệu: query Room theo trường `createdAt` (từ/câu thêm) và `lastReviewedAt` (lượt ôn) trong khoảng từng ngày

**Lưu ý kỹ thuật:**
- `StatsViewModel` inject `GetDailyStatsUseCase` + SharedPreferences (đọc streak)
- `GetDailyStatsUseCase(days = 7)`: với mỗi ngày, query `VocabularyCardDao.getCardsCreatedBetween()`, `SentenceCardDao.getCardsCreatedBetween()`, `VocabularyCardDao.getCardsReviewedBetween()` → trả về `List<DailyStats>`
- `DailyStats` domain model: `date: LocalDate`, `vocabAdded: Int`, `sentencesAdded: Int`, `reviewCount: Int`
- `StatsScreen` gọi `viewModel.loadStats()` qua `LaunchedEffect(Unit)` để reload khi quay lại tab

## 4. Thiết Kế Cơ Sở Dữ Liệu (Database Design - Room to Firebase)

Để đảm bảo sau này push dữ liệu lên Firebase mượt mà, cấu trúc bảng trong Room cần được thiết kế đồng bộ (sử dụng các trường ID duy nhất dạng String/UUID thay vì tự tăng Int của Room).

### 4.1 Bảng VocabularyCard (Lưu từ vựng)

```
id: String (Primary Key - UUID để tránh xung đột khi lên Firebase)
word: String
ipa: String
audioUrl: String (hoặc cờ để dùng TTS)
meaning: String
wordType: String (loại từ: noun, verb, adjective, ...)
examples: String (JSON serialized List<Example> — mỗi Example: enSentence, viSentence)
imageUrl: String (URL ảnh do người dùng tự nhập)
memorizationLevel: Int (0: Không nhớ, 1: Hơi nhớ, 2: Đã nhớ)
updatedAt: Long (Timestamp để đồng bộ sau này)
lastReviewedAt: Long (Thời gian ôn tập gần nhất)
createdAt: Long (Timestamp khi thêm từ lần đầu — dùng cho thống kê; existing records migrate = updatedAt)
isSynced: Boolean (Cờ đồng bộ Firebase, mặc định = false)
```

### 4.2 Bảng SentenceCard (Lưu câu)

```
id: String (Primary Key - UUID)
sentence: String (Preview câu — join displayName của các StructureItem bằng " + ")
description: String (Mô tả, chú thích cấu trúc)
relatedExamples: String (JSON serialized List<Example> — mỗi Example: enSentence, viSentence)
memorizationLevel: Int (0, 1, 2 tương tự từ vựng)
updatedAt: Long (Timestamp)
lastReviewedAt: Long (Thời gian ôn tập gần nhất)
createdAt: Long (Timestamp khi thêm câu lần đầu — dùng cho thống kê; existing records migrate = updatedAt)
isSynced: Boolean (Cờ đồng bộ Firebase, mặc định = false)
```

> **Lưu ý**: `StructureItem` chỉ tồn tại ở tầng Presentation (UiState) khi đang tạo cấu trúc. Sau khi lưu, chỉ field `sentence` (string) được persist. Nếu cần lưu cấu trúc chi tiết để edit lại, cần thêm field JSON cho `structureItems` trong tương lai.

### 4.3 Room Migrations

| Version | Nội dung |
|---------|----------|
| v1 → v2 | Thêm `wordType`, `imageUrl`, `lastReviewedAt` vào `vocabulary_cards`; thêm `lastReviewedAt` vào `sentence_cards` |
| v2 → v3 | Thêm `createdAt` vào cả hai bảng; existing records: `createdAt = updatedAt` |

### 4.4 Lưu ý thiết kế dành cho Firebase integration

- Thêm trường `isSynced: Boolean` (Default = false) ở mỗi bảng trong Room
- Khi push lên Firebase thành công, cập nhật trường này thành true
- Dùng cấu trúc Document-Client chuẩn để khi chuyển đổi sang Firestore chỉ cần map trực tiếp Object từ Entity của Room sang Firebase Model

## 5. Công Nghệ & Dependencies

### APIs được sử dụng
- **Dictionary API**: https://api.dictionaryapi.dev/ (Từ điển tiếng Anh — IPA, audio URL, từ loại)
- **Gemini API**: Google AI SDK — model rotation `gemini-2.5-flash-lite` → `gemini-2.5-flash` → `gemini-1.5-flash` → `gemini-1.5-flash-lite` (auto retry + rotate khi rate limited); (1) tra từ vựng: trả về nghĩa VI + 3 câu ví dụ EN+VI; (2) cấu trúc câu: trả về mô tả cách dùng + 3 câu ví dụ EN+VI. API key lưu trong `local.properties` (`GEMINI_API_KEY`), không commit
- **Datamuse API**: https://api.datamuse.com/sug (Gợi ý từ khi không tìm thấy trong từ điển)

### Libraries chính
- **Retrofit**: HTTP client
- **Room**: Local database
- **Jetpack Compose**: UI framework
- **Coil** (`SubcomposeAsyncImage`): Image loading với loading/error state tích hợp
- **Hilt**: Dependency injection
- **Google AI Android SDK** (`com.google.ai.client.generativeai:generativeai:0.9.0`): Gemini API client
- **Kotlin Coroutines**: Async programming (bao gồm `async` song song cho Dictionary + Gemini)
- **Material 3**: Design system
- **Firebase BOM 33.13.0**: Quản lý version Firebase
- **Firebase Auth KTX**: Xác thực người dùng (Google Sign-In)
- **Firebase Firestore KTX**: Cloud database đồng bộ
- **Google Play Services Auth 21.3.0**: Google Sign-In client
- **Google Services Gradle Plugin 4.4.2**: Tự sinh `R.string.default_web_client_id` từ `google-services.json`

## 6. Trạng Thái Triển Khai

### Hoàn thành ✅
- [x] Màn hình Vocabulary với tìm kiếm từ điển
- [x] Lưu/Sửa/Xoá flashcard từ vựng
- [x] Edit dialog - đổi ảnh (URL thủ công), nghĩa, IPA
- [x] **Edit dialog - CRUD câu ví dụ song ngữ (EN + VI)** _(Phase 2)_
- [x] **Hiển thị câu ví dụ trong card bộ sưu tập** _(Phase 2)_
- [x] Màn hình Sentence Builder với word type chips
- [x] FlowRow UI với animated info panel
- [x] Lưu/Sửa/Xoá sentence cards
- [x] Edit dialog - quản lý ví dụ (thêm/xoá)
- [x] **Sentence Builder: 3-tab component selector** _(Phase 2)_
- [x] **11 thành phần câu ngữ pháp (Sentence Roles)** _(Phase 2)_
- [x] **Custom component — tự nhập tên + mô tả** _(Phase 2)_
- [x] **`StructureItem` model thay thế `List<String>`** _(Phase 2)_
- [x] Màn hình Review/Learning với spaced repetition algorithm
- [x] Flip card animation, 3-level rating system
- [x] Collection tabs hiển thị ảnh và ví dụ
- [x] **Border-only color indicator cho Vocabulary và Sentence cards** _(Phase 3)_
- [x] **Viết hoa chữ đầu từ tiếng Anh** _(Phase 3)_
- [x] **Firebase Authentication — Google Sign-In** _(Phase 3)_
- [x] **Firebase Firestore sync — Vocabulary & Sentence cards** _(Phase 3)_
- [x] **App logo — Vector adaptive icon** _(Phase 3)_
- [x] **SubcomposeAsyncImage — loading indicator + error state cho toàn bộ ảnh** _(Phase 4)_
- [x] **Nhập URL ảnh thủ công trong Search tab và Edit dialog (không có tự động tìm ảnh)** _(Phase 4)_
- [x] **Thêm câu ví dụ thủ công trong Search tab (khi thêm từ mới)** _(Phase 4)_
- [x] **Chip mức độ ghi nhớ clickable — xoay vòng 0→1→2→0** _(Phase 4)_
- [x] **Error handling: save/edit/delete hiển thị dialog lỗi khi thất bại** _(Phase 4)_
- [x] **Fix progress bar Review: `(currentIndex+1)/totalItems`** _(Phase 4)_
- [x] **Fix crash risk: `levelLabels.getOrElse(...)` thay vì direct array access** _(Phase 4)_
- [x] **Tích hợp Gemini API — nghĩa VI + 3 câu ví dụ EN/VI cho tra từ vựng** _(Phase 5)_
- [x] **Datamuse API — gợi ý từ khi không tìm thấy** _(Phase 5)_
- [x] **Ảnh hiển thị `aspectRatio(4f/3f)` + `ContentScale.Fit` xuyên suốt; CollectionTab ảnh 2/3 width** _(Phase 5)_
- [x] **Tab "Bộ sưu tập" hiển thị trước "Tìm kiếm" trong màn hình Vocabulary** _(Phase 5)_
- [x] **Gemini retry + model rotation: exponential backoff → rotate qua 4 model khi rate limited** _(Phase 5)_

### Hoàn thành Phase 8 ✅
- [x] **Fix: Nhấn back/chuyển tab giữa phiên ôn tập không bị reload — activity-scoped ViewModel** _(Phase 8)_
- [x] **Fix: Nhấn thông báo nhắc học mở thẳng màn hình Ôn tập (PendingIntent + onNewIntent)** _(Phase 8)_
- [x] **Streak: đếm số ngày ôn tập liên tục, lưu SharedPreferences, migration tự động** _(Phase 8)_
- [x] **Tab Thống kê (tab thứ 4): streak card + biểu đồ thanh 7 ngày (từ/câu/lượt ôn)** _(Phase 8)_
- [x] **DB migration v2→v3: thêm `createdAt` vào vocabulary_cards và sentence_cards** _(Phase 8)_
- [x] **`GetDailyStatsUseCase` + `DailyStats` domain model + `StatsViewModel/Screen`** _(Phase 8)_
- [x] **Tự nhập từ thủ công khi từ không có trong từ điển (nút + form `ManualEntryCard`)** _(Phase 8)_
- [x] **Fix schema: `relatedExamples` đúng là `List<Example>` (không phải `List<String>`)** _(Phase 8)_

### Hoàn thành Phase 9 ✅
- [x] **Tìm kiếm từ đã có trong bộ sưu tập: tự chuyển sang tab Bộ sưu tập, lọc + cuộn tới thẻ và highlight, không gọi API** _(Phase 9)_
- [x] **Cho phép cấu hình số thẻ mỗi phiên ôn tập trong Cài đặt (`ReviewSettingsStore`, SharedPreferences `review_prefs`); mặc định 20 từ + 5 câu, phạm vi 5–50 và 0–20** _(Phase 9)_
- [x] **Mở rộng thang mức độ thuộc từ 3 lên 5 cấp (Quên hẳn → Thuộc lòng); gom nhãn/màu/trọng số về một nguồn duy nhất** _(Phase 9)_
- [x] **DB migration v3→v4: ánh xạ giá trị `memorizationLevel` cũ sang thang mới (0→0, 1→2, 2→4)** _(Phase 9)_
- [x] **Đa ngôn ngữ giao diện: tách 201 chuỗi ra `values/strings.xml` (mặc định tiếng Anh) + `values-vi/strings.xml`** _(Phase 9)_
- [x] **Cài đặt → Ngôn ngữ: chọn Tiếng Việt / English (`LanguageStore`, SharedPreferences `language_prefs`, mặc định `vi`); áp dụng qua `attachBaseContext` ở `WordFlashApplication` + `MainActivity`, đổi xong `recreate()`** _(Phase 9)_
- [x] **Chỉ đổi ngôn ngữ giao diện — prompt Gemini và nội dung thẻ vẫn giữ tiếng Việt** _(Phase 9)_

### Cần phát triển 📋
- [ ] Offline support enhancement
- [ ] Performance optimization
- [ ] Lưu `structureItems` chi tiết vào DB để cho phép edit lại cấu trúc câu

### Hoàn thành Phase 6 ✅
- [x] **Nhập/chỉnh sửa IPA khi thêm từ mới trong Search tab** _(Phase 6)_
- [x] **Ôn tập: hiển thị câu ví dụ song ngữ EN+VI trên mặt sau flashcard** _(Phase 6)_
- [x] **Ôn tập: hiển thị trạng thái đã học hôm nay (icon + badge)** _(Phase 6)_
- [x] **Ôn tập: giới hạn phiên 20 từ vựng + 5 cấu trúc câu mỗi ngày** _(Phase 6)_
- [x] **Thông báo nhắc nhở hàng ngày: cài giờ, kiểm tra đã học chưa, WorkManager** _(Phase 6)_
- [x] **Cấu trúc câu: thêm dịch tiếng Việt cho câu ví dụ (List<Example> thay List<String>)** _(Phase 6)_
- [x] **Cấu trúc câu: cho phép chỉnh sửa text cấu trúc trong edit dialog** _(Phase 6)_

### Hoàn thành Phase 7 ✅
- [x] **Xoá toàn bộ tích hợp Google Custom Search; ảnh do người dùng tự nhập URL** _(Phase 7)_
- [x] **Xoá imageKeywords khỏi GeminiWordInfo và Gemini prompt từ vựng** _(Phase 7)_
- [x] **Tab "Bộ sưu tập" hiển thị trước "Tạo cấu trúc câu" trong màn hình Sentence** _(Phase 7)_
- [x] **Gemini tự động điền mô tả + 3 câu ví dụ EN+VI cho cấu trúc câu** _(Phase 7)_
- [x] **`GeminiSentenceInfo` domain model + `GetSentenceInfoFromGeminiUseCase`** _(Phase 7)_
