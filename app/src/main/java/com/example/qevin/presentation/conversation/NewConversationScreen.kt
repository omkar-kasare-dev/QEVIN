package com.example.qevin.presentation.conversation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qevin.core.designsystem.color.QevinColors

private data class NewContact(val id: String, val name: String, val phone: String, val initials: String)

@Composable
fun NewConversationScreen(
    onBackClick: () -> Unit = {},
    onNewGroupClick: () -> Unit = {},
    onContactSelected: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val contacts = listOf(
        NewContact("rahul", "Rahul Sharma", "+1 555-0112", "RS"),
        NewContact("priya", "Priya Deshmukh", "+1 555-0189", "PD"),
        NewContact("aman", "Aman Patil", "+1 555-0143", "AP")
    )

    val filtered = if (query.isBlank()) contacts else contacts.filter { it.name.contains(query, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(QevinColors.Background.copy(alpha = 0.95f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick, modifier = Modifier.size(36.dp)) {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back", tint = QevinColors.TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "New Chat", color = QevinColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    // Search Input
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .height(38.dp)
                            .clip(RoundedCornerShape(19.dp))
                            .background(QevinColors.SurfaceVariant)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = QevinColors.Lavender, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) {
                                        Text(text = "Search name or number...", color = QevinColors.TextSecondary, fontSize = 13.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // New Group option item
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onNewGroupClick)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(QevinColors.Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.GroupAdd, contentDescription = "New Group", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "New Group", color = QevinColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                items(filtered, key = { it.id }) { contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onContactSelected(contact.id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(QevinColors.PrimaryDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = contact.initials, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = contact.name, color = QevinColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = contact.phone, color = QevinColors.TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}