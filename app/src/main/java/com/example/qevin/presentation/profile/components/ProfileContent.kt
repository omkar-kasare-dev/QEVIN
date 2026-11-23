package com.example.qevin.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Phone
import android.net.Uri
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.TextButton

@Composable
fun ProfileContent(
    displayName: String,
    username: String,
    about: String,
    phoneNumber: String,
    email: String,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    profileImageUri: Uri? = null,
    onChangePhotoClick: () -> Unit = {},
    onRemovePhotoClick: () -> Unit = {}
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
                        text = "My Profile",
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

                // PROFILE AVATAR
                ProfileAvatar(
                    displayName = displayName,
                    imageUri = profileImageUri,
                    onClick = onChangePhotoClick
                )

                if (profileImageUri != null) {
                    TextButton(
                        onClick = onRemovePhotoClick
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Remove profile photo",
                            tint = QevinColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "Remove Photo",
                            color = QevinColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))


                Spacer(modifier = Modifier.height(14.dp))

                // DISPLAY NAME
                Text(
                    text = displayName.ifBlank { "Your Name" },
                    color = QevinColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // USERNAME
                Text(
                    text = if (username.isNotBlank()) "@$username" else "@username",
                    color = QevinColors.Lavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // EDIT PROFILE BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    QevinColors.Primary,
                                    QevinColors.Lavender
                                )
                            )
                        )
                        .clickable(onClick = onEditProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ABOUT SECTION
                ProfileSectionTitle(
                    title = "About"
                )

                ProfileInfoCard {
                    Text(
                        text = about.ifBlank { "No information added yet." },
                        color = if (about.isBlank()) QevinColors.TextSecondary else Color.White,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CONTACT INFORMATION
                ProfileSectionTitle(
                    title = "Contact Information"
                )

                ProfileInfoCard {
                    ProfileDetailItem(
                        symbol = Icons.Rounded.Email,
                        title = "Email Address",
                        value = email
                    )

                    HorizontalDivider(
                        color = QevinColors.TextSecondary.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    ProfileDetailItem(
                        symbol = Icons.Rounded.Phone,
                        title = "Phone Number",
                        value = phoneNumber
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Your voice matters. Always.",
                    color = QevinColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}