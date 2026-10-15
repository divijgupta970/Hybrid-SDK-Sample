package com.divij.hybridsdksample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divij.hybridsdksample.ui.InsightsUi
import com.divij.hybridsdksample.ui.NewNoteScreen
import com.divij.hybridsdksample.ui.NoteDetailScreen
import com.divij.hybridsdksample.ui.NotesListScreen
import com.divij.hybridsdksample.ui.NotesViewModel
import com.divij.hybridsdksample.ui.PrivacySheet
import com.divij.hybridsdksample.ui.theme.HybridSDKSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HybridSDKSampleTheme {
                val viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory)
                val state by viewModel.state.collectAsStateWithLifecycle()
                val insights by viewModel.insights.collectAsStateWithLifecycle()
                var openNoteId by rememberSaveable { mutableStateOf<Long?>(null) }
                var writing by rememberSaveable { mutableStateOf(false) }
                var privacyOpen by rememberSaveable { mutableStateOf(false) }

                val openNote = openNoteId?.let(viewModel::note)
                when {
                    writing -> {
                        BackHandler { writing = false }
                        NewNoteScreen(
                            onBack = { writing = false },
                            onSave = { title, body ->
                                openNoteId = viewModel.addNote(title, body)
                                writing = false
                            },
                        )
                    }
                    openNote != null -> {
                        BackHandler { openNoteId = null }
                        NoteDetailScreen(
                            note = openNote,
                            insights = insights[openNote.id] ?: InsightsUi(),
                            onBack = { openNoteId = null },
                            onTag = { viewModel.tag(openNote) },
                            onSummarise = { viewModel.summarise(openNote) },
                        )
                    }
                    else -> NotesListScreen(
                        state = state,
                        onNoteClick = { openNoteId = it.id },
                        onNewNote = { writing = true },
                        onDownloadModel = viewModel::downloadModel,
                        onOpenPrivacy = { privacyOpen = true },
                    )
                }
                if (privacyOpen) {
                    PrivacySheet(
                        privacy = state.privacy,
                        onDeviceOnlyChange = viewModel::setDeviceOnly,
                        onDismiss = { privacyOpen = false },
                    )
                }
            }
        }
    }
}
