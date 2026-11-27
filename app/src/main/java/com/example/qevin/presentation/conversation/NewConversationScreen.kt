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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.presentation.home.ErrorState
import com.example.qevin.presentation.home.LoadingState
import com.example.qevin.presentation.home.avatarColorFor

/**
 * @param onContactSelected receives the direct conversation ID to open.
 */
@Composable
fun NewConversationScreen(
    onBackClick: () -> Unit = {},
    onNewGroupClick: () -> Unit = {},
    onContactSelected: (conversationId: String) -> Unit = {},
    viewModel: NewConversationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Saved across rotation and process death. The ViewModel is told again
    // on entry, in case it was recreated empty.
    var query by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.onQueryChange(query)
        runCatching { focusRequester.requestFocus() }
    }

    val isTooShort = query.trim().removePrefix("@").trim().length < MIN_QUERY_LENGTH
    val status = if (isTooShort) SearchStatus.IDLE else uiState.status

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
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = QevinColors.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Chat",
                            color = QevinColors.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Search input
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
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = QevinColors.Lavender,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        BasicTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                viewModel.onQueryChange(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            cursorBrush = SolidColor(QevinColors.Lavender),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            decorationBox = { inner ->
                                Box {
                                    if (query.isEmpty()) {
                                        Text(
                                            text = "Search name, @username or number...",
                                            color = QevinColors.TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                    inner()
                                }
                            }
                        )

                        if (query.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Clear search",
                                tint = QevinColors.TextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        query = ""
                                        viewModel.onQueryChange("")
                                    }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (query.isBlank()) {
                    NewGroupRow(onClick = onNewGroupClick)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        status == SearchStatus.ERROR -> ErrorState(
                            message = uiState.errorMessage.orEmpty(),
                            onRetryClick = viewModel::retry
                        )

                        (status == SearchStatus.RESULTS || status == SearchStatus.SEARCHING) &&
                                uiState.results.isNotEmpty() -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(
                                items = uiState.results,
                                key = { it.uid }
                            ) { result ->
                                SearchResultRow(
                                    result = result,
                                    onClick = { onContactSelected(result.conversationId) }
                                )
                            }
                        }

                        status == SearchStatus.SEARCHING -> LoadingState()

                        status == SearchStatus.EMPTY -> InfoState(
                            icon = Icons.Outlined.SearchOff,
                            title = "No users found",
                            subtitle = "Check the spelling, or try their username."
                        )

                        else -> InfoState(
                            icon = Icons.Outlined.PersonSearch,
                            title = "Find people",
                            subtitle = "Search by name, @username, or full phone number with country code."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGroupRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
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
            Icon(
                imageVector = Icons.Rounded.GroupAdd,
                contentDescription = "New Group",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "New Group",
            color = QevinColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SearchResultRow(
    result: UserSearchResultUi,
    onClick: () -> Unit
) {
    val baseColor = avatarColorFor(result.uid)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(baseColor, baseColor.copy(alpha = 0.7f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Always drawn. A loaded photo covers it; a missing or failed
            // photo leaves the initials showing.
            Text(
                text = result.initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (!result.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = result.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                color = QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (result.username != null) {
                Text(
                    text = "@${result.username}",
                    color = QevinColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun InfoState(
    icon: ImageVector,
    title: String,
    subtitle: String
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