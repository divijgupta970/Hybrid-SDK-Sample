package com.divij.hybridsdksample

import android.content.Context
import com.divij.hybridsdksample.ai.NoteAi
import com.divij.hybridsdksample.ai.OnDeviceModelManager
import com.divij.hybridsdksample.ai.PrivacyMode
import com.divij.hybridsdksample.notes.NotesRepository
import com.divij.hybridsdksample.notes.PrivacySettings

class AppContainer(context: Context) {
    val notes = NotesRepository()
    val privacy = PrivacySettings(context)
    val noteAi = NoteAi()

    val modelManager = OnDeviceModelManager(noteAi.model(PrivacyMode.DEVICE_FIRST).onDeviceExtension)
}
