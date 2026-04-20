package com.filzaardika.tradingbot.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.theme.LiveRed
import kotlinx.coroutines.launch

@Composable
fun ControlScreen(repo: BotRepo) {
    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<String?>(null) }
    var confirmFlatten by remember { mutableStateOf(false) }

    fun call(name: String, block: suspend () -> ApiResult<*>) {
        scope.launch {
            toast = when (val r = block()) {
                is ApiResult.Ok -> "$name: ok"
                is ApiResult.Err -> "$name: ${r.message}"
            }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Bot Control", style = MaterialTheme.typography.headlineLarge)

        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lifecycle", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { call("pause") { repo.pause() } }, modifier = Modifier.fillMaxWidth()) { Text("Pause") }
                OutlinedButton(onClick = { call("resume") { repo.resume() } }, modifier = Modifier.fillMaxWidth()) { Text("Resume") }
                OutlinedButton(onClick = { call("cycle-now") { repo.cycleNow() } }, modifier = Modifier.fillMaxWidth()) { Text("Force cycle now") }
            }
        }

        Card {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Emergency", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { confirmFlatten = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRed, contentColor = MaterialTheme.colorScheme.onError),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("FLATTEN ALL POSITIONS") }
                Text(
                    "Closes every open position at market on the next tick. Use in emergencies only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        toast?.let {
            Card { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }

    if (confirmFlatten) {
        AlertDialog(
            onDismissRequest = { confirmFlatten = false },
            title = { Text("Flatten ALL positions?") },
            text = { Text("This will market-close every open position. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmFlatten = false
                    call("flatten") { repo.flatten() }
                }) { Text("FLATTEN", color = LiveRed) }
            },
            dismissButton = { TextButton(onClick = { confirmFlatten = false }) { Text("Cancel") } }
        )
    }
}
