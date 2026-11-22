package com.example.qevin.feature.auth

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.qevin.core.designsystem.color.QevinColors
import com.example.qevin.core.designsystem.component.QevinTextField
import com.example.qevin.core.designsystem.dimension.QevinSpacing
import com.example.qevin.feature.auth.register.RegisterViewModel
import com.example.qevin.feature.compnents.QevinAuthBackground
import com.example.qevin.feature.compnents.QevinAuthPillButton

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onRegistrationCompleted: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val activity = context as Activity

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Navigate only after phone verification and registration completion.
    LaunchedEffect(uiState.isRegistrationCompleted) {
        if (uiState.isRegistrationCompleted) {
            onRegistrationCompleted()
        }
    }

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

            // Back Navigation
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.offset(
                    x = -QevinSpacing.Medium
                ),
                enabled = !uiState.isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = QevinColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Header
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

            // Registration Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.5f
                ),
                contentColor = QevinColors.Lavender
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        if (!uiState.isLoading) {
                            selectedTab = 0
                            viewModel.clearError()
                        }
                    },
                    text = { Text("Email") },
                    enabled = !uiState.isLoading
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        if (!uiState.isLoading) {
                            selectedTab = 1
                            viewModel.clearError()
                        }
                    },
                    text = { Text("Phone") },
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.Large))

            // Display errors
            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(QevinSpacing.Default))
            }

            // EMAIL TAB
            if (selectedTab == 0) {

                // Display Name
                QevinTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    placeholder = "Enter your name",
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

                // Email
                QevinTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Address",
                    placeholder = "Enter your email",
                    keyboardType = KeyboardType.Email,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = QevinColors.TextSecondary,
                            modifier = Modifier.size(QevinSpacing.IconSmall)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(QevinSpacing.Default))

                // Password
                QevinTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "Create a password",
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
                        IconButton(
                            onClick = {
                                isPasswordVisible = !isPasswordVisible
                            }
                        ) {
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

                // Create Email Account
                /*
                QevinAuthPillButton(
                    text = if (uiState.isLoading) {
                        "Please wait..."
                    } else {
                        "Continue to Phone Verification"
                    },
                    onClick = {
                        if (!uiState.isLoading) {
                            viewModel.onCreateAccountClicked(
                                activity = activity,
                                displayName = name.trim(),
                                email = email.trim(),
                                phoneNumber = mobileNumber.trim(),
                                password = password
                            )

                            selectedTab = 1
                        }
                    }
                )
                */
                QevinAuthPillButton(
                    text = if (uiState.isLoading) {
                        "Creating Account..."
                    } else {
                        "Continue to Phone Verification"
                    },
                    onClick = {
                        if (!uiState.isLoading) {
                            viewModel.onCreateAccountClicked(
                                displayName = name,
                                email = email,
                                phoneNumber = mobileNumber,
                                password = password
                            )
                        }
                    }
                )

                LaunchedEffect(uiState.isEmailAccountCreated) {
                    if (uiState.isEmailAccountCreated) {
                        selectedTab = 1
                    }
                }
            }

            // PHONE TAB
            if (selectedTab == 1) {

                Text(
                    text = "Verify your phone number",
                    style = MaterialTheme.typography.titleMedium,
                    color = QevinColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(QevinSpacing.ExtraSmall))

                Text(
                    text = "We'll send an OTP to confirm your mobile number.",
                    style = MaterialTheme.typography.bodySmall,
                    color = QevinColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(QevinSpacing.Large))

                // Mobile Number
                QevinTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = "Mobile Number",
                    placeholder = "Enter your mobile number",
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

                // Send OTP
                if (!uiState.isOtpSent && !uiState.isPhoneVerified) {
                    QevinAuthPillButton(
                        text = if (uiState.isLoading) {
                            "Sending OTP..."
                        } else {
                            "Send OTP"
                        },
                        onClick = {
                            if (!uiState.isLoading) {
                                viewModel.requestPhoneOtp(
                                    activity = activity,
                                    phoneNumber = mobileNumber.trim()
                                )
                            }
                        }
                    )
                }

                // OTP Section
                if (uiState.isOtpSent && !uiState.isPhoneVerified) {

                    Spacer(modifier = Modifier.height(QevinSpacing.Large))

                    QevinTextField(
                        value = uiState.otp,
                        onValueChange = viewModel::onOtpChanged,
                        label = "Verification Code",
                        placeholder = "Enter 6-digit OTP",
                        keyboardType = KeyboardType.Number,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = QevinColors.TextSecondary,
                                modifier = Modifier.size(QevinSpacing.IconSmall)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

                    QevinAuthPillButton(
                        text = if (uiState.isLoading) {
                            "Verifying..."
                        } else {
                            "Verify OTP"
                        },
                        onClick = {
                            if (!uiState.isLoading) {
                                viewModel.onVerifyOtpClicked()
                            }
                        }
                    )
                }

                // Phone Verified
                if (uiState.isPhoneVerified) {
                    Text(
                        text = "Phone number verified successfully.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
//----------------------------
                if (
                    uiState.isPhoneVerified &&
                    !uiState.isRegistrationCompleted &&
                    !uiState.isLoading &&
                    uiState.errorMessage != null
                ) {
                    Button(
                        onClick = {
                            viewModel.retryProfileCreation()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Retry Profile Creation")
                    }
                }

                // Registration Complete
                if (uiState.isRegistrationCompleted) {
                    Text(
                        text = "Your QEVIN account is ready!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(QevinSpacing.Default))

                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(QevinSpacing.IconLarge),
                    color = QevinColors.Lavender
                )
            }

            Spacer(modifier = Modifier.height(QevinSpacing.ExtraLarge))

            // Terms and Privacy Policy
            val legalText = buildAnnotatedString {
                withStyle(
                    SpanStyle(color = QevinColors.TextSecondary)
                ) {
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

                withStyle(
                    SpanStyle(color = QevinColors.TextSecondary)
                ) {
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
                    modifier = Modifier.clickable {
                        onTermsClick()
                    }
                )
            }
        }
    }
}
