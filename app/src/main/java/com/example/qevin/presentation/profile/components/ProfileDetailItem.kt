package com.example.qevin.presentation.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.qevin.core.designsystem.color.QevinColors

@Composable
fun ProfileDetailItem(
    symbol: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = symbol,
            contentDescription = title,
            tint = QevinColors.Lavender,
            modifier = Modifier.width(22.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = QevinColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value.ifBlank { "Not provided" },
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}