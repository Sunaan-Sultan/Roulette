package com.project.roulette.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.project.roulette.util.ChangelogEntry
import com.project.roulette.ui.theme.RouletteTheme

@Composable
fun WhatsNewDialog(
    entry: ChangelogEntry,
    themeColor: Color,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(28.dp)),
            color = RouletteTheme.colors.surfaceElevated
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(themeColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(AppIcons.NewReleases, contentDescription = null, tint = themeColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("What's new", color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Version ${entry.versionName}", color = RouletteTheme.colors.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    entry.highlights.forEach { highlight ->
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                                    .background(themeColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(AppIcons.Check, contentDescription = null, tint = themeColor, modifier = Modifier.size(12.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(highlight, color = RouletteTheme.colors.textPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor, contentColor = RouletteTheme.colors.onPrimary)
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
