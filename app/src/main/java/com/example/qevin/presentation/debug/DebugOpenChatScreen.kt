package com.example.qevin.presentation.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.domain.model.ConversationIds

/** TEMPORARY: opens a real direct conversation by the other user's UID. */
@Composable
fun DebugOpenChatScreen(
    onBackClick: () -> Unit,
    onOpenChat: (conversationId: String) -> Unit,
    viewModel: DebugOpenChatViewModel = hiltViewModel()
) {
    val myUid by viewModel.myUid.collectAsState()
    var otherUid by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QevinColors.Background)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBackClick) { Text("Back") }

        Text("DEBUG: open real chat", color = QevinColors.TextPrimary)
        Spacer(Modifier.height(12.dp))

        Text("My UID (long-press to copy):", color = QevinColors.TextSecondary)
        SelectionContainer {
            Text(myUid ?: "Not signed in", color = QevinColors.Lavender)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = otherUid,
            onValueChange = { otherUid = it; error = null },
            label = { Text("Other user's UID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        error?.let { Text(it, color = QevinColors.TextSecondary) }

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = myUid != null,
            onClick = {
                runCatching { ConversationIds.direct(myUid!!, otherUid.trim()) }
                    .onSuccess(onOpenChat)
                    .onFailure { error = it.message }
            }
        ) { Text("Open chat") }
    }
}