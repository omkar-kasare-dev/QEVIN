package com.example.qevin.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.qevin.R
import com.example.qevin.core.designsystem.color.QevinColors

private enum class HomeTab(val label: String) {
    CHATS("Chats"),
    GROUPS("Groups"),
    CALLS("Calls")
}

private enum class BottomTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CHATS("Chats", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    CALLS("Calls", Icons.Rounded.Call, Icons.Outlined.Call),
    PEOPLE("People", Icons.Rounded.People, Icons.Outlined.PeopleOutline),
    SETTINGS("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
}

private val AvatarPalette = listOf(
    Color(0xFFB58B64),
    Color(0xFFB56F87),
    Color(0xFF5146BD),
    Color(0xFF9B684A),
    Color(0xFF438A9C)
)

// Stable per person: the same seed always gives the same color.
private fun avatarColorFor(seed: String): Color =
    AvatarPalette[(seed.hashCode() and 0x7FFFFFFF) % AvatarPalette.size]

@Composable
fun QEVINHomeScreen(
    onConversationClick: (String) -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomTabClick: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Saved so returning from a conversation keeps the tab you were on.
    var selectedHomeTab by rememberSaveable { mutableStateOf(HomeTab.CHATS) }
    // Not saved on purpose: Home is always the "Chats" bottom tab when it is shown.
    var selectedBottomTab by remember { mutableStateOf(BottomTab.CHATS) }

    val visibleConversations = remember(selectedHomeTab, uiState.conversations) {
        when (selectedHomeTab) {
            HomeTab.CHATS -> uiState.conversations.filter { !it.isGroup }
            HomeTab.GROUPS -> uiState.conversations.filter { it.isGroup }
            HomeTab.CALLS -> emptyList() // Phase 7
        }
    }

    // The Calls tab has no data source yet, so loading/error/offline
    // states only apply to Chats and Groups.
    val isConversationTab = selectedHomeTab != HomeTab.CALLS

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        // Subtle ambient violet glow
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            QevinColors.PrimaryDark.copy(alpha = 0.16f),
                            Color.Transparent
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                QevinHomeHeader(
                    onSearchClick = onSearchClick,
                    onProfileClick = onProfileClick
                )
            },
            bottomBar = {
                QevinBottomNavigation(
                    selectedTab = selectedBottomTab,
                    onTabSelected = { tab ->
                        selectedBottomTab = tab
                        onBottomTabClick(tab.name)
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNewChatClick,
                    modifier = Modifier.size(48.dp),
                    containerColor = QevinColors.Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New Chat",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                HomeCategoryTabs(
                    selectedTab = selectedHomeTab,
                    onTabSelected = { selectedHomeTab = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isConversationTab && uiState.isOffline && uiState.conversations.isNotEmpty()) {
                    OfflineBanner()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    // Brief moment after logout, before navigation leaves Home.
                    uiState.isSignedOut -> Box(modifier = Modifier.fillMaxSize())

                    isConversationTab && uiState.isLoading -> LoadingState()

                    isConversationTab && uiState.errorMessage != null -> ErrorState(
                        message = uiState.errorMessage.orEmpty(),
                        onRetryClick = viewModel::retry
                    )

                    visibleConversations.isEmpty() -> EmptyConversationState(
                        selectedTab = selectedHomeTab,
                        showOfflineHint = isConversationTab &&
                                uiState.isOffline &&
                                uiState.conversations.isEmpty()
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            end = 14.dp,
                            top = 2.dp,
                            bottom = 76.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = visibleConversations,
                            key = { it.id }
                        ) { conversation ->
                            ConversationListItem(
                                conversation = conversation,
                                onClick = { onConversationClick(conversation.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QevinHomeHeader(
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.qevin_logo),
            contentDescription = "QEVIN Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "QEVIN",
            color = QevinColors.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(onClick = onSearchClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search conversations",
                tint = QevinColors.TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(QevinColors.ElevatedSurface)
                .border(
                    width = 0.75.dp,
                    color = QevinColors.Lavender.copy(alpha = 0.25f),
                    shape = CircleShape
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Profile",
                tint = QevinColors.Lavender,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeCategoryTabs(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        HomeTab.entries.forEach { tab ->
            val selected = selectedTab == tab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) QevinColors.Primary
                        else QevinColors.SurfaceVariant.copy(alpha = 0.55f)
                    )
                    .clickable { onTabSelected(tab) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    color = if (selected) Color.White else QevinColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: ConversationUiModel,
    onClick: () -> Unit
) {
    val hasUnread = conversation.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ConversationAvatar(conversation = conversation)

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.title,
                color = QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = conversation.preview,
                color = if (hasUnread) {
                    QevinColors.TextPrimary.copy(alpha = 0.85f)
                } else {
                    QevinColors.TextSecondary
                },
                fontSize = 12.sp,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = conversation.timeLabel,
                color = if (hasUnread) QevinColors.Lavender else QevinColors.TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (hasUnread) {
                UnreadBadge(label = conversation.unreadLabel)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Circle for one digit, pill for "12" or "99+". A fixed 16dp square would clip
 * the text, so the badge has a minimum size and grows with its content.
 */
@Composable
private fun UnreadBadge(label: String) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            .clip(CircleShape)
            .background(QevinColors.PurpleGlow)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = QevinColors.Background,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun ConversationAvatar(conversation: ConversationUiModel) {
    val baseColor = avatarColorFor(conversation.otherUid ?: conversation.id)

    Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Always drawn. A photo, once loaded, covers it; a missing or
            // failed photo simply leaves the initials showing.
            Text(
                text = conversation.initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (!conversation.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = conversation.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (conversation.isOnline) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(QevinColors.Background)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(QevinColors.Success)
            )
        }
    }
}

@Composable
private fun QevinBottomNavigation(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Surface(
        color = QevinColors.Background.copy(alpha = 0.96f),
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = QevinColors.TextDisabled.copy(alpha = 0.16f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTab.entries.forEach { tab ->
                    val selected = selectedTab == tab

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onTabSelected(tab) }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selected) QevinColors.Primary.copy(alpha = 0.18f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = if (selected) QevinColors.Lavender else QevinColors.TextDisabled,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.label,
                            color = if (selected) QevinColors.Lavender else QevinColors.TextDisabled,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = QevinColors.Lavender)
    }
}

@Composable
private fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(QevinColors.Warning.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = null,
            tint = QevinColors.Warning,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "You're offline. Showing saved conversations.",
            color = QevinColors.Warning,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(QevinColors.ElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = QevinColors.Error,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                color = QevinColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onRetryClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = QevinColors.Primary)
            ) {
                Text(text = "Try Again", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyConversationState(
    selectedTab: HomeTab,
    showOfflineHint: Boolean
) {
    val (icon, title) = when (selectedTab) {
        HomeTab.CHATS -> Icons.Outlined.ChatBubbleOutline to "No conversations yet"
        HomeTab.GROUPS -> Icons.Outlined.Groups to "No groups yet"
        HomeTab.CALLS -> Icons.Outlined.Call to "No calls yet"
    }

    val subtitle = if (showOfflineHint) {
        "Connect to the internet to load your chats."
    } else {
        "Your next connection starts here."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(QevinColors.ElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = QevinColors.Lavender,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                color = QevinColors.TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}