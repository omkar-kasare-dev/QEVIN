package com.example.qevin.feature.auth



import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.core.designsystem.component.QevinTextField
import com.example.qevin.core.designsystem.dimension.QevinSpacing
import com.example.qevin.feature.compnents.QevinAuthBackground
import com.example.qevin.feature.compnents.QevinAuthPillButton


@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: (name: String, mobileNumber: String, password: String) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    QevinAuthBackground(topRightGlow = true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = QevinSpacing.ScreenHorizontal,
                    vertical = QevinSpacing.Default
                )
        ) {
            // Top Back Navigation
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(x = (-QevinSpacing.Medium))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = QevinColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Header Texts
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = QevinColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(QevinSpacing.ExtraSmall))
            Text(
                text = "Let's get you started",
                style = MaterialTheme.typography.bodyMedium,
                color = QevinColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Name Field
            QevinTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                placeholder = "Name",
                keyboardType = KeyboardType.Text,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        tint = QevinColors.TextSecondary,
                        modifier = Modifier.size(QevinSpacing.IconSmall)
                    )
                }
            )

            Spacer(modifier = Modifier.height(QevinSpacing.Default))

            // Mobile Number Field
            QevinTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = "Mobile Number",
                placeholder = "Mobile Number",
                keyboardType = KeyboardType.Phone,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Smartphone,
                        contentDescription = null,
                        tint = QevinColors.TextSecondary,
                        modifier = Modifier.size(QevinSpacing.IconSmall)
                    )
                }
            )

            Spacer(modifier = Modifier.height(QevinSpacing.Default))

            // Password Field
            QevinTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Password",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = QevinColors.TextSecondary,
                        modifier = Modifier.size(QevinSpacing.IconSmall)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = "Toggle Password Visibility",
                            tint = QevinColors.TextSecondary,
                            modifier = Modifier.size(QevinSpacing.IconSmall)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Create Account CTA
            QevinAuthPillButton(
                text = "Create Account",
                onClick = { onCreateAccountClick(name, mobileNumber, password) }
            )

            Spacer(modifier = Modifier.height(QevinSpacing.Large))

            // Terms & Privacy Policy Text
            val legalText = buildAnnotatedString {
                withStyle(SpanStyle(color = QevinColors.TextSecondary)) {
                    append("By continuing, you agree to our\n")
                }
                withStyle(
                    SpanStyle(
                        color = QevinColors.Lavender,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Terms")
                }
                withStyle(SpanStyle(color = QevinColors.TextSecondary)) {
                    append(" & ")
                }
                withStyle(
                    SpanStyle(
                        color = QevinColors.Lavender,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Privacy Policy")
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = legalText,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { onTermsClick() }
                )
            }
        }
    }
}