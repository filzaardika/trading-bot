package com.filzaardika.tradingbot.ui.positions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.PositionDto
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
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

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Positions", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(12.dp))
        error?.let {
            Card { Text("Error: $it", color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))
        }
        if (items.isEmpty()) {
            Card { Text("No open positions.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { p -> PositionCard(p) }
            }
        }
    }
}

@Composable
private fun PositionCard(p: PositionDto) {
    val color = if (p.unrealized_pnl >= 0) AccentGreen else AccentRed
    val sideColor = if (p.side == "long") AccentGreen else AccentRed
    val sign = if (p.unrealized_pnl >= 0) "+" else ""
    Card {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(p.symbol, style = MaterialTheme.typography.titleLarge)
                Text(p.side.uppercase(), style = MaterialTheme.typography.labelLarge, color = sideColor)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Col("Entry", "%.4f".format(p.entry))
                Col("Mark", "%.4f".format(p.mark))
                Col("Size", "%.4f".format(p.size))
                Col("Lev", "${p.leverage.toInt()}x")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${sign}${"%,.2f".format(p.unrealized_pnl)} (${sign}${"%.2f".format(p.unrealized_pnl_pct)}%)",
                color = color,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun Col(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
