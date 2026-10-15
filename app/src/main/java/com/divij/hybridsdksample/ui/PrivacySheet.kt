package com.divij.hybridsdksample.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.divij.hybridsdksample.R
import com.divij.hybridsdksample.ai.PrivacyMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySheet(privacy: PrivacyMode, onDeviceOnlyChange: (Boolean) -> Unit, onDismiss: () -> Unit) {
    val deviceOnly = privacy == PrivacyMode.DEVICE_ONLY
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Privacy", style = MaterialTheme.typography.titleLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.toggleable(value = deviceOnly, role = Role.Switch, onValueChange = onDeviceOnlyChange),
            ) {
                Icon(painterResource(R.drawable.ic_lock), contentDescription = null, modifier = Modifier.size(24.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Keep my notes on this device", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (deviceOnly) "Only Gemini Nano on this phone reads your notes. On phones without it, AI features are off."
                        else "Gemini Nano reads your notes when this phone has it. Otherwise they're sent to Gemini in the cloud.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = deviceOnly, onCheckedChange = null)
            }
        }
    }
}
