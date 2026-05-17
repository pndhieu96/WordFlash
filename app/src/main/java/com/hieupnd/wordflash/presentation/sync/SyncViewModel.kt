package com.hieupnd.wordflash.presentation.sync

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hieupnd.wordflash.R
import com.hieupnd.wordflash.domain.repository.AuthRepository
import com.hieupnd.wordflash.domain.usecase.sync.SyncDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository,
    private val syncDataUseCase: SyncDataUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(
        application,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    fun onGoogleSignInResult(account: GoogleSignInAccount?) {
        val idToken = account?.idToken
        if (idToken == null) {
            _uiState.update { it.copy(syncError = "Không lấy được token đăng nhập") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null) }
            authRepository.signInWithGoogle(idToken)
                .onSuccess { _uiState.update { it.copy(isSyncing = false) } }
                .onFailure { e -> _uiState.update { it.copy(isSyncing = false, syncError = e.message) } }
        }
    }

    fun onGoogleSignInError(message: String?) {
        if (message != null) _uiState.update { it.copy(syncError = message) }
    }

    fun sync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncError = null, syncResult = null) }
            syncDataUseCase()
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(isSyncing = false, lastSyncTime = System.currentTimeMillis(), syncResult = result)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSyncing = false, syncError = e.message ?: e.toString()) }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleSignInClient.signOut().await()
            authRepository.signOut()
            _uiState.update { it.copy(lastSyncTime = null, syncResult = null) }
        }
    }

    fun clearError() = _uiState.update { it.copy(syncError = null) }
    fun clearSyncResult() = _uiState.update { it.copy(syncResult = null) }
}
