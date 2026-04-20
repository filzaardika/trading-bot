package com.filzaardika.tradingbot.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.common.SectionHeader
import com.filzaardika.tradingbot.ui.theme.AccentAmber
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.BorderStrong
import com.filzaardika.tradingbot.ui.theme.KillGradBottom
import com.filzaardika.tradingbot.ui.theme.KillGradTop
import com.filzaardika.tradingbot.ui.theme.LiveRed
import com.filzaardika.tradingbot.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun ControlScreen(repo: BotRepo) {
    val scope = rememberCoroutineScope()
    var toast by remember { mutableStateOf<String?>(null) }
    var confirmFlatten by remember { mutableStateOf(false) }

    fun call(name: String, block: suspend () -> ApiResult<*>) {
        scope.launch {
            toast = when (val r = block()) {
                is ApiResult.Ok -> "$name — ok"
                is ApiResult.Err -> "$name — ${r.message}"
            }
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
        Text("Bot Control", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            "Manage the trading bot's lifecycle. Emergency actions require confirmation.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(20.dp))

        SectionHeader("Lifecycle")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile(
                label = "Pause",
                description = "Halt cycles",
                icon = Icons.Filled.Pause,
                tint = AccentAmber,
                modifier = Modifier.weight(1f),
                onClick = { call("pause") { repo.pause() } }
            )
            ActionTile(
                label = "Resume",
                description = "Resume trading",
                icon = Icons.Filled.PlayArrow,
                tint = AccentGreen,
                modifier = Modifier.weight(1f),
                onClick = { call("resume") { repo.resume() } }
            )
        }
        Spacer(Modifier.height(12.dp))
        ActionTile(
            label = "Force cycle now",
            description = "Run screening + brain decision immediately",
            icon = Icons.Filled.FastForward,
            tint = AccentBlue,
            modifier = Modifier.fillMaxWidth(),
            onClick = { call("cycle-now") { repo.cycleNow() } }
        )

        Spacer(Modifier.height(24.dp))

        SectionHeader("Emergency")
        EmergencyFlatten { confirmFlatten = true }

        toast?.let {
            Spacer(Modifier.height(16.dp))
            Card {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(RoundedCornerShape(999.dp)).background(AccentGreen))
                    Spacer(Modifier.width(10.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }

    if (confirmFlatten) {
        AlertDialog(
            onDismissRequest = { confirmFlatten = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = LiveRed) },
            title = { Text("Flatten ALL positions?") },
            text = { Text("This will market-close every open position immediately. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmFlatten = false
                    call("flatten") { repo.flatten() }
                }) { Text("FLATTEN ALL", color = LiveRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmFlatten = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ActionTile(
    label: String,
    description: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, BorderStrong, RoundedCornerShape(20.dp))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(description, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun EmergencyFlatten(onTap: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(KillGradTop.copy(alpha = 0.2f), KillGradBottom.copy(alpha = 0.2f))))
            .border(1.dp, LiveRed.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = LiveRed, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "FLATTEN ALL POSITIONS",
                    style = MaterialTheme.typography.titleMedium,
                    color = LiveRed,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Closes every open position at market on the next tick. Use in emergencies only — P&L locks immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onTap,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiveRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("INITIATE FLATTEN", fontWeight = FontWeight.Bold)
            }
        }
    }
}
