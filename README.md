# Private Notes: hybrid on-device and cloud AI on Android

A small Jetpack Compose notes app that analyses each note with **Gemini Nano on the phone** when it can, and **Gemini in
the cloud** when it can't, using the [Firebase AI Logic hybrid inference SDK](https://firebase.google.com/docs/ai-logic/hybrid/android/get-started?api=dev).

> **Experimental API.** Hybrid inference in Firebase AI Logic is experimental, and the ML Kit Prompt API it uses is in
> beta. APIs may change between releases.

## What it shows

- **One `GenerativeModel`, two paths.** The same request runs on Gemini Nano or in the cloud depending on the inference mode.
- **A privacy setting that picks the inference mode.** "Keep my notes on this device" switches between `ONLY_ON_DEVICE`
  and `PREFER_ON_DEVICE`.
- **The on-device model lifecycle:** checking its status, downloading it with progress, and warming it up.
- **Structured output** with `@Generable` and `@Guide`. **Tag** returns a typed `NoteInsights` (summary, mood enum, tags).
- **Streaming** with `generateContentStream`. **Summarise** streams a one-line takeaway.
- **Where each answer came from.** Every result shows an "On device" or "Cloud" chip, read from `inferenceSource`.
- **Retrying when AICore is busy,** with exponential backoff.
- **App Check split by build type:** the debug provider in debug builds, Play Integrity in release builds.

## Requirements

- A recent Android Studio that supports **AGP 9.5** (the project uses `9.5.0-alpha01`) and Gradle 9.6.
- JDK 17 or newer.
- A Firebase project (the free Gemini Developer API tier is enough).
- **Min SDK 26.** `firebase-ai-ondevice` needs it.
- To see on-device inference, a phone that
  [supports Gemini Nano](https://developers.google.com/ml-kit/genai#prompt-device). Emulators don't have it, so on an
  emulator every request goes to the cloud.

## Setup

1. **Create the Firebase backend.**
   - In the [Firebase console](https://console.firebase.google.com/), open your project and go to
     **AI Services > AI Logic**.
   - Click **Get started** and choose the **Gemini Developer API**.
   - This setup turns on App Check enforcement for AI Logic.
2. **Register the Android app.** Add an Android app with the package name `com.divij.hybridsdksample`, download
   `google-services.json` and put it in `app/`. The file is gitignored, so each developer brings their own.
3. **Register an App Check debug token.**
   - Run the debug build once.
   - In Logcat, find the `DebugAppCheckProvider` line and copy the debug secret.
   - Add it in the console under **Security > App Check > Apps > Manage debug tokens**.
   - Until you do, every cloud request fails attestation.
4. **(Release builds)** Register the app with **Play Integrity** in App Check. The release build type is signed with the
   debug key so you can install it locally and check R8. Use your own signing config before you ship anything.

## Try it

1. Launch the app. The banner at the top tells you whether this phone can run Gemini Nano.
   - On a supported phone, tap **Download** and wait for the progress bar to finish.
2. Open a note and tap **Tag**. You get a summary, a mood and up to three tags.
   - The chip says **On device** or **Cloud**.
3. Tap **Summarise** to stream a one-sentence takeaway.
4. Tap the shield icon and turn on **Keep my notes on this device**.
   - On a phone without Gemini Nano, the app now explains why it can't run.
   - It sends nothing to the cloud.

## Project structure

```
app/src/main/java/com/divij/hybridsdksample/
├── ai/
│   ├── NoteAi.kt                 GenerativeModel per privacy mode, tag(), summarise(), BUSY retry
│   ├── NoteInsights.kt           @Generable schema for structured output
│   └── OnDeviceModelManager.kt   checkStatus / download / warmUp as a StateFlow
├── notes/                        In-memory notes and the privacy setting
├── ui/                           Compose screens, ViewModel, model banner, source chip
├── AppContainer.kt               Hand-rolled dependencies
├── NotesApp.kt                   Firebase and App Check initialisation
└── MainActivity.kt
app/src/debug/.../AppCheckInstaller.kt     Debug App Check provider
app/src/release/.../AppCheckInstaller.kt   Play Integrity App Check provider
```

## Key versions

| Library | Version |
| --- | --- |
| `com.google.firebase:firebase-ai` | 18.0.0 |
| `com.google.firebase:firebase-ai-ondevice` (not in the Firebase BoM) | 16.0.0-beta06 |
| `com.google.firebase:firebase-ai-ksp-processor` | 17.0.0 |
| `com.google.firebase:firebase-appcheck-debug` / `-playintegrity` | 20.0.0 |
| Kotlin / KSP | 2.3.21 / 2.3.12 |
| Cloud model | `gemini-3.5-flash-lite` |

## Things to know

- **The SDK never downloads the on-device model for you.** Without it, `PREFER_ON_DEVICE` falls back to the cloud and
  `ONLY_ON_DEVICE` throws.
- **On-device limits:**
  - single-turn text (plus at most one image);
  - about 4000 tokens per request;
  - foreground only;
  - English and Korean validated so far.
- **Monitoring:** Firebase AI monitoring only shows cloud requests.
- **App Check enforcement** becomes mandatory for Firebase AI Logic on 2 November 2026.

## Learn more

- [Hybrid inference on Android: get started](https://firebase.google.com/docs/ai-logic/hybrid/android/get-started?api=dev)
- [Configuration options: inference modes, model selection, model config](https://firebase.google.com/docs/ai-logic/hybrid/android/configuration-options)
- [Structured output for hybrid experiences](https://firebase.google.com/docs/ai-logic/hybrid/android/generate-structured-output)
- [ML Kit Prompt API](https://developers.google.com/ml-kit/genai/prompt/android)
- [App Check for Firebase AI Logic](https://firebase.google.com/docs/ai-logic/app-check)
