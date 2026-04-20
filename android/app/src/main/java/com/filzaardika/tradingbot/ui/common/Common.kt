package com.filzaardika.tradingbot.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.ui.theme.AccentAmber
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.LiveRed

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
fun StatusDot(status: String, modifier: Modifier = Modifier) {
    val c = when (status) {
        "running" -> AccentGreen
        "paused" -> AccentAmber
        "error" -> AccentRed
        else -> Color.Gray
    }
    Box(modifier = modifier.size(10.dp).background(c, CircleShape))
}

@Composable
fun LiveBadge(mode: String, testnet: Boolean) {
    val live = mode == "live"
    val bg = if (live) LiveRed else AccentGreen
    Row(
        modifier = Modifier
            .background(bg.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(bg, CircleShape))
        Spacer(Modifier.size(6.dp))
        val text = buildString {
            append(if (live) "LIVE" else "PAPER")
            if (testnet) append(" · TESTNET")
        }
        Text(text, color = bg, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}
