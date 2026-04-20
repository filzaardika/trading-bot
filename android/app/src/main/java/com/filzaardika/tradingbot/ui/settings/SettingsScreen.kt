package com.filzaardika.tradingbot.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.SettingsDto
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.common.HairlineDivider
import com.filzaardika.tradingbot.ui.common.KvRow
import com.filzaardika.tradingbot.ui.common.LiveBadge
import com.filzaardika.tradingbot.ui.common.SectionHeader
import com.filzaardika.tradingbot.ui.theme.AccentAmber
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.TextMuted

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

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
            s?.let { LiveBadge(it.mode, it.testnet) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Read-only in MVP. Edit via server .env and restart the bot to apply.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(20.dp))

        error?.let {
            Card { Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(12.dp))
        }

        val d = s
        if (d != null) {
            SettingsGroup(title = "Trading Mode", icon = Icons.Filled.Tune, tint = AccentBlue) {
                KvRow("Mode", d.mode.uppercase(), valueColor = if (d.mode == "live") AccentRed else AccentGreen)
                HairlineDivider()
                KvRow("Testnet", if (d.testnet) "Yes" else "No", valueColor = if (d.testnet) AccentAmber else AccentGreen)
            }

            Spacer(Modifier.height(12.dp))

            SettingsGroup(title = "Scanner", icon = Icons.Filled.Speed, tint = AccentGreen) {
                KvRow("Universe size", d.universe_size.toString())
                HairlineDivider()
                KvRow("Cycle interval", "${d.cycle_seconds}s")
            }

            Spacer(Modifier.height(12.dp))

            SettingsGroup(title = "Risk Limits", icon = Icons.Filled.Policy, tint = AccentRed) {
                KvRow("Max risk / trade", "${d.max_risk_pct_per_trade}%")
                HairlineDivider()
                KvRow("Max concurrent positions", d.max_concurrent_positions.toString())
                HairlineDivider()
                KvRow("Max leverage", "${d.max_leverage}x")
                HairlineDivider()
                KvRow("Daily loss halt", "${d.daily_loss_halt_pct}%", valueColor = AccentAmber)
                HairlineDivider()
                KvRow("Max drawdown kill", "${d.max_drawdown_kill_pct}%", valueColor = AccentRed)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Unpair
        Card {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Unpair this device", style = MaterialTheme.typography.titleMedium)
                        Text("Removes saved host + token", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                }
                TextButton(onClick = onUnpair) { Text("Unpair", color = AccentRed) }
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    icon: ImageVector,
    tint: Color,
    content: @Composable () -> Unit
) {
    Card {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}
