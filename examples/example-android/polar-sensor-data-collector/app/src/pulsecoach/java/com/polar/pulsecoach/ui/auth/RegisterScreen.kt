package com.polar.pulsecoach.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polar.pulsecoach.ui.components.CheckboxRow
import com.polar.pulsecoach.ui.components.PrimaryButton
import com.polar.pulsecoach.ui.components.PulseTextField
import com.polar.pulsecoach.ui.components.StepHeader
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText

private val STRENGTH_COLORS = listOf(PulseColors.Zone5, PulseColors.Zone4, PulseColors.Zone3, PulseColors.Cyan)

@Composable
fun RegisterScreen(onBackToLogin: () -> Unit, viewModel: RegisterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StepHeader(step = 1, of = 2, onBack = onBackToLogin)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Create account", style = PulseText.ScreenTitle.copy(fontSize = 44.sp, lineHeight = 42.sp), color = PulseColors.Text)
            Text("Next, you’ll set up your profile so Coach can calculate your zones.", style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.TextSecondary)
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PulseTextField("Name", state.name, viewModel::onName, placeholder = "Your name")
            PulseTextField("Email", state.email, viewModel::onEmail, placeholder = "you@example.com", keyboardType = KeyboardType.Email)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PulseTextField(
                    "Password", state.password, viewModel::onPassword,
                    placeholder = "At least 8 characters", isPassword = true, imeAction = ImeAction.Done
                )
                StrengthBars(state.strengthScore)
                Text(state.strengthLabel, style = PulseText.Small, color = PulseColors.TextMuted)
            }

            Box(
                Modifier
                    .background(PulseColors.CoachBackground, RoundedCornerShape(14.dp))
                    .border(1.dp, PulseColors.CoachBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                CheckboxRow(
                    checked = state.coachDataConsent,
                    onCheckedChange = viewModel::onCoachConsent,
                    text = "Let Coach use my heart-rate and body data to give suggestions. You can turn this off in Settings.",
                    tint = PulseColors.Cyan,
                    textColor = PulseColors.CyanText
                )
            }
            CheckboxRow(
                checked = state.termsAccepted,
                onCheckedChange = viewModel::onTerms,
                text = "I agree to the Terms and Privacy Policy.",
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            if (state.error != null) {
                Text(state.error!!, style = PulseText.Small.copy(fontSize = 13.sp), color = PulseColors.Amber)
            }
            PrimaryButton(
                "Create account", onClick = viewModel::register,
                enabled = state.canSubmit, loading = state.loading, modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.TextSecondary)
            TextButton(onClick = onBackToLogin) {
                Text("Log in", style = PulseText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = PulseColors.Text)
            }
        }
    }
}

@Composable
private fun StrengthBars(score: Int) {
    Row(Modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(4) { i ->
            val color = if (i < score) STRENGTH_COLORS[score - 1] else PulseColors.BorderStrong
            Box(Modifier.weight(1f).height(4.dp).background(color, RoundedCornerShape(2.dp)))
        }
    }
}
