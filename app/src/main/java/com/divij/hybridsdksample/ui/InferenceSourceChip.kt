package com.divij.hybridsdksample.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.divij.hybridsdksample.R
import com.google.firebase.ai.InferenceSource

/** Tells the user, honestly, where their note was read. */
@Composable
fun InferenceSourceChip(source: InferenceSource, modifier: Modifier = Modifier) {
    val onDevice = source == InferenceSource.ON_DEVICE
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (onDevice) colors.primaryContainer else colors.tertiaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Icon(
            painter = painterResource(if (onDevice) R.drawable.ic_smartphone else R.drawable.ic_cloud),
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = if (onDevice) colors.onPrimaryContainer else colors.onTertiaryContainer,
        )
        Text(
            text = if (onDevice) "On device" else "Cloud",
            style = MaterialTheme.typography.labelMedium,
            color = if (onDevice) colors.onPrimaryContainer else colors.onTertiaryContainer,
        )
    }
}
