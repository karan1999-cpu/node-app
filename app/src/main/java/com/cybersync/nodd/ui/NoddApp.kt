package com.cybersync.nodd.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.cybersync.nodd.R
import com.cybersync.nodd.data.ContactActions
import com.cybersync.nodd.data.Profile
import com.cybersync.nodd.data.ProfileStore
import com.cybersync.nodd.ui.screens.CardScreen
import com.cybersync.nodd.ui.screens.EditCardScreen
import com.cybersync.nodd.ui.screens.PlanScreen
import com.cybersync.nodd.ui.screens.ScanScreen
import com.cybersync.nodd.ui.screens.ShareScreen

private enum class Tab(val label: String, val icon: Int) {
    Card("My card", R.drawable.ic_card),
    Scan("Scan", R.drawable.ic_scan),
    Plan("Plan", R.drawable.ic_doc),
}

private enum class Overlay { None, Edit, Share }

@Composable
fun NoddApp(store: ProfileStore) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(store.load()) }
    var tab by rememberSaveable { mutableStateOf(Tab.Card) }
    var overlay by rememberSaveable { mutableStateOf(Overlay.None) }

    fun update(next: Profile) {
        store.save(next)
        profile = next
    }

    BackHandler(enabled = overlay != Overlay.None) { overlay = Overlay.None }

    when (overlay) {
        Overlay.Edit -> EditCardScreen(
            initial = profile,
            onSave = {
                update(it)
                overlay = Overlay.None
            },
            onBack = { overlay = Overlay.None },
        )

        Overlay.Share -> ShareScreen(
            profile = profile,
            onClose = { overlay = Overlay.None },
            onSendFile = { ContactActions.shareCard(context, profile) },
        )

        Overlay.None -> Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(painterResource(item.icon), contentDescription = null) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    Tab.Card -> CardScreen(
                        profile = profile,
                        onEdit = { overlay = Overlay.Edit },
                        onShowQr = { overlay = Overlay.Share },
                        onSendFile = { ContactActions.shareCard(context, profile) },
                        onToggle = { field, include ->
                            update(profile.copy(hidden = if (include) profile.hidden - field else profile.hidden + field))
                        },
                    )
                    Tab.Scan -> ScanScreen()
                    Tab.Plan -> PlanScreen()
                }
            }
        }
    }
}
