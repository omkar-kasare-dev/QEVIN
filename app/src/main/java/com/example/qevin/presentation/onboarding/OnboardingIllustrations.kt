package com.example.qevin.presentation.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

private val Lavender = Color(0xFFD9B9FF)
private val Purple = Color(0xFF8B35FF)
private val DeepPurple = Color(0xFF4514B5)
private val DarkNavy = Color(0xFF090522)

@Composable
fun OnboardingIllustration(
    type: OnboardingIllustrationType,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        when (type) {
            OnboardingIllustrationType.CHAT -> drawChatIllustration()
            OnboardingIllustrationType.PRIVACY -> drawPrivacyIllustration()
            OnboardingIllustrationType.MEDIA -> drawMediaIllustration()
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawChatIllustration() {

    val w = size.width
    val h = size.height

    // Orbit around the chat bubbles
    drawOval(
        color = Purple.copy(alpha = 0.8f),
        topLeft = Offset(w * 0.08f, h * 0.28f),
        size = Size(w * 0.84f, h * 0.45f),
        style = Stroke(width = 2f)
    )

    // Orbit highlight
    drawCircle(
        color = Lavender,
        radius = 2.5f,
        center = Offset(w * 0.16f, h * 0.27f)
    )

    // Main chat bubble
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFAC86FF),
                Color(0xFF5921CE)
            )
        ),
        topLeft = Offset(w * 0.27f, h * 0.22f),
        size = Size(w * 0.34f, h * 0.25f),
        cornerRadius = CornerRadius(22f)
    )

    val firstTail = Path().apply {
        moveTo(w * 0.33f, h * 0.45f)
        lineTo(w * 0.31f, h * 0.53f)
        lineTo(w * 0.43f, h * 0.46f)
        close()
    }

    drawPath(firstTail, color = Color(0xFF5921CE))

    // Text lines inside first bubble
    repeat(2) { index ->
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(w * 0.34f, h * (0.31f + index * 0.055f)),
            end = Offset(w * 0.54f, h * (0.31f + index * 0.055f)),
            strokeWidth = 6f
        )
    }

    // Second chat bubble
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Lavender,
                Color(0xFF8D5BFF)
            )
        ),
        topLeft = Offset(w * 0.56f, h * 0.42f),
        size = Size(w * 0.27f, h * 0.21f),
        cornerRadius = CornerRadius(18f)
    )

    val secondTail = Path().apply {
        moveTo(w * 0.72f, h * 0.61f)
        lineTo(w * 0.75f, h * 0.69f)
        lineTo(w * 0.78f, h * 0.61f)
        close()
    }

    drawPath(secondTail, color = Color(0xFF8D5BFF))

    repeat(2) { index ->
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(w * 0.61f, h * (0.49f + index * 0.05f)),
            end = Offset(w * 0.77f, h * (0.49f + index * 0.05f)),
            strokeWidth = 5f
        )
    }

    // Small lock at the bottom-right
    drawRoundRect(
        color = Lavender,
        topLeft = Offset(w * 0.77f, h * 0.65f),
        size = Size(w * 0.10f, h * 0.12f),
        cornerRadius = CornerRadius(8f)
    )

    drawArc(
        color = Lavender,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.79f, h * 0.60f),
        size = Size(w * 0.06f, h * 0.10f),
        style = Stroke(width = 4f)
    )

    drawCircle(
        color = DeepPurple,
        radius = 3f,
        center = Offset(w * 0.82f, h * 0.70f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawPrivacyIllustration() {

    val w = size.width
    val h = size.height

    // Privacy orbit
    drawOval(
        color = Purple.copy(alpha = 0.9f),
        topLeft = Offset(w * 0.08f, h * 0.31f),
        size = Size(w * 0.84f, h * 0.38f),
        style = Stroke(width = 2f)
    )

    // Shield shape
    val shield = Path().apply {
        moveTo(w * 0.50f, h * 0.20f)
        cubicTo(
            w * 0.62f, h * 0.29f,
            w * 0.70f, h * 0.29f,
            w * 0.70f, h * 0.29f
        )
        lineTo(w * 0.68f, h * 0.49f)
        cubicTo(
            w * 0.66f, h * 0.61f,
            w * 0.57f, h * 0.70f,
            w * 0.50f, h * 0.75f
        )
        cubicTo(
            w * 0.43f, h * 0.70f,
            w * 0.34f, h * 0.61f,
            w * 0.32f, h * 0.49f
        )
        lineTo(w * 0.30f, h * 0.29f)
        close()
    }

    drawPath(
        path = shield,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF9E6AFF),
                Color(0xFF37108F)
            )
        )
    )

    drawPath(
        path = shield,
        color = Lavender.copy(alpha = 0.85f),
        style = Stroke(width = 3f)
    )

    // Lock shackle
    drawArc(
        color = Color.White,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(w * 0.43f, h * 0.40f),
        size = Size(w * 0.14f, h * 0.17f),
        style = Stroke(width = 6f)
    )

    // Lock body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White,
                Lavender
            )
        ),
        topLeft = Offset(w * 0.41f, h * 0.49f),
        size = Size(w * 0.18f, h * 0.14f),
        cornerRadius = CornerRadius(10f)
    )

    // Keyhole
    drawCircle(
        color = DeepPurple,
        radius = w * 0.015f,
        center = Offset(w * 0.50f, h * 0.54f)
    )

    drawLine(
        color = DeepPurple,
        start = Offset(w * 0.50f, h * 0.55f),
        end = Offset(w * 0.50f, h * 0.59f),
        strokeWidth = 4f
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawMediaIllustration() {

    val w = size.width
    val h = size.height

    // Orbit
    drawOval(
        color = Purple.copy(alpha = 0.9f),
        topLeft = Offset(w * 0.08f, h * 0.31f),
        size = Size(w * 0.85f, h * 0.38f),
        style = Stroke(width = 2f)
    )

    // Main image card
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFA98AFF),
                Color(0xFF6036DB)
            )
        ),
        topLeft = Offset(w * 0.25f, h * 0.25f),
        size = Size(w * 0.43f, h * 0.42f),
        cornerRadius = CornerRadius(22f)
    )

    // Image card border
    drawRoundRect(
        color = Lavender,
        topLeft = Offset(w * 0.25f, h * 0.25f),
        size = Size(w * 0.43f, h * 0.42f),
        cornerRadius = CornerRadius(22f),
        style = Stroke(width = 3f)
    )

    // Sun
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = w * 0.035f,
        center = Offset(w * 0.37f, h * 0.38f)
    )

    // Mountains inside image
    val mountains = Path().apply {
        moveTo(w * 0.29f, h * 0.61f)
        lineTo(w * 0.42f, h * 0.47f)
        lineTo(w * 0.49f, h * 0.55f)
        lineTo(w * 0.55f, h * 0.49f)
        lineTo(w * 0.65f, h * 0.62f)
        close()
    }

    drawPath(
        path = mountains,
        color = Color(0xFFD5C4FF)
    )

    // Video card
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Lavender,
                Color(0xFF7B4AFF)
            )
        ),
        topLeft = Offset(w * 0.60f, h * 0.46f),
        size = Size(w * 0.27f, h * 0.27f),
        cornerRadius = CornerRadius(18f)
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.7f),
        topLeft = Offset(w * 0.60f, h * 0.46f),
        size = Size(w * 0.27f, h * 0.27f),
        cornerRadius = CornerRadius(18f),
        style = Stroke(width = 2f)
    )

    // Play symbol
    val play = Path().apply {
        moveTo(w * 0.70f, h * 0.53f)
        lineTo(w * 0.70f, h * 0.66f)
        lineTo(w * 0.79f, h * 0.595f)
        close()
    }

    drawPath(
        path = play,
        color = Color.White
    )

    // Small decorative stars
    drawCircle(
        color = Lavender,
        radius = 2f,
        center = Offset(w * 0.78f, h * 0.17f)
    )

    drawCircle(
        color = Purple,
        radius = 2.5f,
        center = Offset(w * 0.20f, h * 0.23f)
    )
}
