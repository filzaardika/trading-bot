package com.filzaardika.tradingbot.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.DashboardDto
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.common.LiveBadge
import com.filzaardika.tradingbot.ui.common.Section
import com.filzaardika.tradingbot.ui.common.StatusDot
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.LiveRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(repo: BotRepo, onUnpair: () -> Unit) {
    var dash by remember { mutableStateOf<DashboardDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var showKillConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            when (val r = repo.dashboard()) {
                is ApiResult.Ok -> { dash = r.value; error = null }
                is ApiResult.Err -> error = r.message
            }
            delay(2000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dashboard", style = MaterialTheme.typography.headlineLarge)
            dash?.let { LiveBadge(it.mode, it.testnet) }
        }
        Spacer(Modifier.height(12.dp))

        error?.let {
            Card {
                Text("Connection error: $it", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(12.dp))
        }

        val d = dash
        if (d != null) {
            Card {
                Column {
                    Text("Equity", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "$" + "%,.2f".format(d.equity),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    val pnlColor = if (d.pnl_today >= 0) AccentGreen else AccentRed
                    val sign = if (d.pnl_today >= 0) "+" else ""
                    Text(
                        "${sign}${"%,.2f".format(d.pnl_today)} (${sign}${"%.2f".format(d.pnl_today_pct)}%) today",
                        color = pnlColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f)) {
                    Column {
                        Text("Open positions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${d.open_positions_count}", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Card(Modifier.weight(1f)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(d.bot_status)
                            Spacer(Modifier.width(8.dp))
                            Text(d.bot_status.uppercase(), style = MaterialTheme.typography.labelLarge)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "next cycle in ${d.seconds_to_next_cycle}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Card {
                Column {
                    Text("Cycle progress", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { d.cycle_progress.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // KILL SWITCH — huge, thumb-reachable, double-confirm
            Button(
                onClick = { showKillConfirm = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiveRed,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Text(
                    if (d.kill_switch) "KILL SWITCH ACTIVE — TAP TO RESET" else "EMERGENCY KILL SWITCH",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        } else if (error == null) {
            Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onUnpair) { Text("Unpair bot") }
    }

    if (showKillConfirm) {
        val d = dash
        val isActive = d?.kill_switch == true
        AlertDialog(
            onDismissRequest = { showKillConfirm = false },
            title = { Text(if (isActive) "Reset kill switch?" else "Activate KILL SWITCH?") },
            text = {
                Text(
                    if (isActive) "This will re-enable trading. Continue?"
                    else "This halts ALL new trading immediately. Existing positions stay open — use Flatten in Control tab to close them. Continue?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showKillConfirm = false
                    scope.launch {
                        if (isActive) repo.killReset() else repo.kill()
                    }
                }) { Text(if (isActive) "RESET" else "ACTIVATE", color = LiveRed) }
            },
            dismissButton = {
                TextButton(onClick = { showKillConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
