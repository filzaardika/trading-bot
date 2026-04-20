package com.filzaardika.tradingbot.ui.positions

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.PositionDto
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.common.PnlChip
import com.filzaardika.tradingbot.ui.common.SectionHeader
import com.filzaardika.tradingbot.ui.common.StatusPill
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.BorderStrong
import com.filzaardika.tradingbot.ui.theme.NumericSmall
import com.filzaardika.tradingbot.ui.theme.TextMuted
import kotlinx.coroutines.delay

@Composable
fun PositionsScreen(repo: BotRepo) {
    var items by remember { mutableStateOf<List<PositionDto>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            when (val r = repo.positions()) {
                is ApiResult.Ok -> { items = r.value; error = null }
                is ApiResult.Err -> error = r.message
            }
            delay(2000)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 14.dp, bottom = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Positions", style = MaterialTheme.typography.headlineLarge)
            StatusPill(
                text = "${items.size} OPEN",
                color = if (items.isEmpty()) TextMuted else AccentBlue
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Live view of open futures positions. Swipe on a card for actions (coming soon).",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(Modifier.height(16.dp))

        error?.let {
            Card { Text("Error: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(12.dp))
        }

        if (items.isEmpty() && error == null) {
            EmptyState()
        } else if (items.isNotEmpty()) {
            SectionHeader(title = "Active Positions")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.id }) { p -> PositionCard(p) }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(padding = 28.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("📊", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text("No open positions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "The bot is scanning the market. New trades will appear here in real time.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun PositionCard(p: PositionDto) {
    val long = p.side == "long"
    val sideColor = if (long) AccentGreen else AccentRed
    Card {
        Column {
            // Header row: symbol + side chip + leverage
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.symbol, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .border(1.dp, BorderStrong, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${p.leverage.toInt()}x", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(sideColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (long) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = sideColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        p.side.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = sideColor
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Price track: SL -------- Entry ---- Mark -------- TP
            PriceTrack(p)

            Spacer(Modifier.height(12.dp))

            // Price data row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataCol("Entry", "%.4f".format(p.entry))
                DataCol("Mark", "%.4f".format(p.mark))
                DataCol("Size", "%.4f".format(p.size))
                DataCol("SL", p.stop_loss?.let { "%.4f".format(it) } ?: "—", color = AccentRed.copy(alpha = 0.85f))
                DataCol("TP", p.take_profit?.let { "%.4f".format(it) } ?: "—", color = AccentGreen.copy(alpha = 0.85f))
            }

            Spacer(Modifier.height(14.dp))
            PnlChip(amount = p.unrealized_pnl, pct = p.unrealized_pnl_pct)
        }
    }
}

@Composable
private fun DataCol(label: String, value: String, color: Color = Color.Unspecified) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, style = NumericSmall, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
    }
}

@Composable
private fun PriceTrack(p: PositionDto) {
    val sl = p.stop_loss
    val tp = p.take_profit
    val values = listOfNotNull(sl, tp, p.entry, p.mark)
    val minV = values.minOrNull() ?: 0.0
    val maxV = values.maxOrNull() ?: 1.0
    val span = (maxV - minV).takeIf { it > 0 } ?: 1.0
    fun pos(v: Double): Float = ((v - minV) / span).toFloat().coerceIn(0f, 1f)

    val pnlPositive = p.unrealized_pnl >= 0
    val fillColor = if (pnlPositive) AccentGreen else AccentRed
    val fromFrac = pos(minOf(p.entry, p.mark))
    val toFrac = pos(maxOf(p.entry, p.mark))

    Box(
        Modifier
            .fillMaxWidth()
            .height(22.dp)
    ) {
        // Base track
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
        // Filled segment from entry to mark
        Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(fromFrac.coerceAtLeast(0.001f)))
            Box(
                Modifier
                    .weight((toFrac - fromFrac).coerceAtLeast(0.001f))
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(fillColor)
            )
            Spacer(Modifier.weight((1f - toFrac).coerceAtLeast(0.001f)))
        }
        // SL marker
        if (sl != null) {
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(pos(sl).coerceAtLeast(0.001f)))
                Marker(AccentRed)
                Spacer(Modifier.weight((1f - pos(sl)).coerceAtLeast(0.001f)))
            }
        }
        // TP marker
        if (tp != null) {
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.weight(pos(tp).coerceAtLeast(0.001f)))
                Marker(AccentGreen)
                Spacer(Modifier.weight((1f - pos(tp)).coerceAtLeast(0.001f)))
            }
        }
        // Entry marker (neutral)
        Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(pos(p.entry).coerceAtLeast(0.001f)))
            Marker(TextMuted, inner = 6.dp, outer = 10.dp)
            Spacer(Modifier.weight((1f - pos(p.entry)).coerceAtLeast(0.001f)))
        }
        // Mark marker (prominent, white)
        Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(pos(p.mark).coerceAtLeast(0.001f)))
            Marker(Color.White, inner = 8.dp, outer = 14.dp)
            Spacer(Modifier.weight((1f - pos(p.mark)).coerceAtLeast(0.001f)))
        }
    }
}

@Composable
private fun Marker(
    color: Color,
    inner: androidx.compose.ui.unit.Dp = 6.dp,
    outer: androidx.compose.ui.unit.Dp = 10.dp
) {
    Box(
        Modifier
            .size(outer)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(inner).clip(RoundedCornerShape(999.dp)).background(color))
    }
}
