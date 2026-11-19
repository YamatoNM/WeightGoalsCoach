package com.polar.pulsecoach.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polar.pulsecoach.ui.components.BackButton
import com.polar.pulsecoach.ui.components.PrimaryButton
import com.polar.pulsecoach.ui.components.PulseCard
import com.polar.pulsecoach.ui.components.PulseTextField
import com.polar.pulsecoach.ui.components.StepHeader
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText

/**
 * Body profile form. [setup] = step 2 of registration (no back button);
 * otherwise it is the edit screen opened from Settings and [onDone] goes back.
 */
@Composable
fun ProfileScreen(setup: Boolean, onDone: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (setup) {
            StepHeader(step = 2, of = 2, onBack = null)
        } else {
            BackButton(onDone)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (setup) "Your profile" else "Edit profile", style = PulseText.ScreenTitle.copy(fontSize = 44.sp, lineHeight = 42.sp), color = PulseColors.Text)
            Text(
                "Coach uses these to set your heart-rate zones, estimate calories and track body changes.",
                style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.TextSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PulseTextField("Name", state.name, viewModel::onName, placeholder = "Your name")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Height, cm", state.heightCm, viewModel::onHeight, "180", state.errors[ProfileField.HEIGHT], Modifier.weight(1f))
                NumberField("Weight, kg", state.weightKg, viewModel::onWeight, "75", state.errors[ProfileField.WEIGHT], Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Age", state.age, viewModel::onAge, "30", state.errors[ProfileField.AGE], Modifier.weight(1f), decimal = false)
                NumberField("Body fat, %", state.bodyFatPercent, viewModel::onBodyFat, "Optional", state.errors[ProfileField.BODY_FAT], Modifier.weight(1f))
            }
            NumberField(
                "Max heart rate, bpm (optional)", state.maxHr, viewModel::onMaxHr,
                state.estimatedMaxHrHint, state.errors[ProfileField.MAX_HR], decimal = false, last = true
            )
        }

        ZonePreview(state)

        if (state.error != null) {
            Text(state.error!!, style = PulseText.Small.copy(fontSize = 13.sp), color = PulseColors.Amber)
        }
        PrimaryButton(if (setup) "Continue" else "Save", onClick = viewModel::save, loading = state.saving)
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    modifier: Modifier = Modifier,
    decimal: Boolean = true,
    last: Boolean = false
) = PulseTextField(
    label = label,
    value = value,
    onValueChange = onChange,
    modifier = modifier,
    placeholder = placeholder,
    keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
    imeAction = if (last) ImeAction.Done else ImeAction.Next,
    error = error
)

@Composable
private fun ZonePreview(state: ProfileUiState) {
    val zones = state.zonePreview
    PulseCard {
        Text("Heart rate zones", style = PulseText.SectionTitle, color = PulseColors.Text)
        if (zones.isEmpty()) {
            Text("Enter your age or max HR to see your zones.", style = PulseText.Small, color = PulseColors.TextMuted)
        } else {
            val colors = listOf(PulseColors.Zone1, PulseColors.Zone2, PulseColors.Zone3, PulseColors.Zone4, PulseColors.Zone5)
            zones.asReversed().forEach { z ->
                Row(Modifier.fillMaxWidth()) {
                    Text("Zone ${z.number}", style = PulseText.Body.copy(fontSize = 13.sp), color = colors[z.number - 1], modifier = Modifier.weight(1f))
                    Text(
                        if (z.highBpm == null) "${z.lowBpm}+ bpm" else "${z.lowBpm}–${z.highBpm} bpm",
                        style = PulseText.Small.copy(fontSize = 13.sp), color = PulseColors.TextSecondary, textAlign = TextAlign.End
                    )
                }
            }
            Text("From max HR ${state.effectiveMaxHr}", style = PulseText.Small, color = PulseColors.TextMuted)
        }
    }
}
