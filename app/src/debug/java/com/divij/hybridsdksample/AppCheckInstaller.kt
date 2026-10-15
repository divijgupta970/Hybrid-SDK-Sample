package com.divij.hybridsdksample

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds use the App Check debug provider. On first launch it logs a debug secret to Logcat
 * (tag DebugAppCheckProvider). Register it in the Firebase console under App Check > Apps > Manage debug tokens.
 */
fun installAppCheck() {
    Firebase.appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
}
