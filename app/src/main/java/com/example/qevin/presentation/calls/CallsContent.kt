package com.example.qevin.presentation.calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.presentation.home.ErrorState
import com.example.qevin.presentation.home.LoadingState
import com.example.qevin.presentation.home.OfflineBanner


/**
 * The call-history list with its loading, offline, error and empty states.
 * Used by the Home "Calls" tab and by the standalone Calls route, so there
 * is one implementation behind both entry points.
 *
 * @param onCallClick receives the conversation ID to open.
 */
@Composable
fun CallsContent(
    onCallClick: (conversationId: String) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 16.dp,
    viewModel: CallsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        if (uiState.isOffline && uiState.calls.isNotEmpty()) {
            OfflineBanner(message = "You're offline. Showing saved calls.")
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            // Brief moment after logout, before navigation leaves the screen.
            uiState.isSignedOut -> Box(modifier = Modifier.fillMaxSize())

            uiState.isLoading -> LoadingState()

            uiState.errorMessage != null -> ErrorState(
                message = uiState.errorMessage.orEmpty(),
                onRetryClick = viewModel::retry
            )

            uiState.calls.isEmpty() -> CallsEmptyState(
                showOfflineHint = uiState.isOffline
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 2.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(
                    items = uiState.calls,
                    key = { it.id }
                ) { call ->
                    CallListItem(
                        call = call,
                        onClick = {
                            // A record with no resolvable chat does nothing.
                            call.conversationId?.let(onCallClick)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallsEmptyState(showOfflineHint: Boolean) {
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
                    imageVector = Icons.Outlined.Call,
                    contentDescription = "No calls",
                    tint = QevinColors.Lavender,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No calls yet",
                color = QevinColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (showOfflineHint) {
                    "Connect to the internet to load your calls."
                } else {
                    "Your incoming, outgoing and missed calls will appear here."
                },
                color = QevinColors.TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}