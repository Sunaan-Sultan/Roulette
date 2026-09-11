package com.project.roulette.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.project.roulette.ui.theme.RouletteTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable, styled alert dialog for showing errors and confirmations.
 * - title: short heading
 * - message: body text
 * - confirmText: primary button text
 * - onConfirm: primary action
 * - onDismiss: close/cancel action
 *
 * Designed to be visually clear and modern using Material3 primitives.
 */
@Composable
fun AlertDialogBox(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "OK",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // subtle error icon
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small),
                    color = RouletteTheme.colors.warningSubtle
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = AppIcons.Warning,
                            contentDescription = "Alert",
                            tint = RouletteTheme.colors.warning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}
