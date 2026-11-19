package com.example.qevin.feature.compnents



import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.core.designsystem.dimension.QevinSpacing

@Composable
fun QevinAuthBackground(
    topRightGlow: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Bottom-left / Bottom-center ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        QevinColors.Primary.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.95f),
                    radius = size.width * 0.85f
                ),
                radius = size.width * 0.85f,
                center = Offset(size.width * 0.35f, size.height * 0.95f)
            )

            // Optional Top-right ambient sweep (shown on Create Account screen)
            if (topRightGlow) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QevinColors.PrimaryDark.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.05f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.85f, size.height * 0.05f)
                )
            }
        }
        content()
    }
}

@Composable
fun QevinAuthPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(QevinSpacing.ButtonHeight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = QevinColors.PurpleGlow,
            contentColor = QevinColors.Background,
            disabledContainerColor = QevinColors.PurpleGlow.copy(alpha = 0.35f),
            disabledContentColor = QevinColors.Background.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun QevinBrandHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Replace R.drawable.ic_qevin_logo with your actual vector asset
        Surface(
            modifier = Modifier.size(84.dp),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            // Placeholder for the QEVIN stylized 'Q' speech-bubble logo
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Q",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 54.sp,
                        color = QevinColors.TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(QevinSpacing.Small))

        Text(
            text = "Q E V I N",
            style = MaterialTheme.typography.headlineSmall.copy(
                letterSpacing = 6.sp,
                fontWeight = FontWeight.Medium,
                color = QevinColors.TextPrimary
            )
        )
    }
}