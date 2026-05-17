package com.hieupnd.wordflash.domain.repository

import com.hieupnd.wordflash.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<UserInfo?>
    suspend fun signInWithGoogle(idToken: String): Result<UserInfo>
    suspend fun signOut()
}
