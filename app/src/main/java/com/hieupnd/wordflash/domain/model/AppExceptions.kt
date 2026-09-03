package com.hieupnd.wordflash.domain.model

/**
 * Lỗi nghiệp vụ có thông báo hiển thị cho người dùng. Tầng domain/data không giữ
 * chuỗi đã dịch — presentation ánh xạ sang string resource tương ứng.
 */
class NotSignedInException : Exception("Not signed in")

class SignInFailedException : Exception("Sign-in failed")
