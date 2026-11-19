package com.example.qevin.feature.auth



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.core.designsystem.component.QevinTextField
import com.example.qevin.core.designsystem.dimension.QevinSpacing
import com.example.qevin.feature.compnents.QevinAuthBackground
import com.example.qevin.feature.compnents.QevinAuthPillButton
import com.example.qevin.feature.compnents.QevinBrandHeader

@Composable
fun LoginScreen(
    onLoginClick: (identifier: String, password: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    QevinAuthBackground(topRightGlow = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = QevinSpacing.ScreenHorizontal,
                    vertical = QevinSpacing.ScreenVertical
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Brand Logo & Wordmark
            QevinBrandHeader()

            Spacer(modifier = Modifier.height(QevinSpacing.Huge))

            // Header Texts
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium,
                    color = QevinColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(QevinSpacing.ExtraSmall))
                Text(
                    text = "Login to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = QevinColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.Large))

            // Mobile Number or Email Field
            QevinTextField(
                value = identifier,
                onValueChange = { identifier = it },
                label = "Mobile Number or Email",
                placeholder = "Mobile Number or Email",
                keyboardType = KeyboardType.Email,
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

            // Login CTA
            QevinAuthPillButton(
                text = "Login",
                onClick = { onLoginClick(identifier, password) }
            )

            Spacer(modifier = Modifier.height(QevinSpacing.Large))

            // "or" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = QevinColors.Divider
                )
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodySmall,
                    color = QevinColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = QevinSpacing.Default)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = QevinColors.Divider
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.Large))

            // Continue with Google Button
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(QevinSpacing.ButtonHeight),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = QevinColors.Surface,
                    contentColor = QevinColors.TextPrimary
                ),
                border = BorderStroke(1.dp, QevinColors.Divider)
            ) {
                // Replace with R.drawable.ic_google_logo
                Text(
                    text = "G",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4285F4)
                    )
                )
                Spacer(modifier = Modifier.width(QevinSpacing.Medium))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.titleSmall,
                    color = QevinColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Sign Up Footer
            val signUpText = buildAnnotatedString {
                withStyle(SpanStyle(color = QevinColors.TextSecondary)) {
                    append("Don't have an account? ")
                }
                withStyle(
                    SpanStyle(
                        color = QevinColors.Lavender,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append("Sign Up")
                }
            }

            Text(
                text = signUpText,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}