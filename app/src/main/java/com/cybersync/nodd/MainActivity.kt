package com.cybersync.nodd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cybersync.nodd.data.ProfileStore
import com.cybersync.nodd.ui.NoddApp
import com.cybersync.nodd.ui.theme.NoddTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val store = ProfileStore(applicationContext)
        setContent {
            NoddTheme {
                NoddApp(store)
            }
        }
    }
}
