package com.polar.pulsecoach.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.polar.pulsecoach.ui.components.OrDivider
import com.polar.pulsecoach.ui.components.PrimaryButton
import com.polar.pulsecoach.ui.components.PulseTextField
import com.polar.pulsecoach.ui.components.SecondaryButton
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onCreateAccount: () -> Unit, viewModel: LoginViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notAvailable: (String) -> Unit = { what -> scope.launch { snackbar.showSnackbar("$what isn't available yet.") } }

    Column(
        Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        EcgHeader()
        Column(
            Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pulse Coach", style = PulseText.ScreenTitle.copy(fontSize = 52.sp, lineHeight = 50.sp), color = PulseColors.Text)
                Text(
                    "Heart-rate training with a coach that reads your Verity Sense.",
                    style = PulseText.Body,
                    color = PulseColors.TextSecondary
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                PulseTextField(
                    label = "Email",
                    value = state.email,
                    onValueChange = viewModel::onEmail,
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email
                )
                PulseTextField(
                    label = "Password",
                    value = state.password,
                    onValueChange = viewModel::onPassword,
                    placeholder = "••••••••",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    error = state.error,
                    labelTrailing = {
                        TextButton(onClick = { notAvailable("Password reset") }) {
                            Text("Forgot password?", style = PulseText.Body.copy(fontSize = 13.sp), color = PulseColors.CyanText)
                        }
                    }
                )
                PrimaryButton("Log in", onClick = viewModel::login, loading = state.loading, modifier = Modifier.padding(top = 4.dp))
            }

            OrDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton("Apple", onClick = { notAvailable("Sign in with Apple") }, modifier = Modifier.weight(1f))
                SecondaryButton("Google", onClick = { notAvailable("Sign in with Google") }, modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("New here?", style = PulseText.Body.copy(fontSize = 14.sp), color = PulseColors.TextSecondary)
                TextButton(onClick = onCreateAccount) {
                    Text("Create an account", style = PulseText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold), color = PulseColors.Text)
                }
            }
        }
        SnackbarHost(snackbar)
    }
}

/** The ECG trace across the top of the Log in screen (decorative). */
@Composable
private fun EcgHeader() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
            .padding(top = 40.dp)
    ) {
        // Points from the design's 390×200 SVG, scaled to the canvas.
        val sx = size.width / 390f
        val sy = size.height / 200f
        val pts = listOf(0 to 120, 70 to 120, 92 to 120, 108 to 70, 126 to 170, 146 to 40, 166 to 150, 180 to 120,
            250 to 120, 268 to 96, 284 to 132, 298 to 120, 390 to 120)
        drawLine(
            PulseColors.Border, Offset(0f, 150 * sy), Offset(size.width, 150 * sy),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
        )
        val path = Path().apply {
            pts.forEachIndexed { i, (x, y) -> if (i == 0) moveTo(x * sx, y * sy) else lineTo(x * sx, y * sy) }
        }
        drawPath(path, PulseColors.Accent, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
