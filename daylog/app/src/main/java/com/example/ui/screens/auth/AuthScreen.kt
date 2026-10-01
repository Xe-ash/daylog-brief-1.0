package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthState
import com.example.ui.DaylogViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.PrimaryButton
import com.example.ui.components.SegmentedPill
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: DaylogViewModel,
    onAuthSuccess: () -> Unit,
    onContinueOffline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    if (authState is AuthState.Authenticated) {
        onAuthSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DaylogTokens.ScreenHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title Hero Card
            GlassPanel(variant = GlassPanelVariant.Hero) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(DaylogTokens.AuroraViolet.copy(alpha = 0.30f))
                            .border(BorderStroke(1.dp, DaylogTokens.AuroraViolet), CircleShape)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Daylog",
                        style = DaylogTypography.Display,
                        color = DaylogTokens.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Time ledger & focus tracker",
                        style = DaylogTypography.Body,
                        color = DaylogTokens.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mode switch pill
            SegmentedPill(
                items = listOf("Sign In", "Create Account"),
                selectedItem = if (isSignUp) "Create Account" else "Sign In",
                onItemSelected = {
                    isSignUp = it == "Create Account"
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassPanel(variant = GlassPanelVariant.Standard) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Google Sign In
                    PrimaryButton(
                        text = "Continue with Google",
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = viewModel.signInWithGoogle()
                                isLoading = false
                                result.onFailure { err ->
                                    errorMessage = err.localizedMessage ?: "Google Sign In failed."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(DaylogTokens.GlassHighlightTop))
                        Text(
                            text = "  or email  ",
                            style = DaylogTypography.Label,
                            color = DaylogTokens.TextTertiary
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(DaylogTokens.GlassHighlightTop))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSignUp) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DaylogTokens.AuroraCyan,
                                unfocusedBorderColor = DaylogTokens.TextTertiary,
                                focusedTextColor = DaylogTokens.TextPrimary,
                                unfocusedTextColor = DaylogTokens.TextPrimary
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DaylogTokens.AuroraCyan,
                            unfocusedBorderColor = DaylogTokens.TextTertiary,
                            focusedTextColor = DaylogTokens.TextPrimary,
                            unfocusedTextColor = DaylogTokens.TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DaylogTokens.AuroraCyan,
                            unfocusedBorderColor = DaylogTokens.TextTertiary,
                            focusedTextColor = DaylogTokens.TextPrimary,
                            unfocusedTextColor = DaylogTokens.TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { err ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                .border(BorderStroke(1.dp, DaylogTokens.Distraction.copy(alpha = 0.5f)), RoundedCornerShape(DaylogTokens.InnerRadius))
                                .background(DaylogTokens.Distraction.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = err,
                                style = DaylogTypography.Label,
                                color = DaylogTokens.Distraction,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = DaylogTokens.AuroraViolet
                            )
                        }
                    } else {
                        PrimaryButton(
                            text = if (isSignUp) "Register account" else "Sign in",
                            enabled = email.isNotBlank() && password.length >= 6,
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val res = if (isSignUp) {
                                        viewModel.signUpWithEmail(email.trim(), password, displayName.trim())
                                    } else {
                                        viewModel.signInWithEmail(email.trim(), password)
                                    }
                                    isLoading = false
                                    res.onFailure { err ->
                                        errorMessage = err.localizedMessage ?: "Authentication failed."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GlassButton(
                        text = "Continue offline",
                        onClick = onContinueOffline,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(name = "AuthScreen Preview")
@Composable
fun AuthScreenPreview() {
    DaylogTheme {
        // AuthScreen preview
    }
}
