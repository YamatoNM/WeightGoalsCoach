package com.polar.pulsecoach.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polar.pulsecoach.ui.PulseScreenFrame
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText

// Step 1 placeholders: each screen has its header from the design. Content arrives in later steps.

@Composable
fun TodayScreen(onOpenCoach: () -> Unit) = PulseScreenFrame(eyebrow = "Today", title = "Today") {
    PlaceholderCard("Live heart rate, Coach insight, stat tiles and time in zones come in step 4.")
}

@Composable
fun HeartScreen() = PulseScreenFrame(eyebrow = "From Verity Sense", title = "Heart") {
    PlaceholderCard("Day / Week / Month charts, zones and the 2-min HRV check come in step 5.")
}

@Composable
fun SessionsScreen() = PulseScreenFrame(eyebrow = "This week", title = "Sessions") {
    PlaceholderCard("Session recording and history come in step 6.")
}

@Composable
fun BodyScreen() = PulseScreenFrame(eyebrow = "Manual log", title = "Body") {
    PlaceholderCard("Weight, body fat, measurements and photos come in step 7.")
}

@Composable
fun CoachScreen() = PulseScreenFrame(eyebrow = "Coach", title = "Coach") {
    PlaceholderCard("The rule-based Coach agent and chat come in step 7.")
}

@Composable
fun GoalsScreen() = PulseScreenFrame(eyebrow = "This month", title = "Goals") {
    PlaceholderCard("Goals and Coach suggestions come in step 7.")
}

@Composable
private fun PlaceholderCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PulseColors.Surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PulseColors.Border)
    ) {
        Text(text, style = PulseText.Body, color = PulseColors.TextSecondary, modifier = Modifier.padding(20.dp))
    }
}
