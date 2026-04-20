package com.filzaardika.tradingbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.filzaardika.tradingbot.ui.nav.AppRoot
import com.filzaardika.tradingbot.ui.theme.TradingBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TradingBotTheme {
                AppRoot(applicationContext)
            }
        }
    }
}
