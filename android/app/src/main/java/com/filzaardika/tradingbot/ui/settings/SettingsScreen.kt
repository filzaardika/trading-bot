package com.filzaardika.tradingbot.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.SettingsDto
import com.filzaardika.tradingbot.ui.common.Card

@Composable
fun SettingsScreen(repo: BotRepo, onUnpair: () -> Unit) {
    var s by remember { mutableStateOf<SettingsDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        when (val r = repo.settings()) {
            is ApiResult.Ok -> s = r.value
            is ApiResult.Err -> error = r.message
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            "Read-only in MVP. Edit via server .env for now.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        error?.let { Card { Text("Error: $it", color = MaterialTheme.colorScheme.error) }; Spacer(Modifier.height(12.dp)) }
        s?.let { d ->
            Card {
                Column {
                    KV("Mode", d.mode.uppercase())
                    KV("Testnet", d.testnet.toString())
                    KV("Universe size", d.universe_size.toString())
                    KV("Cycle seconds", d.cycle_seconds.toString())
                    KV("Max risk % per trade", "${d.max_risk_pct_per_trade}%")
                    KV("Max concurrent positions", d.max_concurrent_positions.toString())
                    KV("Max leverage", "${d.max_leverage}x")
                    KV("Daily loss halt %", "${d.daily_loss_halt_pct}%")
                    KV("Max drawdown kill %", "${d.max_drawdown_kill_pct}%")
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onUnpair) { Text("Unpair bot") }
    }
}

@Composable
private fun KV(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
