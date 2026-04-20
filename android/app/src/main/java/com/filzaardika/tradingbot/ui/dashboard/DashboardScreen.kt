package com.filzaardika.tradingbot.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.DashboardDto
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.common.GradientCard
import com.filzaardika.tradingbot.ui.common.KvRow
import com.filzaardika.tradingbot.ui.common.MetricTile
import com.filzaardika.tradingbot.ui.common.PnlChip
import com.filzaardika.tradingbot.ui.common.SectionHeader
import com.filzaardika.tradingbot.ui.common.StatusDot
import com.filzaardika.tradingbot.ui.theme.AccentAmber
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.HeroGradBottom
import com.filzaardika.tradingbot.ui.theme.HeroGradMid
import com.filzaardika.tradingbot.ui.theme.HeroGradTop
import com.filzaardika.tradingbot.ui.theme.KillGradBottom
import com.filzaardika.tradingbot.ui.theme.KillGradTop
import com.filzaardika.tradingbot.ui.theme.LiveRed
import com.filzaardika.tradingbot.ui.theme.NumericDisplay
import com.filzaardika.tradingbot.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(repo: BotRepo) {
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
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        error?.let {
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(AccentRed, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Connection error: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        val d = dash
        if (d != null) {
            HeroEquityCard(d)
            Spacer(Modifier.height(14.dp))

            MetricsRow(d)
            Spacer(Modifier.height(14.dp))

            CycleCard(d)
            Spacer(Modifier.height(22.dp))

            KillSwitchButton(active = d.kill_switch, onTap = { showKillConfirm = true })
        } else if (error == null) {
            SkeletonHero()
        }
    }

    if (showKillConfirm) {
        val d = dash
        val isActive = d?.kill_switch == true
        AlertDialog(
            onDismissRequest = { showKillConfirm = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
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

@Composable
private fun HeroEquityCard(d: DashboardDto) {
    GradientCard(gradient = listOf(HeroGradTop, HeroGradMid, HeroGradBottom)) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ACCOUNT EQUITY",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$" + "%,.2f".format(d.equity),
                        style = NumericDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    PnlChip(amount = d.pnl_today, pct = d.pnl_today_pct)
                }
                if (d.starting_equity > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Start",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$" + "%,.2f".format(d.starting_equity),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricsRow(d: DashboardDto) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile(
            label = "Open Positions",
            value = "${d.open_positions_count}",
            leadingIcon = {
                Icon(Icons.Filled.Timeline, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
            },
            modifier = Modifier.weight(1f)
        )
        val statusColor = when (d.bot_status) {
            "running" -> AccentGreen
            "paused" -> AccentAmber
            "error" -> AccentRed
            else -> TextMuted
        }
        MetricTile(
            label = "Bot Status",
            value = d.bot_status.replaceFirstChar { it.uppercase() },
            sub = if (d.kill_switch) "kill switch ON" else null,
            valueColor = statusColor,
            leadingIcon = { StatusDot(d.bot_status, size = 8.dp) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CycleCard(d: DashboardDto) {
    SectionHeader(title = "Cycle")
    Card {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Speed, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Next cycle", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    "${d.seconds_to_next_cycle}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { d.cycle_progress.toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = AccentGreen,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
            Spacer(Modifier.height(8.dp))
            if (d.last_cycle_ts > 0) {
                val timeStr = remember(d.last_cycle_ts) {
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(d.last_cycle_ts * 1000))
                }
                KvRow(label = "Last cycle", value = timeStr)
            }
            if (d.last_cycle_error.isNotBlank()) {
                KvRow(label = "Last error", value = d.last_cycle_error, valueColor = AccentRed)
            }
        }
    }
}

@Composable
private fun KillSwitchButton(active: Boolean, onTap: () -> Unit) {
    SectionHeader(title = "Emergency")
    Box(
        Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (active) Brush.verticalGradient(listOf(AccentAmber, Color(0xFFB45309)))
                else Brush.verticalGradient(listOf(KillGradTop, KillGradBottom))
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
    ) {
        Button(
            onClick = onTap,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        if (active) "KILL SWITCH ACTIVE" else "EMERGENCY KILL SWITCH",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (active) "Tap to re-enable trading" else "Tap to halt all trading",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkeletonHero() {
    Card(padding = 24.dp) {
        Column {
            Text("Loading…", style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Spacer(Modifier.height(8.dp))
            Text("— — —", style = NumericDisplay, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
