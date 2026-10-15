package com.divij.hybridsdksample.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

/** Notes live in memory to keep the sample about AI, not storage. */
class NotesRepository {

    private val _notes = MutableStateFlow(seedNotes())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    fun note(id: Long): Note? = _notes.value.firstOrNull { it.id == id }

    fun add(title: String, body: String): Note {
        val note = Note(
            id = (_notes.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title.ifBlank { body.lineSequence().first().take(40) },
            body = body,
            date = LocalDate.now(),
        )
        _notes.update { listOf(note) + it }
        return note
    }
}

private fun seedNotes(): List<Note> {
    val today = LocalDate.now()
    return listOf(
        Note(
            1, "Before the big review", "Performance review is on Thursday and I keep replaying every mistake from " +
                "the last quarter. I know I shipped the payments migration and mentored two juniors, but my brain only " +
                "wants to talk about the outage in March. Going to write down three wins tonight so I walk in with them.",
            today,
        ),
        Note(
            2, "Sunday with Mum", "Took Mum to the botanical garden. She remembered the names of half the plants, " +
                "which surprised both of us. We had chai at the little stall by the lake and she told me stories about " +
                "her first job. I want more days like this, slow and unplanned.",
            today.minusDays(1),
        ),
        Note(
            3, "Running again", "First 5k in eight months. Slow, but I didn't stop once. Knees are fine. Signed up " +
                "for the October 10k so I have a reason to keep going.",
            today.minusDays(2),
        ),
        Note(
            4, "Flat hunting", "Third viewing this week and still nothing within budget near the office. The one in " +
                "Indiranagar was perfect except for the rent. I'm tired of refreshing listings at midnight.",
            today.minusDays(3),
        ),
        Note(
            5, "Ideas for the side project", "A habit tracker that only asks one question a day. No streaks, no " +
                "guilt. Maybe a widget that changes colour with the week. Need to sketch the onboarding before I " +
                "write any code this time.",
            today.minusDays(5),
        ),
        Note(
            6, "Missed the call", "Forgot Arjun's birthday call again. He said it's fine but it isn't really. " +
                "Setting a yearly reminder now and sending him a proper letter this weekend.",
            today.minusDays(6),
        ),
        Note(
            7, "Rainy evening", "Power cut for two hours. Lit candles, read the last chapters of the book I started " +
                "in June, listened to the rain. Didn't miss my phone at all.",
            today.minusDays(8),
        ),
        Note(
            8, "Budget check", "Spent way more on food delivery than I thought this month. Cooking on weekdays from " +
                "now on, and keeping one delivery night as a treat.",
            today.minusDays(10),
        ),
    )
}
