package com.divij.hybridsdksample.notes

import java.time.LocalDate

data class Note(
    val id: Long,
    val title: String,
    val body: String,
    val date: LocalDate,
)
