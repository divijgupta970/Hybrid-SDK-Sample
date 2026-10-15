package com.divij.hybridsdksample.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.GenerativeBackend
import com.google.mlkit.genai.common.GenAiException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan

/** Where the user lets their notes go. This is the only thing that decides the inference mode. */
enum class PrivacyMode(val inferenceMode: InferenceMode) {
    // Never leaves the phone. On devices without Gemini Nano, requests fail instead of quietly using the cloud.
    DEVICE_ONLY(InferenceMode.ONLY_ON_DEVICE),

    // Gemini Nano when the phone has it, Gemini in the cloud otherwise.
    DEVICE_FIRST(InferenceMode.PREFER_ON_DEVICE),
}

data class Tagged(val insights: NoteInsights, val source: InferenceSource)

data class SummarySoFar(val text: String, val source: InferenceSource?)

class NoteAi {

    // A config lives as long as its GenerativeModel, so each privacy mode gets its own instance.
    private val models = PrivacyMode.entries.associateWith { buildModel(it.inferenceMode) }

    fun model(mode: PrivacyMode): GenerativeModel = models.getValue(mode)

    private fun buildModel(mode: InferenceMode): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                // Required for every mode except ONLY_ON_DEVICE: there is no default cloud model.
                modelName = CLOUD_MODEL,
                onDeviceConfig = OnDeviceConfig(
                    mode = mode,
                    // Gemini Nano is happiest with short answers; both of our tasks fit easily.
                    maxOutputTokens = 256,
                ),
            )

    suspend fun tag(note: String, mode: PrivacyMode): Tagged = retryWhenBusy {
        val response = model(mode).generateObject(
            NoteInsights.firebaseAISchema(),
            """
            Read this private note and describe it.

            Note:
            ${note.forPrompt()}
            """.trimIndent(),
        )
        val insights = response.getObject() ?: throw IllegalStateException("The model returned no insights")
        Tagged(insights, response.response.inferenceSource)
    }

    fun summarise(note: String, mode: PrivacyMode): Flow<SummarySoFar> =
        model(mode)
            .generateContentStream(
                """
                In one sentence of at most 20 words, tell the writer the key takeaway of this note.
                Speak to them as "you". Plain text only.

                Note:
                ${note.forPrompt()}
                """.trimIndent(),
            )
            .map { chunk -> chunk.text.orEmpty() to chunk.inferenceSource }
            .scan(SummarySoFar("", null)) { soFar, (text, source) -> SummarySoFar(soFar.text + text, source) }

    // AICore rate-limits each app. A BUSY error means "try again shortly", so back off instead of failing.
    private suspend fun <T> retryWhenBusy(block: suspend () -> T): T {
        var wait = 500L
        repeat(MAX_RETRIES) {
            try {
                return block()
            } catch (e: FirebaseAIException) {
                val genAi = e.findCause<GenAiException>()
                if (genAi?.errorCode != GenAiException.ErrorCode.BUSY) throw e
                delay(genAi.retryDelay.toMillis().takeIf { it > 0 } ?: wait)
                wait *= 2
            }
        }
        return block()
    }

    private companion object {
        const val CLOUD_MODEL = "gemini-3.5-flash-lite"
        const val MAX_RETRIES = 2
    }
}

// On-device requests are capped at about 4000 tokens in total, so very long notes are trimmed.
private const val MAX_NOTE_CHARS = 6_000

private fun String.forPrompt(): String = "\"\"\"\n${take(MAX_NOTE_CHARS).trim()}\n\"\"\""

private inline fun <reified T : Throwable> Throwable.findCause(): T? =
    generateSequence(this) { it.cause }.filterIsInstance<T>().firstOrNull()
