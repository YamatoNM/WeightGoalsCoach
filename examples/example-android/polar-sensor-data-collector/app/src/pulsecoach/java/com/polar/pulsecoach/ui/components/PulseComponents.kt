package com.polar.pulsecoach.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.polar.pulsecoach.ui.theme.PulseColors
import com.polar.pulsecoach.ui.theme.PulseText

/** Labelled text field in the design's style (label above, dark rounded box). */
@Composable
fun PulseTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    error: String? = null,
    labelTrailing: @Composable () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = PulseText.Body.copy(fontSize = 13.sp), color = PulseColors.TextSecondary, modifier = Modifier.weight(1f))
            labelTrailing()
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            textStyle = PulseText.Body.copy(fontSize = 16.sp),
            placeholder = { Text(placeholder, color = PulseColors.NavInactive) },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = if (visible) "Hide password" else "Show password",
                            tint = PulseColors.TextMuted
                        )
                    }
                }
            } else null,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = PulseColors.Text,
                backgroundColor = PulseColors.Surface,
                cursorColor = PulseColors.Accent,
                focusedBorderColor = PulseColors.Accent,
                unfocusedBorderColor = PulseColors.BorderStrong,
                errorBorderColor = PulseColors.Zone5
            )
        )
        if (error != null) {
            Text(error, style = PulseText.Small, color = PulseColors.Amber)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = PulseColors.Accent,
            contentColor = PulseColors.OnAccent,
            disabledBackgroundColor = PulseColors.BorderStrong,
            disabledContentColor = PulseColors.TextMuted
        )
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), color = PulseColors.TextSecondary, strokeWidth = 2.dp)
        } else {
            Text(text, style = PulseText.Body.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, contentColor: Color = PulseColors.Text) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, PulseColors.BorderStrong),
        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = PulseColors.Surface, contentColor = contentColor)
    ) {
        Text(text, style = PulseText.Body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium))
    }
}

/** Checkbox whose whole row (box + text) is one touch target. */
@Composable
fun CheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = PulseColors.Accent,
    textColor: Color = PulseColors.TextSecondary
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = tint, uncheckedColor = PulseColors.TextMuted, checkmarkColor = PulseColors.Background)
        )
        Text(text, style = PulseText.Body.copy(fontSize = 13.sp, lineHeight = 19.sp), color = textColor, modifier = Modifier.padding(top = 2.dp))
    }
}

/** Dark rounded card used across the app. */
@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    background: Color = PulseColors.Surface,
    border: Color = PulseColors.Border,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(24.dp))
            .border(1.dp, border, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

/** Round back button + "Step x of y" eyebrow + progress bars, as on the Register screen. */
@Composable
fun StepHeader(step: Int, of: Int, onBack: (() -> Unit)?) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                BackButton(onBack)
            } else {
                Spacer(Modifier.size(44.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("STEP $step OF $of", style = PulseText.Eyebrow, color = PulseColors.TextMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(of) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (i < step) PulseColors.Accent else PulseColors.BorderStrong, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

/** 44dp round back button from the design. */
@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(
        onClick = onBack,
        modifier = Modifier
            .size(44.dp)
            .background(PulseColors.Surface, CircleShape)
            .border(1.dp, PulseColors.BorderStrong, CircleShape)
    ) {
        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = PulseColors.Text)
    }
}

@Composable
fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(PulseColors.Border))
        Text("or", style = PulseText.Small, color = PulseColors.TextMuted)
        Box(Modifier.weight(1f).height(1.dp).background(PulseColors.Border))
    }
}

@Composable
fun HSpace(width: Int) = Spacer(Modifier.width(width.dp))
