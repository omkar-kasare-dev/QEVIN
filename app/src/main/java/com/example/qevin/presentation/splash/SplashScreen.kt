package com.example.qevin.presentation.splash



import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.R

private val QevinBackground = Color(0xFF05031B)
private val QevinPurple = Color(0xFF8B35FF)
private val QevinLavender = Color(0xFFD9B9FF)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(QevinBackground)
    ) {
        // Decorative background curves
        SplashBackground(
            modifier = Modifier.fillMaxSize()
        )

        // Main logo and branding
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .offset(y = (-20).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.qevin_logo
                ),
                contentDescription = "QEVIN logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QEVIN",
                color = Color.White,
                fontSize = 46.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 10.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Quiet. Every Voice. In. Network.",
                color = QevinLavender.copy(alpha = 0.95f),
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Loading indicator
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * 0.13f)
                .size(34.dp),
            color = QevinLavender,
            trackColor = QevinPurple.copy(alpha = 0.25f),
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun SplashBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        val width = size.width
        val height = size.height

        // Top-left purple curve
        val topCenter = Offset(
            x = -width * 0.12f,
            y = -height * 0.10f
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF211064),
                    Color(0xFF10083C),
                    QevinBackground
                ),
                start = Offset(0f, 0f),
                end = Offset(width * 0.55f, height * 0.35f)
            ),
            radius = width * 0.62f,
            center = topCenter
        )

        // Cut out the center to create the curved ring
        drawCircle(
            color = QevinBackground,
            radius = width * 0.51f,
            center = topCenter
        )

        // Bottom-right purple curve
        val bottomCenter = Offset(
            x = width * 1.12f,
            y = height * 1.08f
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF16094C),
                    Color(0xFF0B0630),
                    QevinBackground
                ),
                start = Offset(width, height),
                end = Offset(width * 0.55f, height * 0.55f)
            ),
            radius = width * 0.66f,
            center = bottomCenter
        )

        // Inner cut-out
        drawCircle(
            color = QevinBackground,
            radius = width * 0.52f,
            center = bottomCenter
        )
    }
}