package com.filzaardika.tradingbot.ui.pair

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.ui.common.Card
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.BgBase
import com.filzaardika.tradingbot.ui.theme.BgElevated
import com.filzaardika.tradingbot.ui.theme.Border
import com.filzaardika.tradingbot.ui.theme.HeroGradBottom
import com.filzaardika.tradingbot.ui.theme.HeroGradMid
import com.filzaardika.tradingbot.ui.theme.HeroGradTop
import com.filzaardika.tradingbot.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun PairScreen(repo: BotRepo, onPaired: () -> Unit) {
    var host by remember { mutableStateOf("https://bot.opclaw.my.id") }
    var token by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(HeroGradTop.copy(alpha = 0.35f), BgBase, BgBase)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 80.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(AccentGreen, AccentBlue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Pair with your bot",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Connect this device to your trading bot over HTTPS. Both fields are saved in the Android Keystore.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            Spacer(Modifier.height(28.dp))

            Card {
                Column {
                    FieldLabel("Host URL", icon = Icons.Filled.Lan)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        placeholder = { Text("https://bot.yourdomain.com", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    FieldLabel("Bearer Token", icon = Icons.Filled.Key)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        placeholder = { Text("Printed on bot startup", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = AccentRed, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(it, color = AccentRed, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = {
                            error = null; busy = true
                            scope.launch {
                                val cleanHost = host.trim()
                                when (val p = repo.ping(cleanHost)) {
                                    is ApiResult.Err -> { error = "Host unreachable: ${p.message}"; busy = false; return@launch }
                                    is ApiResult.Ok -> { /* host reachable */ }
                                }
                                when (val r = repo.pair(cleanHost, token.trim())) {
                                    is ApiResult.Ok -> if (r.value.ok) onPaired() else error = "Pairing rejected"
                                    is ApiResult.Err -> error = r.message
                                }
                                busy = false
                            }
                        },
                        enabled = !busy && host.isNotBlank() && token.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            contentColor = BgBase,
                            disabledContainerColor = BgElevated,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text(
                            if (busy) "Pairing…" else "Connect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Tip: expose your bot via a Cloudflare Tunnel (HTTPS) and point a subdomain at port 8080.",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = TextMuted)
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentGreen,
    unfocusedBorderColor = Border,
    cursorColor = AccentGreen,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)
