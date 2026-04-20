package com.filzaardika.tradingbot.ui.pair

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.data.BotRepo
import kotlinx.coroutines.launch

@Composable
fun PairScreen(repo: BotRepo, onPaired: () -> Unit) {
    var host by remember { mutableStateOf("http://10.0.2.2:8080") }
    var token by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pair with bot", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Enter your bot's HTTPS host (tunnel URL) and the bearer token printed on startup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Bearer token") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                error = null
                busy = true
                scope.launch {
                    when (val r = repo.pair(host.trim(), token.trim())) {
                        is ApiResult.Ok -> if (r.value.ok) onPaired() else error = "pairing rejected"
                        is ApiResult.Err -> error = r.message
                    }
                    busy = false
                }
            },
            enabled = !busy && host.isNotBlank() && token.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Pairing..." else "Pair") }
    }
}
