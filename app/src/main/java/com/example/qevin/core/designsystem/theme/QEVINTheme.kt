package com.example.qevin.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.core.designsystem.shape.QevinShapes
import com.example.qevin.core.designsystem.typography.QevinTypography

private val QevinDarkColorScheme = darkColorScheme(

    primary = QevinColors.Primary,
    onPrimary = QevinColors.TextPrimary,

    primaryContainer = QevinColors.PrimaryDark,
    onPrimaryContainer = QevinColors.TextPrimary,

    secondary = QevinColors.Lavender,
    onSecondary = QevinColors.Background,

    background = QevinColors.Background,
    onBackground = QevinColors.TextPrimary,

    surface = QevinColors.Surface,
    onSurface = QevinColors.TextPrimary,

    surfaceVariant = QevinColors.SurfaceVariant,
    onSurfaceVariant = QevinColors.TextSecondary,

    outline = QevinColors.Outline,
    outlineVariant = QevinColors.Divider,

    error = QevinColors.Error,
    onError = QevinColors.TextPrimary
)

@Composable
fun QEVINTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = QevinDarkColorScheme,
        typography = QevinTypography,
        shapes = QevinShapes,
        content = content
    )
}