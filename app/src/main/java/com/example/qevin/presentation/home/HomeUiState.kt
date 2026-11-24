package com.example.qevin.presentation.home

data class HomeUiState(
    val isLoading: Boolean = true,
    val currentUid: String? = null,
    val conversations: List<ConversationUiModel> = emptyList(),
    val isOffline: Boolean = false,
    val errorMessage: String? = null
) {
    val isSignedOut: Boolean
        get() = !isLoading && currentUid == null

    val isEmpty: Boolean
        get() = !isLoading &&
                !isSignedOut &&
                errorMessage == null &&
                conversations.isEmpty()
}