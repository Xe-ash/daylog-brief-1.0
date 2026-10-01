package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.BuildConfig
import com.example.data.AuthState
import com.example.data.SyncState
import com.example.data.WriteStatus
import com.example.ui.DaylogViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenScaffold
import com.example.ui.components.SegmentedPill
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: DaylogViewModel,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val writeLogs by viewModel.writeLogs.collectAsState()
    val totalWritesSucceeded by viewModel.totalWritesSucceeded.collectAsState()
    val totalWritesFailed by viewModel.totalWritesFailed.collectAsState()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsState()
    val context = LocalContext.current

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteConfirmationInput by remember { mutableStateOf("") }
    var showWakingWindowDialog by remember { mutableStateOf(false) }
    var isSyncingLocalData by remember { mutableStateOf(false) }
    var isRunningDiagnostic by remember { mutableStateOf(false) }
    var diagnosticResult by remember { mutableStateOf<String?>(null) }
    var showLogsExpanded by remember { mutableStateOf(false) }

    // Secret tap counter for developer settings: tap version number 7 times
    var versionTapCount by remember { mutableIntStateOf(0) }
    var developerUnlocked by remember { mutableStateOf(false) }
    val isDeveloperVisible = BuildConfig.DEBUG || developerUnlocked

    ScreenScaffold(
        title = "Settings",
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = DaylogTokens.ScreenHorizontalPadding,
                end = DaylogTokens.ScreenHorizontalPadding,
                top = 8.dp,
                bottom = DaylogTokens.ContentBottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(DaylogTokens.PanelGap)
        ) {
            // --- 1. ACCOUNT PANEL ---
            item {
                GlassPanel(variant = GlassPanelVariant.Standard) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Account",
                            style = DaylogTypography.Title,
                            color = DaylogTokens.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                        when (val currentAuth = authState) {
                            is AuthState.Authenticated -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(DaylogTokens.GlassFill)
                                            .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = DaylogTokens.TextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentAuth.profile.displayName.ifBlank { "User" },
                                            style = DaylogTypography.Body.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = DaylogTokens.TextPrimary
                                            )
                                        )
                                        // Email in Body size (not Title)
                                        Text(
                                            text = currentAuth.profile.email.ifBlank { "Signed in with Google" },
                                            style = DaylogTypography.Body,
                                            color = DaylogTokens.TextSecondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Sync state in one line ("Synced just now" with a small dot) and "Sync now" text button
                                val (syncText, syncColor) = when (syncState) {
                                    is SyncState.Synced -> {
                                        val timeStr = lastSyncTimestamp?.let {
                                            SimpleDateFormat("HH:mm", Locale.US).format(Date(it))
                                        } ?: "just now"
                                        "Synced at $timeStr" to DaylogTokens.Productive
                                    }
                                    is SyncState.Syncing -> "Syncing..." to DaylogTokens.Neutral
                                    is SyncState.Error -> "Sync failed" to DaylogTokens.Distraction
                                    else -> "Local only" to DaylogTokens.TextTertiary
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(syncColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = syncText,
                                            style = DaylogTypography.Label,
                                            color = DaylogTokens.TextSecondary
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            viewModel.forceSyncLocalData { count, err ->
                                                if (err != null) {
                                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Synced $count items", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "Sync now",
                                            style = DaylogTypography.Label.copy(
                                                color = DaylogTokens.AuroraCyan,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                GlassButton(
                                    text = "Log out",
                                    onClick = { viewModel.signOut() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            else -> {
                                Text(
                                    text = "You are currently using Daylog offline. Sign in to sync your data across devices.",
                                    style = DaylogTypography.Body,
                                    color = DaylogTokens.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryButton(
                                    text = "Sign in or register",
                                    onClick = onNavigateToAuth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. FOCUS PANEL ---
            item {
                GlassPanel(variant = GlassPanelVariant.Standard) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Focus",
                            style = DaylogTypography.Title,
                            color = DaylogTokens.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                        // Daily Target as SegmentedPill (2h / 4h / 6h / 8h)
                        Text(
                            text = "Daily focus target",
                            style = DaylogTypography.Label,
                            color = DaylogTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val targetOptions = listOf(2, 4, 6, 8)
                        val currentTargetHours = (profile.defaultDailyGoalMinutes / 60).let {
                            if (it in targetOptions) it else 4
                        }

                        SegmentedPill(
                            items = targetOptions,
                            selectedItem = currentTargetHours,
                            onItemSelected = { hrs ->
                                viewModel.updateProfile(profile.copy(defaultDailyGoalMinutes = hrs * 60))
                            },
                            itemLabel = { "${it}h" }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Waking window (wake and sleep times, tap to edit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                .background(DaylogTokens.GlassFill)
                                .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), RoundedCornerShape(DaylogTokens.InnerRadius))
                                .clickable { showWakingWindowDialog = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Waking window",
                                    style = DaylogTypography.Body.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${profile.wakeTime} — ${profile.sleepTime} (17h)",
                                    style = DaylogTypography.Label,
                                    color = DaylogTokens.TextSecondary
                                )
                            }

                            Text(
                                text = "Edit",
                                style = DaylogTypography.Label.copy(
                                    color = DaylogTokens.AuroraCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Accountability Toggle: Direct daily review
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Direct daily review",
                                    style = DaylogTypography.Body.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = DaylogTokens.TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Blunt end-of-day questions about your distractions.",
                                    style = DaylogTypography.Label,
                                    color = DaylogTokens.TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Switch(
                                checked = profile.brutalMode,
                                onCheckedChange = { checked ->
                                    viewModel.updateProfile(profile.copy(brutalMode = checked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DaylogTokens.TextPrimary,
                                    checkedTrackColor = DaylogTokens.AuroraViolet,
                                    uncheckedThumbColor = DaylogTokens.TextSecondary,
                                    uncheckedTrackColor = DaylogTokens.GlassFill
                                )
                            )
                        }
                    }
                }
            }

            // --- 3. DEVELOPER SECTION (Gated: BuildConfig.DEBUG or tap version 7 times) ---
            if (isDeveloperVisible) {
                item {
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Developer",
                                style = DaylogTypography.Title,
                                color = DaylogTokens.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                            // Firestore status & stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .background(DaylogTokens.GlassFill)
                                        .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(text = "Confirmed", style = DaylogTypography.Label, color = DaylogTokens.TextSecondary)
                                        Text(text = "$totalWritesSucceeded", style = DaylogTypography.Title, color = DaylogTokens.Productive)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .background(DaylogTokens.GlassFill)
                                        .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text(text = "Failed", style = DaylogTypography.Label, color = DaylogTokens.TextSecondary)
                                        Text(text = "$totalWritesFailed", style = DaylogTypography.Title, color = if (totalWritesFailed > 0) DaylogTokens.Distraction else DaylogTokens.TextPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Test Firestore action (single line, maxLines = 1, button wraps to content or "Test sync")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GlassButton(
                                    text = if (isRunningDiagnostic) "Testing..." else "Test sync",
                                    onClick = {
                                        isRunningDiagnostic = true
                                        viewModel.runDiagnostics { res ->
                                            isRunningDiagnostic = false
                                            diagnosticResult = if (res.isSuccess) res.getOrNull() else res.exceptionOrNull()?.message
                                        }
                                    },
                                    enabled = !isRunningDiagnostic,
                                    modifier = Modifier.weight(1f)
                                )

                                GlassButton(
                                    text = if (isSyncingLocalData) "Syncing..." else "Sync local data",
                                    onClick = {
                                        isSyncingLocalData = true
                                        viewModel.forceSyncLocalData { count, err ->
                                            isSyncingLocalData = false
                                            Toast.makeText(context, err ?: "Synced $count items", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !isSyncingLocalData,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Write logs toggle
                            Text(
                                text = if (showLogsExpanded) "Hide write logs" else "View write logs (${writeLogs.size})",
                                style = DaylogTypography.Label.copy(color = DaylogTokens.AuroraCyan),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { showLogsExpanded = !showLogsExpanded }
                                    .padding(vertical = 4.dp)
                            )

                            if (showLogsExpanded && writeLogs.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .background(DaylogTokens.Night)
                                        .border(BorderStroke(1.dp, DaylogTokens.GlassStroke), RoundedCornerShape(DaylogTokens.InnerRadius))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    writeLogs.take(5).forEach { log ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${log.operation}: ${log.path}",
                                                style = DaylogTypography.Label.copy(fontSize = 10.sp),
                                                color = DaylogTokens.TextSecondary,
                                                maxLines = 1
                                            )
                                            val statusCol = when (log.status) {
                                                WriteStatus.SUCCESS -> DaylogTokens.Productive
                                                WriteStatus.FAILED -> DaylogTokens.Distraction
                                                else -> DaylogTokens.Neutral
                                            }
                                            Text(
                                                text = log.status.name,
                                                style = DaylogTypography.Label.copy(fontSize = 10.sp, color = statusCol)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. DATA AND DANGER ZONE (At the very bottom, visually separated) ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (authState is AuthState.Authenticated) {
                        TextButton(
                            onClick = {
                                deleteConfirmationInput = ""
                                showDeleteAccountDialog = true
                            }
                        ) {
                            Text(
                                text = "Delete account",
                                style = DaylogTypography.Label.copy(
                                    color = DaylogTokens.Distraction,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Version number label - tap 7 times to unlock developer tools in release
                    Text(
                        text = "Version ${BuildConfig.VERSION_NAME}",
                        style = DaylogTypography.Label.copy(
                            fontSize = 11.sp,
                            color = DaylogTokens.TextTertiary
                        ),
                        modifier = Modifier
                            .clickable {
                                versionTapCount++
                                if (versionTapCount >= 7) {
                                    developerUnlocked = true
                                    Toast.makeText(context, "Developer settings enabled", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    // Waking window edit dialog
    if (showWakingWindowDialog) {
        var wakeInput by remember { mutableStateOf(profile.wakeTime) }
        var sleepInput by remember { mutableStateOf(profile.sleepTime) }

        AlertDialog(
            onDismissRequest = { showWakingWindowDialog = false },
            title = {
                Text("Waking window", style = DaylogTypography.Title, color = DaylogTokens.TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your standard schedule in 24-hour format (e.g. 06:00 to 23:00):", style = DaylogTypography.Body, color = DaylogTokens.TextSecondary)
                    OutlinedTextField(
                        value = wakeInput,
                        onValueChange = { wakeInput = it },
                        label = { Text("Wake time (HH:mm)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DaylogTokens.AuroraCyan,
                            unfocusedBorderColor = DaylogTokens.TextTertiary,
                            focusedTextColor = DaylogTokens.TextPrimary,
                            unfocusedTextColor = DaylogTokens.TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sleepInput,
                        onValueChange = { sleepInput = it },
                        label = { Text("Sleep time (HH:mm)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DaylogTokens.AuroraCyan,
                            unfocusedBorderColor = DaylogTokens.TextTertiary,
                            focusedTextColor = DaylogTokens.TextPrimary,
                            unfocusedTextColor = DaylogTokens.TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProfile(profile.copy(wakeTime = wakeInput.trim(), sleepTime = sleepInput.trim()))
                        showWakingWindowDialog = false
                    }
                ) {
                    Text("Save", color = DaylogTokens.AuroraCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWakingWindowDialog = false }) {
                    Text("Cancel", color = DaylogTokens.TextSecondary)
                }
            },
            containerColor = DaylogTokens.NightDeep
        )
    }

    // Delete Account confirmation dialog (requires typing DELETE)
    if (showDeleteAccountDialog) {
        val isConfirmed = deleteConfirmationInput.trim() == "DELETE"

        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text("Delete account", style = DaylogTypography.Title, color = DaylogTokens.Distraction)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This will permanently remove your cloud database and all synced records. To confirm, please type DELETE below:",
                        style = DaylogTypography.Body,
                        color = DaylogTokens.TextSecondary
                    )
                    OutlinedTextField(
                        value = deleteConfirmationInput,
                        onValueChange = { deleteConfirmationInput = it },
                        placeholder = { Text("Type DELETE", color = DaylogTokens.TextTertiary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DaylogTokens.Distraction,
                            unfocusedBorderColor = DaylogTokens.TextTertiary,
                            focusedTextColor = DaylogTokens.TextPrimary,
                            unfocusedTextColor = DaylogTokens.TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = isConfirmed,
                    onClick = {
                        viewModel.deleteAccount { errMsg ->
                            Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                        }
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text(
                        text = "Permanently delete",
                        color = if (isConfirmed) DaylogTokens.Distraction else DaylogTokens.TextTertiary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = DaylogTokens.TextSecondary)
                }
            },
            containerColor = DaylogTokens.NightDeep
        )
    }

    // Diagnostic result dialog
    if (diagnosticResult != null) {
        AlertDialog(
            onDismissRequest = { diagnosticResult = null },
            title = { Text("Diagnostic result", style = DaylogTypography.Title, color = DaylogTokens.TextPrimary) },
            text = { Text(diagnosticResult!!, style = DaylogTypography.Body, color = DaylogTokens.TextSecondary) },
            confirmButton = {
                TextButton(onClick = { diagnosticResult = null }) {
                    Text("Close", color = DaylogTokens.AuroraCyan)
                }
            },
            containerColor = DaylogTokens.NightDeep
        )
    }
}

@Preview(name = "SettingsScreen Preview")
@Composable
fun SettingsScreenPreview() {
    DaylogTheme {
        // SettingsScreen preview
    }
}
