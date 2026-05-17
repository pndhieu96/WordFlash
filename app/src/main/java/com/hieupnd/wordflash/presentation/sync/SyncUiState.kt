package com.hieupnd.wordflash.presentation.sync

import com.hieupnd.wordflash.domain.model.UserInfo
import com.hieupnd.wordflash.domain.usecase.sync.SyncResult

data class SyncUiState(
    val currentUser: UserInfo? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val lastSyncTime: Long? = null,
    val syncResult: SyncResult? = null
)
