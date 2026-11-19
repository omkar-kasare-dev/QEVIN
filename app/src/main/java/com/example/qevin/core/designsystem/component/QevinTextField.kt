package com.example.qevin.core.designsystem.component


/*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.qevin.core.designsystem.color.QevinColors

@Composable
fun QevinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            Text(text = label)
        },
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(text = placeholder)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        singleLine = singleLine,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = QevinColors.Primary,
            unfocusedBorderColor = QevinColors.Outline,
            focusedLabelColor = QevinColors.Lavender,
            unfocusedLabelColor = QevinColors.TextSecondary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = QevinColors.Primary,
            focusedContainerColor = QevinColors.Surface,
            unfocusedContainerColor = QevinColors.Surface,
            errorBorderColor = QevinColors.Error
        )
    )
}

 */


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.qevin.core.designsystem.color.QevinColors

@Composable
fun QevinTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder.ifEmpty { label },
                color = QevinColors.TextSecondary
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        singleLine = singleLine,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = QevinColors.Primary,
            unfocusedBorderColor = QevinColors.Divider,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = QevinColors.Primary,
            focusedContainerColor = QevinColors.Surface,
            unfocusedContainerColor = QevinColors.Surface,
            errorBorderColor = QevinColors.Error
        )
    )
}