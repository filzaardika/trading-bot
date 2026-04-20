package com.filzaardika.tradingbot.ui.nav

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.filzaardika.tradingbot.data.BotRepo
import com.filzaardika.tradingbot.data.SecureStore
import com.filzaardika.tradingbot.ui.control.ControlScreen
import com.filzaardika.tradingbot.ui.dashboard.DashboardScreen
import com.filzaardika.tradingbot.ui.pair.PairScreen
import com.filzaardika.tradingbot.ui.positions.PositionsScreen
import com.filzaardika.tradingbot.ui.settings.SettingsScreen

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("dashboard", "Dashboard", Icons.Filled.Dashboard),
    Tab("positions", "Positions", Icons.Filled.TrendingUp),
    Tab("control", "Control", Icons.Filled.PowerSettingsNew),
    Tab("settings", "Settings", Icons.Filled.Settings),
)

@Composable
fun AppRoot(ctx: Context) {
    val repo = remember { BotRepo(ctx) }
    var paired by remember { mutableStateOf(SecureStore.isPaired(ctx)) }

    if (!paired) {
        PairScreen(repo = repo, onPaired = { paired = true })
        return
    }

    val nav = rememberNavController()
    val backstack by nav.currentBackStackEntryAsState()
    val currentRoute = backstack?.destination?.route ?: "dashboard"

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = currentRoute.startsWith(t.route),
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
                        label = { Text(t.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") { DashboardScreen(repo = repo, onUnpair = {
                SecureStore.clear(ctx); paired = false
            }) }
            composable("positions") { PositionsScreen(repo = repo) }
            composable("control") { ControlScreen(repo = repo) }
            composable("settings") { SettingsScreen(repo = repo, onUnpair = {
                SecureStore.clear(ctx); paired = false
            }) }
        }
    }
}
