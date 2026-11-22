package com.example.qevin.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.presentation.profile.ProfileUiState

@Composable
fun EditProfileContent(
    uiState: ProfileUiState,
    onBackClick: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onAboutChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmDiscard: () -> Unit,
    onDismissDiscard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = QevinColors.TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Edit Profile",
                        color = QevinColors.TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // PROFILE AVATAR PREVIEW
                ProfileAvatar(
                    displayName = uiState.editedDisplayName
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Update your personal information",
                    color = QevinColors.TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // DISPLAY NAME
                EditProfileField(
                    label = "Display Name",
                    value = uiState.editedDisplayName,
                    placeholder = "Enter your name",
                    error = uiState.displayNameError,
                    enabled = !uiState.isSaving,
                    onValueChange = onDisplayNameChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // USERNAME
                EditProfileField(
                    label = "Username",
                    value = uiState.editedUsername,
                    placeholder = "Enter username",
                    error = uiState.usernameError,
                    enabled = !uiState.isSaving,
                    onValueChange = onUsernameChange
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "3–20 characters. Letters, numbers and underscores.",
                    color = QevinColors.TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ABOUT
                EditProfileField(
                    label = "About",
                    value = uiState.editedAbout,
                    placeholder = "Tell something about yourself",
                    error = uiState.aboutError,
                    enabled = !uiState.isSaving,
                    singleLine = false,
                    onValueChange = onAboutChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SAVE ERROR
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // SAVE BUTTON
                Button(
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving && uiState.hasUnsavedChanges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QevinColors.Primary,
                        disabledContainerColor = QevinColors.SurfaceVariant
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text(
                            text = "Save Changes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CANCEL BUTTON
                OutlinedButton(
                    onClick = onCancelClick,
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = QevinColors.Lavender,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // DISCARD CHANGES DIALOG
    if (uiState.showDiscardChangesDialog) {
        AlertDialog(
            onDismissRequest = onDismissDiscard,
            containerColor = QevinColors.ElevatedSurface,
            title = {
                Text(
                    text = "Discard Changes?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Are you sure you want to discard them?",
                    color = QevinColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDiscard) {
                    Text(
                        text = "Discard",
                        color = Color(0xFFFF6B6B)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDiscard) {
                    Text(
                        text = "Keep Editing",
                        color = QevinColors.Lavender
                    )
                }
            }
        )
    }
}

@Composable
private fun EditProfileField(
    label: String,
    value: String,
    placeholder: String,
    error: String?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    color = QevinColors.TextSecondary,
                    fontSize = 13.sp
                )
            },
            isError = error != null,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = QevinColors.SurfaceVariant,
                unfocusedContainerColor = QevinColors.SurfaceVariant,
                disabledContainerColor = QevinColors.SurfaceVariant,
                focusedBorderColor = QevinColors.Lavender,
                unfocusedBorderColor = QevinColors.TextSecondary.copy(alpha = 0.3f),
                errorBorderColor = Color(0xFFFF6B6B),
                cursorColor = QevinColors.Lavender
            )
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                color = Color(0xFFFF6B6B),
                fontSize = 11.sp
            )
        }
    }
}