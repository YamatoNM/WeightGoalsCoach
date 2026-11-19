package com.polar.pulsecoach.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.polarsensordatacollector.ui.landing.MainActivity
import com.polar.pulsecoach.account.AuthRepository
import com.polar.pulsecoach.account.Session
import com.polar.pulsecoach.account.UserProfile
import com.polar.pulsecoach.ui.components.BackButton
import com.polar.pulsecoach.ui.components.PulseCard
import com.polar.pulsecoach.ui.components.SecondaryButton
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    val session: StateFlow<Session?> = repository.session

    fun setCoachConsent(enabled: Boolean) = update { it.copy(coachDataConsent = enabled) }

    /** Only admins can change this; the repository's session still gates access via [Session.rawDataAccess]. */
    fun setRawDataAccess(enabled: Boolean) {
        if (session.value?.isAdmin == true) update { it.copy(developerToolsEnabled = enabled) }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    private fun update(change: (UserProfile) -> UserProfile) {
        val profile = session.value?.profile ?: return
        viewModelScope.launch { repository.updateProfile(change(profile)) }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, onEditProfile: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsState()
    val s = session ?: return
    val p = s.profile
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackButton(onBack)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (s.isAdmin) "ADMIN · ${p.email}" else p.email, style = PulseText.Eyebrow, color = PulseColors.TextMuted)
            Text("Settings", style = PulseText.ScreenTitle, color = PulseColors.Text)
        }

        PulseCard {
            Text("Profile", style = PulseText.SectionTitle, color = PulseColors.Text)
            InfoRow("Name", p.name)
            InfoRow("Height", p.heightCm?.let { "${fmt(it)} cm" })
            InfoRow("Weight", p.weightKg?.let { "${fmt(it)} kg" })
            InfoRow("Age", p.age?.toString())
            InfoRow("Body fat", p.bodyFatPercent?.let { "${fmt(it)} %" })
            InfoRow("BMI", p.bmi?.let { String.format("%.1f", it) })
            InfoRow("Max HR", p.maxHr?.let { "$it bpm" + if (p.maxHrOverride == null) " (estimated)" else "" })
            SecondaryButton("Edit profile", onClick = onEditProfile, modifier = Modifier.fillMaxWidth())
        }

        PulseCard {
            Text("Privacy", style = PulseText.SectionTitle, color = PulseColors.Text)
            SwitchRow(
                title = "Coach can use my data",
                subtitle = "Heart-rate and body data are used for suggestions. Everything stays on this phone.",
                checked = p.coachDataConsent,
                onChange = viewModel::setCoachConsent,
                tint = PulseColors.Cyan
            )
        }

        if (s.isAdmin) {
            PulseCard(background = PulseColors.CoachBackground, border = PulseColors.CoachBorder) {
                Text("Developer tools", style = PulseText.SectionTitle, color = PulseColors.CyanText)
                SwitchRow(
                    title = "Raw data access",
                    subtitle = "Shows the Polar Sensor Data Collector screens with raw device values.",
                    checked = p.developerToolsEnabled,
                    onChange = viewModel::setRawDataAccess,
                    tint = PulseColors.Cyan
                )
                if (s.rawDataAccess) {
                    SecondaryButton(
                        "Open raw data screens",
                        onClick = { context.startActivity(Intent(context, MainActivity::class.java)) },
                        modifier = Modifier.fillMaxWidth(),
                        contentColor = PulseColors.CyanText
                    )
                }
            }
        }

        SecondaryButton("Log out", onClick = viewModel::logout, modifier = Modifier.fillMaxWidth(), contentColor = PulseColors.Accent)
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.TextMuted, modifier = Modifier.weight(1f))
        Text(value ?: "—", style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.Text)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, tint: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.Text)
            Text(subtitle, style = PulseText.Small, color = PulseColors.TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedThumbColor = tint, checkedTrackColor = tint, uncheckedThumbColor = PulseColors.TextMuted)
        )
    }
}

private fun fmt(v: Float) = if (v % 1f == 0f) v.toInt().toString() else String.format("%.1f", v)
