package com.polar.pulsecoach.ui.theme

import android.graphics.Typeface
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/** Color tokens from the Pulse Coach design. */
object PulseColors {
    val Background = Color(0xFF0E0F0C)
    val Surface = Color(0xFF171914)
    val Border = Color(0xFF23251F)
    val BorderStrong = Color(0xFF2A2C26)
    val NavBackground = Color(0xFF141511)

    val Text = Color(0xFFEDEDE6)
    val TextBright = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB9BCB1)
    val TextMuted = Color(0xFF9A9D93)
    val NavInactive = Color(0xFF8C8F86)

    val Accent = Color(0xFFFF6B4A)
    val OnAccent = Color(0xFF1A0B06)
    val Cyan = Color(0xFF5CC8E8)
    val CyanText = Color(0xFF8FD9F0)
    val CoachBackground = Color(0xFF12232A)
    val CoachBorder = Color(0xFF1E3A44)
    val Amber = Color(0xFFF2A65A)

    val Zone1 = Color(0xFF4B5560)
    val Zone2 = Color(0xFF3F8FB8)
    val Zone3 = Color(0xFFF2C45A)
    val Zone4 = Color(0xFFF07A3A)
    val Zone5 = Color(0xFFC9341E)
}

/**
 * Font roles from the design. They use system fonts for now
 * (Barlow Condensed → sans-serif-condensed, IBM Plex Sans → sans-serif, IBM Plex Mono → monospace).
 * Swap in bundled font files here later without touching the screens.
 */
object PulseFonts {
    val Display = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    val Body = FontFamily.SansSerif
    val Mono = FontFamily.Monospace
}

object PulseText {
    /** Screen titles, e.g. "Today". */
    val ScreenTitle = TextStyle(fontFamily = PulseFonts.Display, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 40.sp)
    /** Big numbers, e.g. the live HR. */
    val BigNumber = TextStyle(fontFamily = PulseFonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 96.sp, lineHeight = 82.sp)
    /** Stat tile values. */
    val StatValue = TextStyle(fontFamily = PulseFonts.Display, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 34.sp)
    /** Upper-case mono labels, e.g. "LIVE HEART RATE". */
    val Eyebrow = TextStyle(fontFamily = PulseFonts.Mono, fontSize = 12.sp, letterSpacing = 0.08.em)
    val SectionTitle = TextStyle(fontFamily = PulseFonts.Body, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    val Body = TextStyle(fontFamily = PulseFonts.Body, fontSize = 15.sp, lineHeight = 22.sp)
    val Small = TextStyle(fontFamily = PulseFonts.Body, fontSize = 12.sp)
    val NavLabel = TextStyle(fontFamily = PulseFonts.Body, fontWeight = FontWeight.Medium, fontSize = 11.sp)
}

private val PulseMaterialColors = darkColors(
    primary = PulseColors.Accent,
    primaryVariant = PulseColors.Accent,
    onPrimary = PulseColors.OnAccent,
    secondary = PulseColors.Cyan,
    onSecondary = PulseColors.Background,
    background = PulseColors.Background,
    onBackground = PulseColors.Text,
    surface = PulseColors.Surface,
    onSurface = PulseColors.Text,
    error = PulseColors.Zone5
)

private val PulseTypography = Typography(
    defaultFontFamily = PulseFonts.Body,
    h4 = PulseText.ScreenTitle,
    body1 = PulseText.Body,
    caption = PulseText.Small
)

@Composable
fun PulseCoachTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = PulseMaterialColors, typography = PulseTypography, content = content)
}
