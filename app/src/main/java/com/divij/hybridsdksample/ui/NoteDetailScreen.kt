package com.divij.hybridsdksample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.divij.hybridsdksample.R
import com.divij.hybridsdksample.ai.Mood
import com.divij.hybridsdksample.notes.Note
import com.google.firebase.ai.InferenceSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note,
    insights: InsightsUi,
    onBack: () -> Unit,
    onTag: () -> Unit,
    onSummarise: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        ) {
            Text(note.title, style = MaterialTheme.typography.headlineSmall)
            Text(note.formattedDate(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(note.body, style = MaterialTheme.typography.bodyLarge)
            InsightsCard(insights, onTag, onSummarise)
        }
    }
}

@Composable
private fun InsightsCard(insights: InsightsUi, onTag: () -> Unit, onSummarise: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onTag, enabled = insights.tag != TagUi.Loading) {
                    Icon(painterResource(R.drawable.ic_sparkle), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tag")
                }
                OutlinedButton(onClick = onSummarise, enabled = insights.summary !is SummaryUi.Streaming) {
                    Icon(painterResource(R.drawable.ic_short_text), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Summarise")
                }
            }
            TagResult(insights.tag)
            SummaryResult(insights.summary)
        }
    }
}

@Composable
private fun TagResult(tag: TagUi) {
    when (tag) {
        TagUi.Idle -> Unit
        TagUi.Loading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Text("Reading your note…", style = MaterialTheme.typography.bodyMedium)
        }
        is TagUi.Error -> ErrorText(tag.message)
        is TagUi.Done -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider()
            ResultHeader("Insights", tag.result.source)
            Text(tag.result.insights.summary, style = MaterialTheme.typography.bodyLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SuggestionChip(onClick = {}, label = { Text(tag.result.insights.mood.label()) })
                tag.result.insights.tags.forEach { SuggestionChip(onClick = {}, label = { Text("#$it") }) }
            }
        }
    }
}

@Composable
private fun SummaryResult(summary: SummaryUi) {
    val (text, source) = when (summary) {
        SummaryUi.Idle -> return
        is SummaryUi.Error -> return ErrorText(summary.message)
        is SummaryUi.Streaming -> summary.soFar.text to summary.soFar.source
        is SummaryUi.Done -> summary.summary.text to summary.summary.source
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        ResultHeader("Takeaway", source)
        Text(text.ifEmpty { "…" }, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ResultHeader(title: String, source: InferenceSource?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (source != null) InferenceSourceChip(source)
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
}

private fun Mood.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }
