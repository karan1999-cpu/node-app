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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.cybersync.nodd.R
import com.cybersync.nodd.data.ContactActions
import com.cybersync.nodd.data.Profile
import com.cybersync.nodd.data.ProfileStore
import com.cybersync.nodd.data.vault.Vault
import com.cybersync.nodd.data.vault.VaultManager
import com.cybersync.nodd.ui.screens.CardScreen
import com.cybersync.nodd.ui.screens.EditCardScreen
import com.cybersync.nodd.ui.screens.PlanScreen
import com.cybersync.nodd.ui.screens.ScanScreen
import com.cybersync.nodd.ui.screens.ShareScreen
import com.cybersync.nodd.ui.vault.PinScreen
import com.cybersync.nodd.ui.vault.VaultHost
import com.cybersync.nodd.ui.vault.tapAnywhere

private enum class Tab(val label: String, val icon: Int) {
    Card("My card", R.drawable.ic_card),
    Scan("Scan", R.drawable.ic_scan),
    Plan("Plan", R.drawable.ic_doc),
}

private sealed interface Route {
    data object Home : Route
    data object Edit : Route
    data object Share : Route
    data class Pin(val ultra: Boolean) : Route
    data class VaultOpen(val vault: Vault, val ultra: Boolean) : Route
}

@Composable
fun NoddApp(store: ProfileStore) {
    val context = LocalContext.current
    val manager = remember { VaultManager(context) }
    var profile by remember { mutableStateOf(store.load()) }
    var tab by rememberSaveable { mutableStateOf(Tab.Card) }
    // Plain remember, not saveable: on process death the vault re-locks instead of reopening.
    var route by remember { mutableStateOf<Route>(Route.Home) }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Lock any open vault and wipe decrypted temp files when the app leaves the foreground.
    val owner = context.findActivity() as? LifecycleOwner
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                route = Route.Home
                pinError = null
                manager.clearTmp()
            }
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    fun update(next: Profile) {
        store.save(next)
        profile = next
    }

    fun leaveVault() {
        route = Route.Home
        pinError = null
        manager.clearTmp()
    }

    BackHandler(enabled = route != Route.Home) { leaveVault() }

    when (val r = route) {
        is Route.Edit -> EditCardScreen(
            initial = profile,
            onSave = {
                update(it)
                route = Route.Home
            },
            onBack = { route = Route.Home },
        )

        is Route.Share -> ShareScreen(
            profile = profile,
            onClose = { route = Route.Home },
            onSendFile = { ContactActions.shareCard(context, profile) },
        )

        is Route.Pin -> {
            val id = if (r.ultra) manager.ultraId() else manager.mainId()
            val setup = !manager.isSetUp(id)
            PinScreen(
                title = if (r.ultra) "Ultra Go Dark" else "Go Dark",
                subtitle = if (setup) {
                    if (r.ultra) "This is a second, separate vault." else "Set a PIN for your private vault."
                } else {
                    "Enter your PIN."
                },
                isSetup = setup,
                error = pinError,
                onSubmit = { pin ->
                    pinError = null
                    val vault = if (setup) manager.setup(id, pin) else manager.unlock(id, pin)
                    pin.fill('\u0000')
                    if (vault == null) pinError = "Wrong PIN" else route = Route.VaultOpen(vault, r.ultra)
                },
                onBack = { leaveVault() },
            )
        }

        is Route.VaultOpen -> VaultHost(
            manager = manager,
            vault = r.vault,
            title = if (r.ultra) "Ultra Go Dark" else "Go Dark",
            isUltra = r.ultra,
            onLock = ::leaveVault,
        )

        // Five quick taps anywhere on the main screen open Ultra Go Dark, with no PIN.
        is Route.Home -> Box(
            modifier = Modifier
                .fillMaxSize()
                .tapAnywhere { route = Route.VaultOpen(manager.openUltra(), ultra = true) },
        ) {
            Scaffold(
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
                            onEdit = { route = Route.Edit },
                            onShowQr = { route = Route.Share },
                            onSendFile = { ContactActions.shareCard(context, profile) },
                            onToggle = { field, include ->
                                update(profile.copy(hidden = if (include) profile.hidden - field else profile.hidden + field))
                            },
                            onGoDark = {
                                pinError = null
                                route = Route.Pin(ultra = false)
                            },
                        )
                        Tab.Scan -> ScanScreen()
                        Tab.Plan -> PlanScreen()
                    }
                }
            }
        }
    }
}
