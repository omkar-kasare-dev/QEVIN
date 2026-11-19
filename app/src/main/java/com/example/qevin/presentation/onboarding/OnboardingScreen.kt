package com.example.qevin.presentation.onboarding


import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val headingStart: String,
    val headingHighlight: String,
    val headingEnd: String,
    val description: String,
    val illustration: OnboardingIllustrationType
)

enum class OnboardingIllustrationType {
    CHAT,
    PRIVACY,
    MEDIA
}

private val onboardingPages = listOf(
    OnboardingPageData(
        headingStart = "Connect\nin ",
        headingHighlight = "your",
        headingEnd = " own way",
        description = "Chat, call, and share —\nwith complete privacy.",
        illustration = OnboardingIllustrationType.CHAT
    ),
    OnboardingPageData(
        headingStart = "Your ",
        headingHighlight = "privacy",
        headingEnd = ",\nour priority",
        description = "Your chats stay yours,\nalways.",
        illustration = OnboardingIllustrationType.PRIVACY
    ),
    OnboardingPageData(
        headingStart = "More than ",
        headingHighlight = "chat",
        headingEnd = "",
        description = "Share moments,\nexpress more.",
        illustration = OnboardingIllustrationType.MEDIA
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { onboardingPages.size }
    )

    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val isLastPage = currentPage == onboardingPages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        OnboardingBackground(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = true,
                verticalAlignment = Alignment.Top
            ) { pageIndex ->

                val page = onboardingPages[pageIndex]

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = buildAnnotatedString {
                            append(page.headingStart)

                            withStyle(
                                SpanStyle(
                                    color = QevinColors.PurpleGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(page.headingHighlight)
                            }

                            append(page.headingEnd)
                        },
                        color = QevinColors.TextPrimary,
                        fontSize = 27.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.description,
                        color = QevinColors.TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 21.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        OnboardingIllustration(
                            type = page.illustration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 290.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Page indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                onboardingPages.indices.forEach { index ->
                    val selected = currentPage == index

                    val width by animateDpAsState(
                        targetValue = if (selected) 10.dp else 7.dp,
                        label = "indicatorWidth"
                    )

                    val color by animateColorAsState(
                        targetValue = if (selected) {
                            QevinColors.Lavender
                        } else {
                            QevinColors.Outline
                        },
                        label = "indicatorColor"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 5.dp)
                            .size(width = width, height = 7.dp)
                            .background(
                                color = color,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Primary action
            OnboardingGradientButton(
                text = if (isLastPage) "Get Started" else "Next",
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPage + 1)
                        }
                    }
                }
            )

            // Skip action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable {
                        onFinish()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isLastPage) "" else "Skip",
                    color = QevinColors.TextDisabled,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun OnboardingGradientButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFC29AFF),
                        Color(0xFF9360FF),
                        Color(0xFFA477FF)
                    )
                ),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (text == "Next") "$text   →" else text,
            color = QevinColors.Background,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OnboardingBackground(
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF211064).copy(alpha = 0.7f),
                    Color.Transparent
                )
            ),
            radius = w * 0.65f,
            center = androidx.compose.ui.geometry.Offset(
                -w * 0.15f,
                -h * 0.08f
            )
        )

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF3510A0).copy(alpha = 0.35f),
                    Color.Transparent
                )
            ),
            radius = w * 0.55f,
            center = androidx.compose.ui.geometry.Offset(
                w * 1.08f,
                h * 1.05f
            )
        )
    }
}
