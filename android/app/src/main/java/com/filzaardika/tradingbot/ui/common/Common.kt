package com.filzaardika.tradingbot.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.ui.theme.AccentAmber
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.BgCard
import com.filzaardika.tradingbot.ui.theme.BgCardHigh
import com.filzaardika.tradingbot.ui.theme.Border
import com.filzaardika.tradingbot.ui.theme.BorderStrong
import com.filzaardika.tradingbot.ui.theme.LiveRed
import com.filzaardika.tradingbot.ui.theme.NumericMedium

/** Primary container with soft border and subtle gradient for depth. */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    padding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(listOf(BgCardHigh, BgCard))
            )
            .border(1.dp, Border, RoundedCornerShape(20.dp))
            .padding(padding)
    ) { content() }
}

/** Higher-emphasis card — used for the hero equity card. */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradient: List<Color>,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(gradient))
            .border(1.dp, BorderStrong, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) { content() }
}

@Composable
fun StatusDot(status: String, size: androidx.compose.ui.unit.Dp = 10.dp, modifier: Modifier = Modifier) {
    val c = when (status) {
        "running" -> AccentGreen
        "paused" -> AccentAmber
        "error" -> AccentRed
        else -> Color.Gray
    }
    Box(
        modifier = modifier.size(size).background(c.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(size * 0.55f).background(c, CircleShape))
    }
}

/** Softly pulsing "live" dot for WebSocket/polling indicator. */
@Composable
fun PulsingDot(color: Color = LiveRed, diameter: androidx.compose.ui.unit.Dp = 8.dp) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )
    val ringScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-ring"
    )
    Box(modifier = Modifier.size(diameter * 2), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(diameter * ringScale)
                .background(color.copy(alpha = (0.35f * (2f - ringScale)).coerceAtLeast(0f)), CircleShape)
        )
        Box(Modifier.size(diameter).background(color.copy(alpha = alpha), CircleShape))
    }
}

/** Colored pill chip — used for LIVE / PAPER / TESTNET / bot status. */
@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pulsing) {
            PulsingDot(color = color, diameter = 7.dp)
        } else {
            Box(Modifier.size(7.dp).background(color, CircleShape))
        }
        Spacer(Modifier.width(6.dp))
        Text(text, color = color, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun LiveBadge(mode: String, testnet: Boolean) {
    val live = mode == "live"
    val color = if (live) LiveRed else AccentGreen
    val label = buildString {
        append(if (live) "LIVE" else "PAPER")
        if (testnet) append(" · TESTNET")
    }
    StatusPill(text = label, color = color, pulsing = live)
}

@Composable
fun SectionHeader(
    title: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (trailing != null) trailing()
    }
}

/** Compact metric tile — label on top, big tabular value below, optional sub-line. */
@Composable
fun MetricTile(
    label: String,
    value: String,
    sub: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, padding = 14.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leadingIcon != null) { leadingIcon(); Spacer(Modifier.width(6.dp)) }
                Text(
                    label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = NumericMedium, color = valueColor)
            if (sub != null) {
                Spacer(Modifier.height(2.dp))
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Inline P&L chip with arrow icon and sign. */
@Composable
fun PnlChip(amount: Double, pct: Double, currency: String = "$") {
    val positive = amount >= 0
    val color = if (positive) AccentGreen else AccentRed
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (positive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        val sign = if (positive) "+" else ""
        Text(
            "$sign$currency${"%,.2f".format(amount)}  $sign${"%.2f".format(pct)}%",
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

/** Left-aligned KV row. */
@Composable
fun KvRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End
        )
    }
}

/** Thin divider line used between KV rows (optional). */
@Composable
fun HairlineDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
}
