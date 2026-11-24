package com.example.qevin.presentation.conversation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.model.ConversationIds
import com.example.qevin.domain.repository.AuthRepository
import com.example.qevin.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.get<String>("conversationId").orEmpty()

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val uid = authRepository.currentUser.first()?.id

            if (uid == null) {
                _errors.tryEmit("You are signed out.")
                return@launch
            }

            // Sample IDs such as "rahul" are not real direct conversations.
            // They stay local-only until Phase 5 replaces them.
            if (ConversationIds.otherParticipant(conversationId, uid) == null) {
                Log.d(TAG, "Local-only conversation '$conversationId'. Not persisted.")
                return@launch
            }

            messageRepository
                .sendTextMessage(conversationId, uid, text)
                .onSuccess { Log.d(TAG, "Message sent to $conversationId") }
                .onFailure { error ->
                    Log.e(TAG, "Send failed", error)
                    _errors.tryEmit(error.message ?: "Message could not be sent.")
                }
        }
    }

    private companion object {
        const val TAG = "QEVIN_SEND"
    }
}