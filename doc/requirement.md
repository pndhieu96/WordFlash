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
| **External APIs** | Pixabay API (Image Search), Dictionary API, Translation API |

## 3. Các Tính Năng Chính (Core Features)

### 3.1 Màn hình 1: Note Từ Vựng (Tra từ điển & Lưu Flashcard)

Tính năng này cho phép người dùng tìm kiếm từ vựng và chủ động thêm các từ cần học vào bộ sưu tập.

**Chức năng tra cứu:**
- Ô tìm kiếm (Search bar) kết nối với API Từ điển
- Hiển thị kết quả tìm kiếm từng từ

**Nội dung hiển thị của từ:**
- **Từ gốc (Word)**: Từ tiếng Anh cần học
- **Phiên âm (IPA)**: Cách phát âm chuẩn
- **Phát âm (Audio)**: Nút bấm kích hoạt Text-to-Speech (TTS)
- **Nghĩa của từ (Meaning)**: Dịch tự động nghĩa của **từ gốc** (không phải định nghĩa) qua MyMemory API; chữ đầu viết hoa
- **Ví dụ (Examples)**:
  - Tự động lấy tối đa 3 câu ví dụ từ Dictionary API khi tìm từ
  - **Tự động dịch song song** từng câu sang tiếng Việt qua MyMemory API (hiển thị "Đang dịch câu ví dụ..." trong lúc chờ)
  - Người dùng có thể **tự thêm câu ví dụ thủ công** (EN + VI tùy chọn) ngay trong Search tab trước khi lưu
- **Hình ảnh minh họa**:
  - 5 ảnh từ Pixabay API + 1 ô xám "Không có ảnh", người dùng chọn 1 để lưu (hiển thị border + checkmark)
  - **Ô nhập URL ảnh tùy chỉnh** — người dùng paste URL bất kỳ; preview hiển thị ngay bên dưới; ưu tiên dùng URL tùy chỉnh nếu được nhập

**Hành động:**
- Nút "Thêm Flashcard" để lưu toàn bộ thông tin vào Room Database
- Nút "Sửa" để cập nhật:
  - Nghĩa tiếng Việt
  - IPA
  - Ảnh: hiển thị ảnh hiện tại + nút "Tìm ảnh" (chọn 5 ảnh mới từ Pixabay + ô xám) + **ô nhập URL ảnh tùy chỉnh** với nút "Áp dụng"
  - **Câu ví dụ (Examples)**: Danh sách ví dụ song ngữ EN/VI với nút X để xoá từng ví dụ; input field "Câu tiếng Anh" + "Nghĩa tiếng Việt (tuỳ chọn)" + nút "Thêm ví dụ" để thêm mới
- Nút "Xoá" để xóa flashcard khỏi bộ sưu tập
- **Error handling**: Các thao tác thất bại hiển thị AlertDialog thông báo lỗi; không set trạng thái thành công khi thực ra thất bại

**Chip mức độ ghi nhớ trong bộ sưu tập:**
- Click chip để xoay vòng: Không nhớ (0) → Hơi nhớ (1) → Đã nhớ (2) → Không nhớ (0)

**Hiển thị trong bộ sưu tập:**
- Card từ vựng hiển thị: từ, IPA, nghĩa, và tối đa 3 câu ví dụ đầu tiên (EN italic + VI onSurfaceVariant)
- Phân tách bằng HorizontalDivider nếu có ví dụ

**Lưu ý kỹ thuật:**
- Sử dụng Pixabay REST API cho tìm kiếm ảnh (cần free API key); `perPage = 5`
- API key Pixabay lưu trong `local.properties` (gitignored), expose qua `BuildConfig.PIXABAY_API_KEY`
- `SubcomposeAsyncImage` từ Coil library — hiển thị `CircularProgressIndicator` khi đang tải, icon lỗi (nền errorContainer) khi tải thất bại, `crossfade(true)` cho animation mượt
- `ImageSelectionGrid` composable: lưới 3 cột, hàng đầu = ô xám "Không có ảnh" (sentinel `""`) + 5 ảnh Pixabay; dùng `chunked(3)` để render từng hàng
- Ảnh được chọn có border dày (3dp) + checkmark icon
- Chữ đầu của từ tiếng Anh và nghĩa tiếng Việt tự động viết hoa (`replaceFirstChar { it.uppercaseChar() }`)
- Edit dialog có `verticalScroll` để cuộn khi nội dung dài (ví dụ nhiều)
- `Example` model: `enSentence: String`, `viSentence: String` — serialized JSON trong Room
- Dịch câu ví dụ song song với `async { }.awaitAll()` — tối đa 3 lần gọi API đồng thời

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
- Nút "Lưu cấu trúc" để lưu vào Room Database

**Quản lý bộ sưu tập:**
- Tab "Bộ sưu tập": Hiển thị danh sách câu đã lưu
- Nút "Sửa" để cập nhật:
  - Mô tả (Description)
  - Ví dụ liên quan (RelatedExamples) — thêm/xoá từng ví dụ
- Nút "Xoá" để xóa câu
- Hiển thị mức độ ghi nhớ với **màu border** khác nhau (không đổi màu nền card)
- **Chip mức độ ghi nhớ clickable**: click để xoay vòng 0 → 1 → 2 → 0
- **Error handling**: Thao tác thất bại hiển thị thông báo lỗi trong `error` state

**Lưu ý kỹ thuật:**
- Sử dụng `TabRow` lồng nhau: outer tab (Tạo cấu trúc / Bộ sưu tập), inner tab (3 loại component)
- `StructureItem` thay thế `List<String>` cũ — linh hoạt hơn, không phụ thuộc lookup từ static data
- `focusedWordType` và `focusedSentenceRole` là 2 state riêng biệt; chuyển tab sẽ clear cả 2
- `selectedComponentTab` reset focused state khi chuyển tab
- FlowRow với `@OptIn(ExperimentalLayoutApi::class)` trên từng composable riêng
- `CustomTabContent` có `LocalFocusManager` để clear focus khi submit

### 3.3 Màn hình 3: Học & Ghi Nhớ (Spaced Repetition / Học thông minh)

Hệ thống quản lý việc hiển thị Flashcard (cả Từ và Câu) dựa trên mức độ thuộc bài của người dùng.

**Đánh giá mức độ thuộc (3 Cấp độ):**

Khi một Flashcard hiện lên, người dùng sẽ chọn 1 trong 3 trạng thái:

| Trạng thái | Mức độ | Mô tả |
|-----------|-------|-------|
| Không nhớ | 0 | Chưa thuộc, cần học lại ngay |
| Hơi nhớ | 1 | Mơ hồ, cần ôn tập vừa phải |
| Đã nhớ | 2 | Đã thuộc lòng |

**Thuật toán hiển thị (Thuật toán ưu tiên):**

Hệ thống sẽ dựa vào cấp độ này để phân phối tần suất xuất hiện của Flashcard trong các phiên học tiếp theo:

- **Ưu tiên cao nhất**: Trạng thái "Không nhớ" → Xuất hiện liên tục và nhiều nhất
- **Ưu tiên trung bình**: Trạng thái "Hơi nhớ" → Xuất hiện ít hơn nhóm "Không nhớ"
- **Ưu tiên thấp nhất**: Trạng thái "Đã nhớ" → Xuất hiện rất ít (chỉ để nhắc nhở định kỳ)

**Progress bar:**
- Hiển thị `(currentIndex + 1) / totalItems` — khớp với text "1/20" ngay từ card đầu tiên

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
imageUrl: String (URL ảnh từ Pixabay hoặc URL tùy chỉnh do người dùng nhập)
memorizationLevel: Int (0: Không nhớ, 1: Hơi nhớ, 2: Đã nhớ)
updatedAt: Long (Timestamp để đồng bộ sau này)
lastReviewedAt: Long (Thời gian ôn tập gần nhất)
isSynced: Boolean (Cờ đồng bộ Firebase, mặc định = false)
```

### 4.2 Bảng SentenceCard (Lưu câu)

```
id: String (Primary Key - UUID)
sentence: String (Preview câu — join displayName của các StructureItem bằng " + ")
description: String (Mô tả, chú thích cấu trúc)
relatedExamples: String (JSON serialized List<String> các câu ví dụ)
memorizationLevel: Int (0, 1, 2 tương tự từ vựng)
updatedAt: Long (Timestamp)
isSynced: Boolean (Cờ đồng bộ Firebase, mặc định = false)
```

> **Lưu ý**: `StructureItem` chỉ tồn tại ở tầng Presentation (UiState) khi đang tạo cấu trúc. Sau khi lưu, chỉ field `sentence` (string) được persist. Nếu cần lưu cấu trúc chi tiết để edit lại, cần thêm field JSON cho `structureItems` trong tương lai.

### 4.3 Lưu ý thiết kế dành cho Firebase integration

- Thêm trường `isSynced: Boolean` (Default = false) ở mỗi bảng trong Room
- Khi push lên Firebase thành công, cập nhật trường này thành true
- Dùng cấu trúc Document-Client chuẩn để khi chuyển đổi sang Firestore chỉ cần map trực tiếp Object từ Entity của Room sang Firebase Model

## 5. Công Nghệ & Dependencies

### APIs được sử dụng
- **Dictionary API**: https://api.dictionaryapi.dev/ (Từ điển tiếng Anh)
- **Pixabay API**: https://pixabay.com/api/ (Tìm kiếm ảnh - API key lưu trong `local.properties`, không commit)
- **MyMemory Translation API**: https://api.mymemory.translated.net/ (Dịch tiếng Việt — dùng cho cả từ gốc lẫn câu ví dụ)

### Libraries chính
- **Retrofit**: HTTP client
- **Room**: Local database
- **Jetpack Compose**: UI framework
- **Coil** (`SubcomposeAsyncImage`): Image loading với loading/error state tích hợp
- **Hilt**: Dependency injection
- **Kotlin Coroutines**: Async programming (bao gồm `async/awaitAll` cho dịch song song)
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
- [x] Tích hợp Pixabay Image API - hiển thị 5 ảnh + 1 ô xám "không có ảnh" + chọn
- [x] Edit dialog - đổi ảnh, nghĩa, IPA
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
- [x] **Tự động dịch từ gốc + viết hoa chữ đầu** _(Phase 3)_
- [x] **Tự động thêm tối đa 3 câu ví dụ khi lưu** _(Phase 3)_
- [x] **ImageSelectionGrid 5 ảnh + 1 ô xám** _(Phase 3)_
- [x] **Firebase Authentication — Google Sign-In** _(Phase 3)_
- [x] **Firebase Firestore sync — Vocabulary & Sentence cards** _(Phase 3)_
- [x] **App logo — Vector adaptive icon** _(Phase 3)_
- [x] **SubcomposeAsyncImage — loading indicator + error state cho toàn bộ ảnh** _(Phase 4)_
- [x] **Auto-dịch câu ví dụ từ điển sang tiếng Việt khi tìm từ (song song)** _(Phase 4)_
- [x] **Nhập URL ảnh tùy chỉnh trong Search tab và Edit dialog** _(Phase 4)_
- [x] **Thêm câu ví dụ thủ công trong Search tab (khi thêm từ mới)** _(Phase 4)_
- [x] **Chip mức độ ghi nhớ clickable — xoay vòng 0→1→2→0** _(Phase 4)_
- [x] **Error handling: save/edit/delete hiển thị dialog lỗi khi thất bại** _(Phase 4)_
- [x] **Pixabay API key chuyển sang BuildConfig qua local.properties** _(Phase 4)_
- [x] **Fix progress bar Review: `(currentIndex+1)/totalItems`** _(Phase 4)_
- [x] **Fix crash risk: `levelLabels.getOrElse(...)` thay vì direct array access** _(Phase 4)_

### Cần phát triển 📋
- [ ] Offline support enhancement
- [ ] Performance optimization
- [ ] Lưu `structureItems` chi tiết vào DB để cho phép edit lại cấu trúc câu

---

## 7. Lịch Sử Cập Nhật

### Phase 4: Bug Fixes, UX & Security (2026-05-18)

**A. Cải thiện load ảnh (SubcomposeAsyncImage)**

**Lý do:** `AsyncImage` không xử lý trạng thái loading/error, người dùng không biết ảnh đang tải hay đã thất bại.

**Cập nhật:**
- ✅ Tạo composable `WordFlashAsyncImage` wrapper dùng `SubcomposeAsyncImage` + `ImageRequest.Builder` với `crossfade(true)`
- ✅ Hiển thị `CircularProgressIndicator` trong lúc tải
- ✅ Hiển thị icon lỗi (nền `errorContainer`) khi tải thất bại
- ✅ Áp dụng cho: `ImageSelectionGrid`, `VocabularyCardItem`, edit dialog, `ReviewScreen`
- ✅ `ReviewScreen.kt` — thay `AsyncImage` bằng `SubcomposeAsyncImage` trực tiếp

**Files thay đổi:** `VocabularyScreen.kt`, `ReviewScreen.kt`

---

**B. Auto-dịch câu ví dụ từ điển**

**Lý do:** Câu ví dụ từ Dictionary API chỉ có tiếng Anh, người dùng phải tự dịch thủ công.

**Cập nhật:**
- ✅ `VocabularyViewModel.kt` — thêm `translateExamples(sentences)`: gọi `async { translateWordUseCase(sentence) }.awaitAll()` song song cho tối đa 3 câu
- ✅ `VocabularyUiState.kt` — thêm `dictionaryExamples: List<Example>`, `isTranslatingExamples: Boolean`
- ✅ `VocabularyScreen.kt` — SearchTab hiển thị "Đang dịch câu ví dụ..." khi đang chờ; hiển thị kết quả EN + VI trong card surfaceVariant
- ✅ `saveVocabularyCard()` sử dụng `dictionaryExamples + manualExamples` thay vì extract tại thời điểm lưu

**Files thay đổi:** `VocabularyViewModel.kt`, `VocabularyUiState.kt`, `VocabularyScreen.kt`

---

**C. Nhập URL ảnh tùy chỉnh**

**Lý do:** Người dùng muốn dùng ảnh từ nguồn bên ngoài Pixabay thay vì bị giới hạn 5 ảnh gợi ý.

**Cập nhật:**
- ✅ `VocabularyUiState.kt` — thêm `customImageUrl: String`
- ✅ `VocabularyViewModel.kt` — thêm `onCustomImageUrlChange(url)`; `saveVocabularyCard()` ưu tiên `customImageUrl` nếu không rỗng
- ✅ `VocabularyScreen.kt` — SearchTab: field "Hoặc nhập URL ảnh tùy chỉnh" + preview `WordFlashAsyncImage` ngay bên dưới
- ✅ Edit dialog: field "Hoặc nhập URL ảnh" + nút "Áp dụng" để set vào `editImageUrl`; nút "Xoá ảnh" nếu đang có ảnh

**Files thay đổi:** `VocabularyViewModel.kt`, `VocabularyUiState.kt`, `VocabularyScreen.kt`

---

**D. Thêm câu ví dụ thủ công khi thêm từ**

**Lý do:** Search tab chỉ hiển thị câu ví dụ từ từ điển, người dùng không thể thêm câu ví dụ riêng trước khi lưu.

**Cập nhật:**
- ✅ `VocabularyUiState.kt` — thêm `manualExamples: List<Example>`
- ✅ `VocabularyViewModel.kt` — thêm `addManualExample(example)`, `removeManualExample(index)`; reset khi tìm từ mới
- ✅ `VocabularyScreen.kt` — SearchTab: phần "Câu ví dụ" gồm danh sách từ điển (surfaceVariant) + danh sách thủ công (secondaryContainer, có nút xoá) + input EN/VI + nút "Thêm câu ví dụ"
- ✅ `LaunchedEffect(word)` reset local state `newExampleEn/newExampleVi` khi từ thay đổi

**Files thay đổi:** `VocabularyViewModel.kt`, `VocabularyUiState.kt`, `VocabularyScreen.kt`

---

**E. Chip mức độ ghi nhớ clickable**

**Lý do:** Chip trong Collection tab bị `enabled = false` — người dùng không thể đổi mức độ từ màn Collection, phải vào Review mới đổi được.

**Cập nhật:**
- ✅ `VocabularyScreen.kt` — `VocabularyCardItem`: `AssistChip(onClick = { onUpdateLevel(card.id, (card.memorizationLevel + 1) % 3) }, enabled = true)`
- ✅ `SentenceScreen.kt` — card item trong `SentenceCollectionTab`: tương tự
- ✅ Cả 2 màn hình: dùng `levelLabels.getOrElse(...)` thay vì `levelLabels[index]`

**Files thay đổi:** `VocabularyScreen.kt`, `SentenceScreen.kt`

---

**F. Error Handling cho save/edit/delete**

**Lý do:** Các thao tác DB thất bại âm thầm — người dùng thấy thành công dù thực ra lỗi.

**Cập nhật:**
- ✅ `VocabularyViewModel.kt` — wrap `saveVocabularyCard()`, `saveEdit()`, `confirmDelete()` bằng `runCatching { }.onSuccess { }.onFailure { }`; `isSaved = true` chỉ set khi thành công
- ✅ `VocabularyUiState.kt` — thêm `saveError: String? = null`; thêm `clearSaveError()`
- ✅ `VocabularyScreen.kt` — thêm `AlertDialog` khi `uiState.saveError != null`
- ✅ `SentenceViewModel.kt` — wrap `saveSentence()`, `saveEdit()`, `confirmDelete()` tương tự; dùng field `error` có sẵn
- ✅ `SentenceScreen.kt` — error dialog đã có, tự động nhận lỗi từ save/delete

**Files thay đổi:** `VocabularyViewModel.kt`, `VocabularyUiState.kt`, `VocabularyScreen.kt`, `SentenceViewModel.kt`

---

**G. Bảo mật API Key**

**Lý do:** Pixabay API key hardcode trong `AppConfig.kt` có thể bị lộ khi public repo.

**Cập nhật:**
- ✅ `local.properties` — thêm `PIXABAY_API_KEY=...` (file đã có trong `.gitignore`)
- ✅ `app/build.gradle.kts` — bật `buildConfig = true`; đọc key từ `local.properties` bằng `readLines()`, expose qua `buildConfigField`
- ✅ `AppConfig.kt` — đổi từ `const val` hardcode sang `val PIXABAY_API_KEY: String get() = BuildConfig.PIXABAY_API_KEY`

**Files thay đổi:** `AppConfig.kt`, `app/build.gradle.kts`, `local.properties`

---

**H. Fix progress bar Review**

**Lý do:** Text hiển thị "1/20" nhưng progress bar ở 0% khi đang xem card đầu tiên — gây nhầm lẫn.

**Cập nhật:**
- ✅ `ReviewUiState.kt` — đổi `currentIndex.toFloat() / totalItems` → `(currentIndex + 1).toFloat() / totalItems.coerceAtLeast(1)`

**Files thay đổi:** `ReviewUiState.kt`

**Build Status:** ✅ BUILD SUCCESSFUL

---

### Phase 3: Firebase Sync, UX Polish & App Icon (2026-05-17)

**A. Border-only Color Indicator**

**Lý do:** Đổi màu nền cả card làm giảm khả năng đọc nội dung; chỉ đổi màu border giữ nội dung rõ ràng hơn.

**Cập nhật:**
- ✅ `VocabularyScreen.kt` — `VocabularyCardItem`: xoá `containerColor`, thêm `Modifier.border(2.dp, levelColor, shape)` trên Card
- ✅ `SentenceScreen.kt` — tương tự Vocabulary

---

**B. Auto-translate, Auto-capitalize, Limit Examples**

**Lý do:** Giảm thao tác thủ công khi lưu thẻ — nghĩa tiếng Việt, viết hoa, và câu ví dụ tự điền sẵn.

**Cập nhật:**
- ✅ `VocabularyViewModel.kt` — dịch **từ gốc** (không phải định nghĩa) qua `translateDefinition(entry.word)`
- ✅ Viết hoa chữ đầu từ tiếng Anh: `entry.word.replaceFirstChar { it.uppercaseChar() }`
- ✅ Viết hoa chữ đầu nghĩa tiếng Việt sau khi dịch
- ✅ Tự thêm tối đa 3 câu ví dụ từ Dictionary API khi lưu (`.take(3)`)

---

**C. ImageSelectionGrid 5+1**

**Lý do:** 3 ảnh quá ít để chọn; thêm ô "không có ảnh" để người dùng chủ động bỏ chọn ảnh.

**Cập nhật:**
- ✅ `ImageSearchApi.kt` — `perPage` đổi từ 3 → 5
- ✅ `VocabularyScreen.kt` — composable `ImageSelectionGrid`: lưới 3 cột, `listOf("") + images.take(5)`, ô xám sentinel `""`

---

**D. Firebase Sync**

**Lý do:** Đồng bộ dữ liệu giữa các thiết bị, backup lên cloud để không mất dữ liệu.

**Thiết kế đồng bộ:**
- Local DB là source of truth
- Nếu local rỗng → restore từ Firestore (thiết bị mới / clear data)
- Nếu local có dữ liệu → card nào thiếu trên local nhưng có trên Firestore → xoá khỏi Firestore (orphan)
- Batch write Firestore tối đa 400 documents / lần (giới hạn an toàn dưới 500)

**Cấu trúc Firestore:**
```
users/{uid}/vocabularyCards/{cardId}
users/{uid}/sentenceCards/{cardId}
```

**Files thêm mới:**
- `domain/model/UserInfo.kt` — `data class UserInfo(uid, displayName, email, photoUrl)`
- `domain/repository/AuthRepository.kt` — `getCurrentUser(): Flow<UserInfo?>`, `signInWithGoogle()`, `signOut()`
- `domain/repository/SyncRepository.kt` — upload/download/delete cho vocab và sentence
- `domain/usecase/sync/SyncDataUseCase.kt` — logic đồng bộ đầy đủ
- `data/remote/firebase/FirebaseAuthRepositoryImpl.kt` — `callbackFlow` với `AuthStateListener`
- `data/remote/firebase/FirebaseSyncRepositoryImpl.kt` — Firestore batch write, chunked 400
- `di/FirebaseModule.kt` — provides `FirebaseAuth`, `FirebaseFirestore`
- `presentation/sync/SyncUiState.kt` — `currentUser`, `isSyncing`, `syncError`, `lastSyncTime`, `syncResult`
- `presentation/sync/SyncViewModel.kt` — `sync()`, `signOut()`, `onGoogleSignInResult()`, `clearError()`, `clearSyncResult()`

**Files cập nhật:**
- `VocabularyCardDao.kt`, `SentenceCardDao.kt` — thêm `getAllOnce()`, `markAllSynced()`
- `VocabularyRepository.kt`, `SentenceRepository.kt` — interface mới
- `VocabularyRepositoryImpl.kt`, `SentenceRepositoryImpl.kt` — implement
- `di/RepositoryModule.kt` — bind `AuthRepository`, `SyncRepository`
- `VocabularyScreen.kt` — TopAppBar với sync icon, người dùng icon + DropdownMenu, dialog kết quả sync

**Setup Firebase (4 bước):**
1. Tạo Firebase project
2. Thêm Android app → tải `google-services.json` → đặt vào `app/`
3. Bật Authentication → Google
4. Bật Firestore Database
> `R.string.default_web_client_id` được Google Services plugin tự sinh — không cần thủ công copy Web Client ID

---

**E. App Logo — Vector Adaptive Icon**

**Lý do:** App cần icon nhận diện thương hiệu, thể hiện chủ đề flashcard học từ vựng.

**Cập nhật:**
- ✅ `ic_launcher_background.xml` — nền xanh đậm `#1565C0`
- ✅ `ic_launcher_foreground.xml` — thẻ nền xanh nhạt (xoay -10°, `#90CAF9`) + thẻ trắng phía trước + accent bar `#BBDEFB` + tia sét vàng `#FFC107`

**Build Status:** ✅ BUILD SUCCESSFUL

---

### Phase 2: Examples CRUD & Sentence Builder Enhancement (2026-05-17)

**A. Câu ví dụ cho từ vựng (Vocabulary Examples CRUD)**

**Lý do:** Người dùng cần tự thêm/sửa/xoá câu ví dụ cho flashcard từ vựng — tương tự tính năng ví dụ của Sentence Builder.

**Cập nhật:**
- ✅ Edit dialog cho VocabularyCard bổ sung phần "Câu ví dụ"
- ✅ Hiển thị danh sách ví dụ hiện tại (EN italic + VI onSurfaceVariant) với nút X xoá
- ✅ Input field "Câu tiếng Anh" (required) + "Nghĩa tiếng Việt" (optional)
- ✅ Nút "Thêm ví dụ" — enabled khi EN field không rỗng
- ✅ Hiển thị tối đa 3 ví dụ đầu trong VocabularyCardItem (bộ sưu tập)
- ✅ Column trong edit dialog thêm `verticalScroll` để cuộn khi nội dung dài

**Files thay đổi:**
- `VocabularyScreen.kt` — edit dialog, VocabularyCardItem

---

**B. Sentence Builder — 3-tab Component Selector**

**Lý do:** Mở rộng khả năng tạo cấu trúc câu vượt ra ngoài 12 loại từ — thêm thành phần ngữ pháp (Subject, Object...) và cho phép người dùng tự định nghĩa thành phần.

**Cập nhật:**
- ✅ `SentenceRoleData.kt` — mở rộng từ 3 lên 11 `SentenceRole` với `viName` + `description` đầy đủ
- ✅ `SentenceUiState.kt` — thêm `StructureItem` data class; thay `structureTypes: List<String>` bằng `structureItems: List<StructureItem>`; thêm state: `selectedComponentTab`, `focusedSentenceRole`, `customInputName`, `customInputDesc`
- ✅ `SentenceViewModel.kt` — thêm `addSentenceRole()`, `addCustomItem()`, `onSentenceRoleFocused()`, `onComponentTabSelected()`, `onCustomInputNameChange()`, `onCustomInputDescChange()`, `removeStructureItemAt()`
- ✅ `SentenceScreen.kt` — thay FlowRow chip cũ bằng `TabRow` 3 tab; thêm composables `WordTypeTabContent`, `SentenceRoleTabContent`, `CustomTabContent`

**Build Status:** ✅ BUILD SUCCESSFUL

---

### Phase 1: Wikipedia → Pixabay Switch (2026-05-17)
**Lý do thay đổi:** Wikipedia API không trả về ảnh cho nhiều từ vựng; Pixabay API đáng tin cậy hơn

**Cập nhật:**
- ✅ Chuyển từ Wikipedia REST API → Pixabay API
- ✅ Hiển thị 3 ảnh (thumbnail 90x90dp) trong SearchTab
- ✅ User click để chọn 1 ảnh → border dày + checkmark
- ✅ Edit Dialog hiển thị ảnh hiện tại
- ✅ Nút "Thay đổi ảnh" → search Pixabay, chọn 3 ảnh mới
- ✅ Dialog sửa ví dụ câu - thêm/xoá từng ví dụ

**Files thay đổi:**
- `AppConfig.kt` (new) - API key config
- `ImageSearchApi.kt` - Pixabay endpoint
- `NetworkModule.kt` - base URL
- `VocabularyRepositoryImpl.kt` - Pixabay response parsing
- `VocabularyUiState.kt` - editDialogImages, isLoadingEditImages
- `VocabularyViewModel.kt` - searchImagesForEdit(), onSelectEditImage()
- `VocabularyScreen.kt` - 3-image selection, edit dialog ảnh
- `SentenceScreen.kt` - edit dialog ví dụ
- `SentenceUiState.kt` - (existing, used for examples)

**Build Status:** ✅ BUILD SUCCESSFUL
