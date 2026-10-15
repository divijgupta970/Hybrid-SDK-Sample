package com.divij.hybridsdksample.ai

import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.OnDeviceExtension
import com.google.firebase.ai.OnDeviceModelStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface ModelState {
    data object Checking : ModelState

    /** This phone can't run Gemini Nano. Only cloud inference is possible. */
    data object Unsupported : ModelState

    /** Supported, but the model isn't on the phone yet. Nothing downloads until we ask. */
    data object Downloadable : ModelState

    data class Downloading(val downloadedBytes: Long, val totalBytes: Long?) : ModelState {
        val progress: Float? get() = totalBytes?.takeIf { it > 0 }?.let { downloadedBytes.toFloat() / it }
    }

    data class Ready(val modelName: String?) : ModelState

    data class Failed(val message: String) : ModelState
}

/**
 * Tracks the on-device model's lifecycle: check, download, warm up.
 */
class OnDeviceModelManager(private val onDevice: OnDeviceExtension?) {

    private val _state = MutableStateFlow<ModelState>(ModelState.Checking)
    val state: StateFlow<ModelState> = _state.asStateFlow()

    suspend fun refresh() {
        val extension = onDevice ?: return _state.emit(ModelState.Unsupported)
        val status = runCatching { extension.checkStatus() }.getOrElse { return _state.emit(ModelState.Unsupported) }
        when (status) {
            OnDeviceModelStatus.AVAILABLE -> {
                runCatching { extension.warmUp() }
                _state.emit(ModelState.Ready(runCatching { extension.getModelName() }.getOrNull()))
            }
            OnDeviceModelStatus.DOWNLOADABLE -> _state.emit(ModelState.Downloadable)
            OnDeviceModelStatus.DOWNLOADING -> download()
            else -> _state.emit(ModelState.Unsupported)
        }
    }

    suspend fun download() {
        val extension = onDevice ?: return
        var total: Long? = null
        _state.emit(ModelState.Downloading(0, null))
        extension.download().collect { status ->
            when (status) {
                is DownloadStatus.DownloadStarted -> {
                    total = status.bytesToDownload
                    _state.emit(ModelState.Downloading(0, total))
                }
                is DownloadStatus.DownloadInProgress ->
                    _state.emit(ModelState.Downloading(status.totalBytesDownloaded, total))
                is DownloadStatus.DownloadCompleted -> refresh()
                is DownloadStatus.DownloadFailed ->
                    _state.emit(ModelState.Failed(status.exception.message ?: "Download failed"))
            }
        }
    }
}
