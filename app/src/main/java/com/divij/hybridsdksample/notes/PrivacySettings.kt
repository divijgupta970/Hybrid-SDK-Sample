package com.divij.hybridsdksample.notes

import android.content.Context
import androidx.core.content.edit
import com.divij.hybridsdksample.ai.PrivacyMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PrivacySettings(context: Context) {

    private val prefs = context.getSharedPreferences("privacy", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(
        if (prefs.getBoolean(KEY_DEVICE_ONLY, false)) PrivacyMode.DEVICE_ONLY else PrivacyMode.DEVICE_FIRST,
    )
    val mode: StateFlow<PrivacyMode> = _mode.asStateFlow()

    fun setDeviceOnly(deviceOnly: Boolean) {
        prefs.edit { putBoolean(KEY_DEVICE_ONLY, deviceOnly) }
        _mode.value = if (deviceOnly) PrivacyMode.DEVICE_ONLY else PrivacyMode.DEVICE_FIRST
    }

    private companion object {
        const val KEY_DEVICE_ONLY = "device_only"
    }
}
