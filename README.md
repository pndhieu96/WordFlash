# WordFlash - Ứng dụng Học Từ Vựng & Cấu Trúc Câu Thông Minh

**WordFlash** là một ứng dụng Android hiện đại được thiết kế để giúp người dùng học và ghi nhớ từ vựng, mẫu câu tiếng Anh một cách hiệu quả thông qua phương pháp **Flashcard** kết hợp với thuật toán **Spaced Repetition (Lặp lại ngắt quãng)**.

## 🚀 Tính Năng Nổi Bật

### 1. Note Từ Vựng & Tra Từ Điển
- **Tra cứu thông minh**: Kết nối trực tiếp với Dictionary API để cung cấp phiên âm (IPA), định nghĩa và phát âm.
- **Minh họa bằng hình ảnh**: Tích hợp **Pixabay API** cho phép tìm kiếm và chọn ảnh minh họa trực quan cho mỗi từ vựng.
- **Dịch tự động**: Hỗ trợ dịch nghĩa tiếng Việt nhanh chóng thông qua Translation API.
- **Ví dụ song ngữ**: Quản lý danh sách các câu ví dụ (Anh-Việt) để hiểu cách dùng từ trong ngữ cảnh.

### 2. Xây Dựng Cấu Trúc Câu (Sentence Builder)
- **Hệ thống mảnh ghép**: Tạo cấu trúc câu bằng cách lắp ghép các thành phần ngữ pháp (Danh từ, Động từ, Chủ ngữ, Tân ngữ...) dưới dạng các **Chip** trực quan.
- **Tùy biến linh hoạt**: Cho phép người dùng tự định nghĩa các thành phần cấu trúc riêng.
- **Ghi chú ngữ pháp**: Lưu trữ mô tả chi tiết và các ví dụ thực tế cho từng cấu trúc câu.

### 3. Ôn Tập Thông Minh (Spaced Repetition)
- **Thuật toán ưu tiên**: Tự động phân phối tần suất xuất hiện của thẻ dựa trên mức độ thuộc bài (Không nhớ - Hơi nhớ - Đã nhớ).
- **Trải nghiệm lật thẻ**: Giao diện thẻ 2 mặt (Flip card) sinh động với hiệu ứng 3D.
- **Căn chỉnh thông minh**: Nội dung thẻ tự động tối ưu hóa không gian hiển thị, hỗ trợ cuộn khi nội dung dài.

## 🛠 Công Nghệ Sử Dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose với Material Design 3
- **Kiến trúc**: Clean Architecture + MVVM
- **Cơ sở dữ liệu**: Room Database (lưu trữ cục bộ, sẵn sàng cho Firebase Sync)
- **Mạng (Networking)**: Retrofit, OkHttp
- **Dependency Injection**: Dagger Hilt
- **Xử lý ảnh**: Coil
- **Đồng bộ**: Coroutines & Flow

## 📈 Lộ Trình Phát Triển (Roadmap)

- [x] Tích hợp Pixabay Image API.
- [x] Hệ thống Spaced Repetition cơ bản.
- [ ] Đồng bộ hóa dữ liệu đám mây với Firebase Firestore.
- [ ] Hệ thống xác thực người dùng (Firebase Auth).
- [ ] Lưu trữ cấu trúc câu chi tiết dưới dạng JSON để chỉnh sửa linh hoạt.
- [ ] Chế độ học Offline nâng cao.
- [ ] Thống kê tiến độ học tập bằng biểu đồ.

---
*Dự án được phát triển với sự hỗ trợ của AI Assistant.*
