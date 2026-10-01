package com.example.ui.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.DaylogViewModel
import com.example.ui.components.EmptyState
import com.example.ui.components.GlassPanel
import com.example.ui.components.GlassPanelVariant
import com.example.ui.components.PrimaryButton
import com.example.ui.components.ScreenScaffold
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography
import com.example.util.DateTimeUtils

@Composable
fun NotesScreen(
    viewModel: DaylogViewModel,
    modifier: Modifier = Modifier
) {
    val dayNotes by viewModel.dayNotes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var newNoteContent by remember { mutableStateOf("") }
    val notesForDate = dayNotes.filter { it.date == selectedDate }

    ScreenScaffold(
        title = "Notes",
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
            // New note input
            item {
                GlassPanel(variant = GlassPanelVariant.Standard) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Add note for $selectedDate",
                            style = DaylogTypography.Title,
                            color = DaylogTokens.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(DaylogTokens.TitleContentGap))

                        OutlinedTextField(
                            value = newNoteContent,
                            onValueChange = { newNoteContent = it },
                            placeholder = { Text("Write your thoughts, observations, or reflections...", color = DaylogTokens.TextTertiary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DaylogTokens.AuroraCyan,
                                unfocusedBorderColor = DaylogTokens.TextTertiary,
                                focusedTextColor = DaylogTokens.TextPrimary,
                                unfocusedTextColor = DaylogTokens.TextPrimary
                            ),
                            shape = RoundedCornerShape(DaylogTokens.InnerRadius),
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        PrimaryButton(
                            text = "Save note",
                            enabled = newNoteContent.isNotBlank(),
                            onClick = {
                                viewModel.saveNote(newNoteContent.trim())
                                newNoteContent = ""
                            },
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Existing notes
            if (notesForDate.isEmpty()) {
                item {
                    EmptyState(
                        title = "No notes for this date",
                        body = "Record quick observations and reflections throughout your day."
                    )
                }
            } else {
                items(notesForDate, key = { it.id }) { note ->
                    GlassPanel(variant = GlassPanelVariant.Standard) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DateTimeUtils.formatTime(note.createdAt),
                                    style = DaylogTypography.Label,
                                    color = DaylogTokens.AuroraCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    style = DaylogTypography.Body,
                                    color = DaylogTokens.TextPrimary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteNote(note.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete note",
                                    tint = DaylogTokens.TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "NotesScreen Preview")
@Composable
fun NotesScreenPreview() {
    DaylogTheme {
        // NotesScreen preview
    }
}
