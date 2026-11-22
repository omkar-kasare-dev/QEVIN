package com.example.qevin.presentation.profile.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import com.example.qevin.core.designsystem.color.QevinColors

@Composable
fun ProfileAvatar(
    displayName: String,
    imageUri: Uri? = null,
    onClick: () -> Unit = {}
) {
    val initials = displayName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") {
            it.first().uppercase()
        }
        .ifBlank { "Q" }

    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        QevinColors.Lavender,
                        QevinColors.Primary
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(3.dp)
            .clip(CircleShape)
            .background(QevinColors.ElevatedSurface),
        contentAlignment = Alignment.Center
    ) {

        // Default initials remain visible as fallback.
        Text(
            text = initials,
            color = QevinColors.Lavender,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        // Selected local image preview.
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile photo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Camera icon overlay.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(2.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(QevinColors.Primary)
                .border(
                    width = 2.dp,
                    color = QevinColors.ElevatedSurface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = "Change profile photo",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}