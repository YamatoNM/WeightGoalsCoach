package com.polar.pulsecoach.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.polar.pulsecoach.ui.screens.BodyScreen
import com.polar.pulsecoach.ui.screens.CoachScreen
import com.polar.pulsecoach.ui.screens.GoalsScreen
import com.polar.pulsecoach.ui.screens.HeartScreen
import com.polar.pulsecoach.ui.screens.SessionsScreen
import com.polar.pulsecoach.ui.screens.TodayScreen
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText

/** The six tabs of the design's bottom bar, in order. */
enum class PulseTab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.GridView),
    Heart("heart", "Heart", Icons.Outlined.FavoriteBorder),
    Sessions("sessions", "Sessions", Icons.Outlined.Timer),
    Body("body", "Body", Icons.Outlined.MonitorWeight),
    Coach("coach", "Coach", Icons.Outlined.ChatBubbleOutline),
    Goals("goals", "Goals", Icons.Outlined.TrackChanges)
}

@Composable
fun PulseCoachApp() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination

    Scaffold(
        backgroundColor = PulseColors.Background,
        bottomBar = {
            PulseBottomBar(
                selected = PulseTab.values().firstOrNull { tab ->
                    currentDestination?.hierarchy?.any { it.route == tab.route } == true
                } ?: PulseTab.Today,
                onSelect = { tab ->
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PulseTab.Today.route,
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            composable(PulseTab.Today.route) { TodayScreen(onOpenCoach = { navController.navigate(PulseTab.Coach.route) }) }
            composable(PulseTab.Heart.route) { HeartScreen() }
            composable(PulseTab.Sessions.route) { SessionsScreen() }
            composable(PulseTab.Body.route) { BodyScreen() }
            composable(PulseTab.Coach.route) { CoachScreen() }
            composable(PulseTab.Goals.route) { GoalsScreen() }
        }
    }
}

@Composable
private fun PulseBottomBar(selected: PulseTab, onSelect: (PulseTab) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(PulseColors.NavBackground)
            .navigationBarsPadding()
    ) {
        Divider(color = PulseColors.Border, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            PulseTab.values().forEach { tab ->
                val isSelected = tab == selected
                val color = if (isSelected) PulseColors.Accent else PulseColors.NavInactive
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tab.label,
                        color = color,
                        style = PulseText.NavLabel.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

/** Shared screen frame: eyebrow label + big title, with the body below. Filled in by later steps. */
@Composable
fun PulseScreenFrame(
    eyebrow: String,
    title: String,
    trailing: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(PulseColors.Background)
            .padding(start = 20.dp, end = 20.dp, top = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(eyebrow.uppercase(), style = PulseText.Eyebrow, color = PulseColors.TextMuted)
                Spacer(Modifier.height(4.dp))
                Text(title, style = PulseText.ScreenTitle, color = PulseColors.Text)
            }
            trailing()
        }
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxSize()) { content() }
    }
}
