package com.example.qevin.presentation.calls

data class CallsUiState(
    val isLoading: Boolean = true,
    val currentUid: String? = null,
    val calls: List<CallUiModel> = emptyList(),
    val isOffline: Boolean = false,
    val errorMessage: String? = null
) {
    val isSignedOut: Boolean
        get() = !isLoading && currentUid == null
}