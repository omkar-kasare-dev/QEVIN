package com.example.qevin.presentation.conversation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors

// Temparory
import androidx.compose.runtime.LaunchedEffect // already imported
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

private data class ChatMessage(
    val id: Int,
    val text: String,
    val time: String,
    val isSent: Boolean,
    val isRead: Boolean = false
)

@Composable
fun ConversationScreen(
    conversationId: String,
    onBackClick: () -> Unit = {},
    onVoiceCallClick: () -> Unit = {},
    onVideoCallClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val contactName = getContactName(conversationId)

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.errors.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val messages = remember(conversationId) {
        mutableStateListOf(
            ChatMessage(
                id = 1,
                text = "Hey! How are you?",
                time = "9:20 PM",
                isSent = false
            ),
            ChatMessage(
                id = 2,
                text = "I'm good 😊",
                time = "9:21 PM",
                isSent = true,
                isRead = true
            ),
            ChatMessage(
                id = 3,
                text = "Let's catch up later?",
                time = "9:22 PM",
                isSent = false
            ),
            ChatMessage(
                id = 4,
                text = "Sure! Sounds good 👍",
                time = "9:23 PM",
                isSent = true,
                isRead = true
            )
        )
    }

    var messageText by remember(conversationId) {
        mutableStateOf("")
    }

    val listState = rememberLazyListState()
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)

    // Scroll to bottom when a new message arrives or when the keyboard opens
    LaunchedEffect(messages.size, imeBottom > 0) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        ChatWallpaper()

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                ChatHeader(
                    contactName = contactName,
                    onBackClick = onBackClick,
                    onVoiceCallClick = onVoiceCallClick,
                    onVideoCallClick = onVideoCallClick,
                    onMoreClick = onMoreClick
                )
            },
            bottomBar = {
                MessageComposer(
                    value = messageText,
                    onValueChange = { messageText = it },
                    onSendClick = {
                        val trimmedMessage = messageText.trim()
                        if (trimmedMessage.isNotEmpty()) {
                            messages.add(
                                ChatMessage(
                                    id = (messages.maxOfOrNull { it.id } ?: 0) + 1,
                                    text = trimmedMessage,
                                    time = "Now",
                                    isSent = true,
                                    isRead = false
                                )
                            )
                            // Temp
                            viewModel.sendMessage(trimmedMessage)
                            messageText = ""
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    ChatMessageBubble(message = message)
                }
            }
        }
    }
}

private fun getContactName(conversationId: String): String {
    return when (conversationId.lowercase()) {
        "rahul" -> "Rahul Sharma"
        "priya" -> "Priya Deshmukh"
        "dev-team" -> "Dev Team"
        "family" -> "Family Group"
        "aman" -> "Aman Patil"
        else -> "QEVIN Contact"
    }
}

@Composable
private fun ChatHeader(
    contactName: String,
    onBackClick: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QevinColors.Background.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp),
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

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                QevinColors.Lavender,
                                QevinColors.PrimaryDark
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contactName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contactName,
                    color = QevinColors.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(QevinColors.Success)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Online",
                        color = QevinColors.TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            IconButton(
                onClick = onVoiceCallClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = "Voice Call",
                    tint = QevinColors.TextPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }

            IconButton(
                onClick = onVideoCallClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Videocam,
                    contentDescription = "Video Call",
                    tint = QevinColors.TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More Options",
                    tint = QevinColors.TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(QevinColors.Divider.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isSent) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (message.isSent) 14.dp else 4.dp,
                        bottomEnd = if (message.isSent) 4.dp else 14.dp
                    )
                )
                .background(
                    if (message.isSent) {
                        Brush.linearGradient(
                            colors = listOf(
                                QevinColors.Primary,
                                QevinColors.PrimaryDark
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                QevinColors.ReceivedMessage,
                                QevinColors.SurfaceVariant
                            )
                        )
                    }
                )
                .padding(horizontal = 11.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.time,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )

                if (message.isSent) {
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (message.isRead) "✓✓" else "✓",
                        color = if (message.isRead) QevinColors.Lavender else Color.White.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        QevinColors.Background.copy(alpha = 0.98f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(QevinColors.SurfaceVariant)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                contentDescription = "Emoji",
                tint = QevinColors.Lavender,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { }
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                maxLines = 4,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                cursorBrush = Brush.verticalGradient(
                    colors = listOf(
                        QevinColors.Lavender,
                        QevinColors.Primary
                    )
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = "Type a message...",
                                color = QevinColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Outlined.AttachFile,
                contentDescription = "Attach file",
                tint = QevinColors.Lavender,
                modifier = Modifier
                    .size(22.dp)
                    .clickable { }
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            QevinColors.PurpleGlow,
                            QevinColors.PrimaryDark
                        )
                    )
                )
                .clickable(onClick = onSendClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChatWallpaper() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    QevinColors.Background,
                    Color(0xFF10083C),
                    Color(0xFF24105D)
                )
            )
        )

        val stars = listOf(
            Offset(size.width * 0.12f, size.height * 0.20f),
            Offset(size.width * 0.32f, size.height * 0.12f),
            Offset(size.width * 0.75f, size.height * 0.18f),
            Offset(size.width * 0.88f, size.height * 0.31f),
            Offset(size.width * 0.22f, size.height * 0.48f),
            Offset(size.width * 0.65f, size.height * 0.40f),
            Offset(size.width * 0.48f, size.height * 0.56f)
        )

        stars.forEach { star ->
            drawCircle(
                color = QevinColors.Lavender.copy(alpha = 0.55f),
                radius = 1.2.dp.toPx(),
                center = star
            )
        }

        val distantMountain = Path().apply {
            moveTo(0f, size.height * 0.79f)
            lineTo(size.width * 0.22f, size.height * 0.65f)
            lineTo(size.width * 0.42f, size.height * 0.78f)
            lineTo(size.width * 0.68f, size.height * 0.62f)
            lineTo(size.width, size.height * 0.77f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = distantMountain,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF302078),
                    Color(0xFF130A3D)
                )
            )
        )

        val foregroundMountain = Path().apply {
            moveTo(0f, size.height * 0.88f)
            lineTo(size.width * 0.35f, size.height * 0.75f)
            lineTo(size.width * 0.58f, size.height * 0.86f)
            lineTo(size.width * 0.82f, size.height * 0.73f)
            lineTo(size.width, size.height * 0.84f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = foregroundMountain,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF21105D),
                    QevinColors.Background
                )
            )
        )
    }
}