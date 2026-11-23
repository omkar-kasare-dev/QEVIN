package com.example.qevin.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.presentation.profile.components.EditProfileContent
import com.example.qevin.presentation.profile.components.ProfileContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

@Suppress("UNUSED_PARAMETER")
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        uri?.let { selectedUri ->
            viewModel.onProfileImageSelected(selectedUri)
        }
    }



    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Profile updated successfully.")
            viewModel.clearSaveSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        when {
            uiState.isLoading -> {
                ProfileLoadingContent()
            }

            uiState.errorMessage != null && !uiState.isEditing -> {
                ProfileErrorContent(
                    message = uiState.errorMessage.orEmpty(),
                    onRetryClick = viewModel::loadProfile
                )
            }

            uiState.isProfileNotFound -> {
                ProfileEmptyContent(
                    onRetryClick = viewModel::loadProfile
                )
            }

            uiState.isEditing -> {
                EditProfileContent(
                    uiState = uiState,
                    onBackClick = viewModel::cancelEditing,
                    onDisplayNameChange = viewModel::onDisplayNameChange,
                    onUsernameChange = viewModel::onUsernameChange,
                    onAboutChange = viewModel::onAboutChange,
                    onSaveClick = viewModel::saveProfile,
                    onCancelClick = viewModel::cancelEditing,
                    onConfirmDiscard = viewModel::confirmDiscardChanges,
                    onDismissDiscard = viewModel::dismissDiscardChangesDialog
                )
            }

            else -> {
                val profile = uiState.profile

                if (profile != null) {
                    ProfileContent(
                        displayName = profile.displayName,
                        username = profile.username.orEmpty(),
                        about = profile.about,
                        phoneNumber = profile.phoneNumber.orEmpty(),
                        email = profile.email.orEmpty(),
                        onBackClick = onBackClick,
                        onEditProfileClick = viewModel::startEditing,
                        profileImageUri = uiState.selectedProfileImageUri,
                        onChangePhotoClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        onRemovePhotoClick = viewModel::onRemoveProfileImage
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Composable
private fun ProfileLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = QevinColors.Lavender
        )
    }
}

@Composable
private fun ProfileErrorContent(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = QevinColors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QevinColors.Primary)
        ) {
            Text("Try Again", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileEmptyContent(
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your profile hasn't been created yet.",
            color = QevinColors.TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRetryClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = QevinColors.Primary)
        ) {
            Text("Check Again", fontWeight = FontWeight.Bold)
        }
    }
}