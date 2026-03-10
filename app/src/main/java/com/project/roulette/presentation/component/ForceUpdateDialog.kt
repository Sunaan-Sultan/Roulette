package com.project.roulette.presentation.component

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

@Composable
fun ForceUpdateDialog() {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { /* non-dismissible */ },
        title = { Text("Update Required") },
        text = { Text("A new version of the app is available. Please update to continue using the app.") },
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()
                )
                context.startActivity(intent)
            }) {
                Text("Update Now")
            }
        }
    )
}