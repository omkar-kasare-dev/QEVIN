package com.example.qevin.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallMissed
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.presentation.home.avatarColorFor


@Composable
internal fun CallListItem(
    call: CallUiModel,
    onClick: () -> Unit
) {
    val subtitleColor = if (call.isMissed) QevinColors.Error else QevinColors.TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CallAvatar(call = call)

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.title,
                color = if (call.isMissed) QevinColors.Error else QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        call.isMissed -> Icons.AutoMirrored.Rounded.CallMissed
                        call.isIncoming -> Icons.AutoMirrored.Rounded.CallReceived
                        else -> Icons.AutoMirrored.Rounded.CallMade
                    },
                    contentDescription = null,
                    tint = subtitleColor,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = call.subtitle,
                    color = subtitleColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = call.timeLabel,
                color = QevinColors.TextSecondary,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Indicator only. Starting calls arrives in Phase 7.
            Icon(
                imageVector = if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.Call,
                contentDescription = if (call.isVideo) "Video call" else "Audio call",
                tint = QevinColors.Lavender,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CallAvatar(call: CallUiModel) {
    val baseColor = avatarColorFor(call.colorSeed)

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (call.isGroupCall) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = "Group call",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        } else {
            // Always drawn. A loaded photo covers it; a missing or failed
            // photo leaves the initials showing.
            Text(
                text = call.initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (!call.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = call.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}