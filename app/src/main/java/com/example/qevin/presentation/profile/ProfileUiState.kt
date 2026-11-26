package com.example.qevin.presentation.profile

import com.example.qevin.domain.model.UserProfile
import android.net.Uri

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val isProfileNotFound: Boolean = false,
    val errorMessage: String? = null,

    // Profile Photo - Local Preview
    val selectedProfileImageUri: Uri? = null,
    val profileImageErrorMessage: String? = null,

    // Edit Profile
    val isEditing: Boolean = false,
    val editedDisplayName: String = "",
    val editedUsername: String = "",
    val editedAbout: String = "",

    // Validation
    val displayNameError: String? = null,
    val usernameError: String? = null,
    val aboutError: String? = null,

    // Save state
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,

    // Unsaved changes
    val hasUnsavedChanges: Boolean = false,
    val showDiscardChangesDialog: Boolean = false,

    // Logout
    val showLogoutDialog: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val logoutErrorMessage: String? = null
)

