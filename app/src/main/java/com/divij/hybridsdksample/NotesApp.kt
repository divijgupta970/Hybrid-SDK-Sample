package com.divij.hybridsdksample

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize

class NotesApp : Application() {

    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
        // Debug and release builds each provide their own installAppCheck(), see src/debug and src/release.
        installAppCheck()
    }
}
