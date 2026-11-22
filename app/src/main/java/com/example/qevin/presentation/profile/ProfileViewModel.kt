package com.example.qevin.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qevin.domain.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.net.Uri
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var observeProfileJob: Job? = null

    init {
        loadProfile()
    }

    // --------------------------------------------------
    // PROFILE LOADING
    // --------------------------------------------------

    fun loadProfile() {
        observeProfileJob?.cancel()

        val uid = firebaseAuth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = null,
                    isProfileNotFound = false,
                    errorMessage = "You are not currently signed in."
                )
            }
            return
        }

        observeProfileJob = viewModelScope.launch {
            profileRepository.observeProfile(uid)
                .onStart {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                }
                .catch { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.localizedMessage
                                ?: "Unable to load your profile."
                        )
                    }
                }
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            isProfileNotFound = profile == null,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    // --------------------------------------------------
    // EDIT PROFILE
    // --------------------------------------------------

    fun startEditing() {
        val profile = _uiState.value.profile ?: return

        _uiState.update {
            it.copy(
                isEditing = true,
                editedDisplayName = profile.displayName,
                editedUsername = profile.username.orEmpty(),
                editedAbout = profile.about,
                displayNameError = null,
                usernameError = null,
                aboutError = null,
                errorMessage = null,
                saveSuccess = false,
                hasUnsavedChanges = false,
                showDiscardChangesDialog = false
            )
        }
    }

    // --------------------------------------------------
    // FIELD UPDATES
    // --------------------------------------------------

    fun onDisplayNameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                editedDisplayName = value,
                displayNameError = null,
                hasUnsavedChanges = hasChanges(
                    state = state,
                    displayName = value
                )
            )
        }
    }

    fun onUsernameChange(value: String) {
        _uiState.update { state ->
            state.copy(
                editedUsername = value,
                usernameError = null,
                hasUnsavedChanges = hasChanges(
                    state = state,
                    username = value
                )
            )
        }
    }

    fun onAboutChange(value: String) {
        _uiState.update { state ->
            state.copy(
                editedAbout = value,
                aboutError = null,
                hasUnsavedChanges = hasChanges(
                    state = state,
                    about = value
                )
            )
        }
    }

    // --------------------------------------------------
    // UNSAVED CHANGE DETECTION
    // --------------------------------------------------

    private fun hasChanges(
        state: ProfileUiState,
        displayName: String = state.editedDisplayName,
        username: String = state.editedUsername,
        about: String = state.editedAbout
    ): Boolean {

        val profile = state.profile ?: return false

        return displayName.trim() != profile.displayName.trim() ||
                username.trim() != profile.username.orEmpty().trim() ||
                about.trim() != profile.about.trim()
    }

    // --------------------------------------------------
    // VALIDATION
    // --------------------------------------------------

    private fun validateProfileInput(): Boolean {

        val state = _uiState.value

        val displayName = state.editedDisplayName.trim()
        val username = state.editedUsername.trim()

        val displayNameError = when {
            displayName.isBlank() ->
                "Display name cannot be empty."

            else -> null
        }

        // Username rule: 3–20 characters,
        // letters, numbers and underscores only.
        val usernameRegex = Regex("^[a-zA-Z0-9_]{3,20}$")

        val usernameError = when {
            username.isBlank() ->
                "Username cannot be empty."

            !usernameRegex.matches(username) ->
                "Username must contain 3–20 letters, numbers or underscores."

            else -> null
        }

        _uiState.update {
            it.copy(
                displayNameError = displayNameError,
                usernameError = usernameError
            )
        }

        return displayNameError == null &&
                usernameError == null
    }

    // --------------------------------------------------
    // SAVE PROFILE
    // --------------------------------------------------

    fun saveProfile() {

        if (_uiState.value.isSaving) return

        if (!validateProfileInput()) return

        val state = _uiState.value
        val existingProfile = state.profile

        if (existingProfile == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Profile information is unavailable."
                )
            }
            return
        }

        val uid = firebaseAuth.currentUser?.uid

        if (uid.isNullOrBlank() || uid != existingProfile.uid) {
            _uiState.update {
                it.copy(
                    errorMessage = "Your session is invalid. Please sign in again."
                )
            }
            return
        }

        val displayName = state.editedDisplayName.trim()
        val username = state.editedUsername.trim()
        val about = state.editedAbout.trim()

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    saveSuccess = false
                )
            }

            // Check username availability only
            // when the username has actually changed.
            val usernameChanged = !username.equals(
                existingProfile.username.orEmpty().trim(),
                ignoreCase = true
            )

            if (usernameChanged) {

                val availabilityResult =
                    profileRepository.isUsernameAvailable(
                        username = username,
                        currentUid = uid
                    )

                if (availabilityResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = availabilityResult.exceptionOrNull()
                                ?.localizedMessage
                                ?: "Unable to verify username availability."
                        )
                    }
                    return@launch
                }

                if (availabilityResult.getOrNull() != true) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            usernameError = "This username is already taken."
                        )
                    }
                    return@launch
                }
            }

            val updatedProfile = existingProfile.copy(
                displayName = displayName,
                username = username,
                about = about,
                updatedAt = System.currentTimeMillis()
            )

            val updateResult =
                profileRepository.updateProfile(updatedProfile)

            updateResult.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            profile = updatedProfile,
                            isEditing = false,
                            isSaving = false,
                            saveSuccess = true,
                            hasUnsavedChanges = false,
                            showDiscardChangesDialog = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = exception.localizedMessage
                                ?: "Unable to update your profile."
                        )
                    }
                }
            )
        }
    }

    // --------------------------------------------------
    // CANCEL EDITING
    // --------------------------------------------------

    fun cancelEditing() {

        val state = _uiState.value

        if (state.hasUnsavedChanges) {
            _uiState.update {
                it.copy(showDiscardChangesDialog = true)
            }
        } else {
            discardChanges()
        }
    }

    // --------------------------------------------------
    // DISCARD CONFIRMATION
    // --------------------------------------------------

    fun confirmDiscardChanges() {
        discardChanges()
    }

    fun dismissDiscardChangesDialog() {
        _uiState.update {
            it.copy(showDiscardChangesDialog = false)
        }
    }

    private fun discardChanges() {

        val profile = _uiState.value.profile

        _uiState.update {
            it.copy(
                isEditing = false,
                editedDisplayName = profile?.displayName.orEmpty(),
                editedUsername = profile?.username.orEmpty(),
                editedAbout = profile?.about.orEmpty(),
                displayNameError = null,
                usernameError = null,
                aboutError = null,
                hasUnsavedChanges = false,
                showDiscardChangesDialog = false,
                errorMessage = null,
                isSaving = false
            )
        }
    }

    // --------------------------------------------------
    // CLEAR SUCCESS MESSAGE
    // --------------------------------------------------

    fun clearSaveSuccess() {
        _uiState.update {
            it.copy(saveSuccess = false)
        }
    }

    // --------------------------------------------------
// PROFILE PHOTO - LOCAL IMAGE SELECTION
// --------------------------------------------------

    fun onProfileImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedProfileImageUri = uri
            )
        }
    }
}
