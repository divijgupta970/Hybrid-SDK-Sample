package com.divij.hybridsdksample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.divij.hybridsdksample.R
import com.divij.hybridsdksample.ai.ModelState
import com.divij.hybridsdksample.ai.PrivacyMode

@Composable
fun ModelStatusBanner(
    model: ModelState,
    privacy: PrivacyMode,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deviceOnly = privacy == PrivacyMode.DEVICE_ONLY
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    painter = painterResource(if (model is ModelState.Ready) R.drawable.ic_smartphone else R.drawable.ic_sparkle),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = when (model) {
                        ModelState.Checking -> "Checking for Gemini Nano…"
                        ModelState.Unsupported ->
                            if (deviceOnly) "This phone can't run Gemini Nano, so AI is off while notes stay on this device."
                            else "This phone can't run Gemini Nano. Notes are analysed by Gemini in the cloud."
                        ModelState.Downloadable -> "Download Gemini Nano to analyse notes without them leaving your phone."
                        is ModelState.Downloading -> "Downloading Gemini Nano…"
                        is ModelState.Ready -> "Gemini Nano is ready" + (model.modelName?.let { " ($it)" } ?: "") + ". Notes are analysed on your phone."
                        is ModelState.Failed -> "Gemini Nano couldn't be downloaded: ${model.message}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
            when (model) {
                ModelState.Checking -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is ModelState.Downloading -> {
                    val progress = model.progress
                    if (progress == null) LinearProgressIndicator(Modifier.fillMaxWidth())
                    else LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
                ModelState.Downloadable, is ModelState.Failed ->
                    FilledTonalButton(onClick = onDownload, modifier = Modifier.align(Alignment.End)) {
                        Text(if (model is ModelState.Failed) "Try again" else "Download")
                    }
                else -> Unit
            }
        }
    }
}
