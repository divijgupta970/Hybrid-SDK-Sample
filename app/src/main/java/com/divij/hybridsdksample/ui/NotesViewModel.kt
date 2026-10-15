package com.divij.hybridsdksample.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.divij.hybridsdksample.AppContainer
import com.divij.hybridsdksample.NotesApp
import com.divij.hybridsdksample.ai.ModelState
import com.divij.hybridsdksample.ai.PrivacyMode
import com.divij.hybridsdksample.ai.SummarySoFar
import com.divij.hybridsdksample.ai.Tagged
import com.divij.hybridsdksample.notes.Note
import com.google.firebase.ai.type.FirebaseAIOnDeviceNotAvailableException
import com.google.firebase.ai.type.QuotaExceededException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotesUiState(
    val notes: List<Note> = emptyList(),
    val model: ModelState = ModelState.Checking,
    val privacy: PrivacyMode = PrivacyMode.DEVICE_FIRST,
)

sealed interface TagUi {
    data object Idle : TagUi
    data object Loading : TagUi
    data class Done(val result: Tagged) : TagUi
    data class Error(val message: String) : TagUi
}

sealed interface SummaryUi {
    data object Idle : SummaryUi
    data class Streaming(val soFar: SummarySoFar) : SummaryUi
    data class Done(val summary: SummarySoFar) : SummaryUi
    data class Error(val message: String) : SummaryUi
}

data class InsightsUi(val tag: TagUi = TagUi.Idle, val summary: SummaryUi = SummaryUi.Idle)

class NotesViewModel(private val app: AppContainer) : ViewModel() {

    val state: StateFlow<NotesUiState> =
        combine(app.notes.notes, app.modelManager.state, app.privacy.mode, ::NotesUiState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    private val _insights = MutableStateFlow<Map<Long, InsightsUi>>(emptyMap())
    val insights: StateFlow<Map<Long, InsightsUi>> = _insights.asStateFlow()

    private var downloadJob: Job? = null

    init {
        viewModelScope.launch { app.modelManager.refresh() }
    }

    fun downloadModel() {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch { app.modelManager.download() }
    }

    fun setDeviceOnly(deviceOnly: Boolean) = app.privacy.setDeviceOnly(deviceOnly)

    fun note(id: Long): Note? = app.notes.note(id)

    fun addNote(title: String, body: String): Long = app.notes.add(title, body).id

    fun tag(note: Note) {
        val mode = app.privacy.mode.value
        blockedReason(mode)?.let { return updateInsights(note.id) { copy(tag = TagUi.Error(it)) } }
        updateInsights(note.id) { copy(tag = TagUi.Loading) }
        viewModelScope.launch {
            val tag = runCatching { app.noteAi.tag(note.body, mode) }
                .fold({ TagUi.Done(it) }, { TagUi.Error(it.toMessage()) })
            updateInsights(note.id) { copy(tag = tag) }
        }
    }

    fun summarise(note: Note) {
        val mode = app.privacy.mode.value
        blockedReason(mode)?.let { return updateInsights(note.id) { copy(summary = SummaryUi.Error(it)) } }
        viewModelScope.launch {
            var last = SummarySoFar("", null)
            app.noteAi.summarise(note.body, mode)
                .catch { error -> updateInsights(note.id) { copy(summary = SummaryUi.Error(error.toMessage())) } }
                .collect { soFar ->
                    last = soFar
                    updateInsights(note.id) { copy(summary = SummaryUi.Streaming(soFar)) }
                }
            if (insights.value[note.id]?.summary is SummaryUi.Streaming) {
                updateInsights(note.id) { copy(summary = SummaryUi.Done(last)) }
            }
        }
    }

    // In device-only mode the request would fail anyway, so explain why up front instead of sending it.
    private fun blockedReason(mode: PrivacyMode): String? {
        if (mode != PrivacyMode.DEVICE_ONLY) return null
        return when (state.value.model) {
            is ModelState.Ready -> null
            ModelState.Unsupported -> "This phone can't run Gemini Nano. Turn off \"Keep notes on this device\" to use the cloud."
            else -> "Gemini Nano isn't ready yet. Download it from the banner on the notes list."
        }
    }

    private fun updateInsights(id: Long, change: InsightsUi.() -> InsightsUi) =
        _insights.update { it + (id to (it[id] ?: InsightsUi()).change()) }

    private fun Throwable.toMessage(): String {
        Log.w(TAG, "AI request failed", this)
        return when (this) {
            is FirebaseAIOnDeviceNotAvailableException -> "Gemini Nano isn't available on this phone right now."
            is QuotaExceededException -> "The cloud model is busy. Try again in a minute."
            else -> "Couldn't read this note. Try again."
        }
    }

    companion object {
        private const val TAG = "NotesViewModel"

        val Factory = viewModelFactory {
            initializer { NotesViewModel((this[APPLICATION_KEY] as NotesApp).container) }
        }
    }
}
