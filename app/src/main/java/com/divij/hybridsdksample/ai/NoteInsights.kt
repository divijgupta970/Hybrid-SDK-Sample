package com.divij.hybridsdksample.ai

import com.google.firebase.ai.annotations.Generable
import com.google.firebase.ai.annotations.Guide
import kotlinx.serialization.Serializable

@Serializable
enum class Mood { CALM, HAPPY, GRATEFUL, EXCITED, ANXIOUS, STRESSED, SAD, FRUSTRATED }

// On-device and hybrid modes only accept KSP-generated schemas, so the model's answer is described with
// @Generable and @Guide instead of a hand-built JsonSchema. The enum lives inside a data class because a
// root-level enum schema is not supported on device.
@Serializable
@Generable(description = "Insights about a private personal note")
data class NoteInsights(
    @Guide(description = "One short sentence, under 15 words, that captures what the note is about")
    val summary: String,
    @Guide(description = "The overall mood of the person who wrote the note")
    val mood: Mood,
    @Guide(description = "Short lowercase topic tags of one or two words each", minItems = 1, maxItems = 3)
    val tags: List<String>,
) {
    // KSP adds NoteInsights.firebaseAISchema() to this companion object, so it has to exist even if empty.
    companion object
}
