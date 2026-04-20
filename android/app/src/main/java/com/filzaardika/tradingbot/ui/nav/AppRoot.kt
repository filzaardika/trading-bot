package com.filzaardika.tradingbot.ui.nav

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.SecureStore
import com.filzaardika.tradingbot.data.ApiResult
import com.filzaardika.tradingbot.ui.common.LiveBadge
import com.filzaardika.tradingbot.ui.common.PulsingDot
import com.filzaardika.tradingbot.ui.control.ControlScreen
import com.filzaardika.tradingbot.ui.dashboard.DashboardScreen
import com.filzaardika.tradingbot.ui.pair.PairScreen
import com.filzaardika.tradingbot.ui.positions.PositionsScreen
import com.filzaardika.tradingbot.ui.settings.SettingsScreen
import com.filzaardika.tradingbot.ui.theme.AccentBlue
import com.filzaardika.tradingbot.ui.theme.AccentGreen
import com.filzaardika.tradingbot.ui.theme.AccentRed
import com.filzaardika.tradingbot.ui.theme.BgElevated
import com.filzaardika.tradingbot.ui.theme.TextMuted
import kotlinx.coroutines.delay

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("dashboard", "Dashboard", Icons.Filled.Dashboard),
    Tab("positions", "Positions", Icons.Filled.ShowChart),
    Tab("control", "Control", Icons.Filled.PowerSettingsNew),
    Tab("settings", "Settings", Icons.Filled.Settings),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot(ctx: Context) {
    val repo = remember { BotRepo(ctx) }
    var paired by remember { mutableStateOf(SecureStore.isPaired(ctx)) }

    if (!paired) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            PairScreen(repo = repo, onPaired = { paired = true })
        }
        return
    }

    // Global meta state — fetched here so TopBar is always accurate across all tabs
    var mode by remember { mutableStateOf("paper") }
    var testnet by remember { mutableStateOf(true) }
    var connected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            when (val r = repo.dashboard()) {
                is ApiResult.Ok -> {
                    mode = r.value.mode
                    testnet = r.value.testnet
                    connected = true
                }
                is ApiResult.Err -> connected = false
            }
            delay(7000)
        }
    }

    val nav = rememberNavController()
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route ?: "dashboard"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgElevated.copy(alpha = 0.92f),
                    scrolledContainerColor = BgElevated
                ),
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(AccentGreen, AccentBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        androidx.compose.foundation.layout.Column {
                            Text(
                                "Trading Bot",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingDot(
                                    color = if (connected) AccentGreen else AccentRed,
                                    diameter = 6.dp
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (connected) "LIVE" else "OFFLINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                },
                actions = {
                    LiveBadge(mode = mode, testnet = testnet)
                    Spacer(Modifier.width(12.dp))
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BgElevated,
                tonalElevation = 0.dp
            ) {
                tabs.forEach { t ->
                    val selected = currentRoute.startsWith(t.route)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != t.route) {
                                nav.navigate(t.route) {
                                    popUpTo("dashboard") { inclusive = false; saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = AccentGreen,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = AccentGreen.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavHost(navController = nav, startDestination = "dashboard") {
                composable("dashboard") {
                    DashboardScreen(repo = repo)
                }
                composable("positions") { PositionsScreen(repo = repo) }
                composable("control") { ControlScreen(repo = repo) }
                composable("settings") {
                    SettingsScreen(repo = repo, onUnpair = {
                        SecureStore.clear(ctx)
                        paired = false
                    })
                }
            }
        }
    }
}
