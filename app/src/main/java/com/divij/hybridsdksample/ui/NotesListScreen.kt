package com.divij.hybridsdksample.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.divij.hybridsdksample.R
import com.divij.hybridsdksample.notes.Note
import java.time.format.DateTimeFormatter

private val DateFormat = DateTimeFormatter.ofPattern("EEE, d MMM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    state: NotesUiState,
    onNoteClick: (Note) -> Unit,
    onNewNote: () -> Unit,
    onDownloadModel: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Notes") },
                actions = {
                    IconButton(onClick = onOpenPrivacy) {
                        Icon(painterResource(R.drawable.ic_shield), contentDescription = "Privacy")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewNote) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = "New note")
            }
        },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(key = "model") { ModelStatusBanner(state.model, state.privacy, onDownloadModel) }
            items(state.notes, key = { it.id }) { note ->
                Card(Modifier.fillMaxWidth().clickable { onNoteClick(note) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(note.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            note.formattedDate(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            note.body,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

internal fun Note.formattedDate(): String = date.format(DateFormat)
