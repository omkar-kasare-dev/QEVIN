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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private data class ConversationPreview(
    val id: String,
    val name: String,
    val message: String,
    val time: String,
    val initials: String,
    val avatarColor: Color,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false,
    val isCall: Boolean = false
)

private val sampleConversations = listOf(
    ConversationPreview(
        id = "rahul",
        name = "Rahul Sharma",
        message = "Hey! How are you?",
        time = "9:32 PM",
        initials = "RS",
        avatarColor = Color(0xFFB58B64),
        unreadCount = 2,
        isOnline = true
    ),
    ConversationPreview(
        id = "priya",
        name = "Priya Deshmukh",
        message = "See you tomorrow!",
        time = "8:15 PM",
        initials = "PD",
        avatarColor = Color(0xFFB56F87),
        unreadCount = 1
    ),
    ConversationPreview(
        id = "dev-team",
        name = "Dev Team",
        message = "Project updated",
        time = "7:40 PM",
        initials = "DT",
        avatarColor = Color(0xFF5146BD),
        isGroup = true
    ),
    ConversationPreview(
        id = "family",
        name = "Family Group",
        message = "Good night ❤️",
        time = "Yesterday",
        initials = "FG",
        avatarColor = Color(0xFF9B684A),
        isGroup = true
    ),
    ConversationPreview(
        id = "aman",
        name = "Aman Patil",
        message = "Thanks!",
        time = "Mon",
        initials = "AP",
        avatarColor = Color(0xFF438A9C)
    )
)

@Composable
fun QEVINHomeScreen(
    onConversationClick: (String) -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBottomTabClick: (String) -> Unit = {}
) {
    var selectedHomeTab by remember { mutableStateOf(HomeTab.CHATS) }
    var selectedBottomTab by remember { mutableStateOf(BottomTab.CHATS) }

    val visibleConversations = remember(selectedHomeTab) {
        when (selectedHomeTab) {
            HomeTab.CHATS -> sampleConversations.filter { !it.isGroup }
            HomeTab.GROUPS -> sampleConversations.filter { it.isGroup }
            HomeTab.CALLS -> sampleConversations.filter { it.isCall }
        }
    }

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

                if (visibleConversations.isEmpty()) {
                    EmptyConversationState(selectedTab = selectedHomeTab)
                } else {
                    LazyColumn(
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
        // Replace R.drawable.ic_qevin_logo with your actual drawable resource name
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
    conversation: ConversationPreview,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Compact Avatar
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
                            colors = listOf(
                                conversation.avatarColor,
                                conversation.avatarColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.initials,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
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

        Spacer(modifier = Modifier.width(11.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.name,
                color = QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = conversation.message,
                color = if (conversation.unreadCount > 0) {
                    QevinColors.TextPrimary.copy(alpha = 0.85f)
                } else {
                    QevinColors.TextSecondary
                },
                fontSize = 12.sp,
                fontWeight = if (conversation.unreadCount > 0) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                },
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
                text = conversation.time,
                color = if (conversation.unreadCount > 0) {
                    QevinColors.Lavender
                } else {
                    QevinColors.TextSecondary
                },
                fontSize = 10.sp,
                fontWeight = if (conversation.unreadCount > 0) {
                    FontWeight.Medium
                } else {
                    FontWeight.Normal
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(QevinColors.PurpleGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = QevinColors.Background,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
private fun EmptyConversationState(
    selectedTab: HomeTab
) {
    val (icon, title) = when (selectedTab) {
        HomeTab.CHATS -> Icons.Outlined.ChatBubbleOutline to "No conversations yet"
        HomeTab.GROUPS -> Icons.Outlined.Groups to "No groups yet"
        HomeTab.CALLS -> Icons.Outlined.Call to "No calls yet"
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
                text = "Your next connection starts here.",
                color = QevinColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}