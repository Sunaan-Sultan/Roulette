package com.project.roulette.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.roulette.ui.theme.RouletteTheme
import androidx.compose.material3.MaterialTheme

@Composable
fun HistoryItem(name: String, color: Color, isLatest: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = RouletteTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isLatest) color.copy(alpha = 0.3f) else RouletteTheme.colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                Spacer(Modifier.width(12.dp))
                Text(name, color = RouletteTheme.colors.textPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
            }
            if (isLatest) {
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "LATEST",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
