package com.kora.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Kora MVP's neutral surfaces, shared typography rhythm, and tenant brand accent. */
object KoraTokens {
    val Brand = Color(0xFF334155)
    val Ink = Color(0xFF0F172A)
    val MutedInk = Color(0xFF475569)
    val Canvas = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFF1F5F9)
    val Border = Color(0xFFDBE3EC)
    val Danger = Color(0xFFB42318)
    val Success = Color(0xFF15803D)
    val Warning = Color(0xFFB45309)
    val Info = Color(0xFF0369A1)

    val Space1: Dp = 4.dp
    val Space2: Dp = 8.dp
    val Space3: Dp = 12.dp
    val Space4: Dp = 16.dp
    val Space5: Dp = 24.dp
    val Space6: Dp = 32.dp
    // Inputs use a tight radius; larger radii are reserved for cards and sheets.
    val RadiusControl: Dp = 10.dp
    val RadiusField: Dp = 4.dp
    val RadiusCard: Dp = 12.dp
}

private val KoraTypography = Typography(
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, lineHeight = 15.sp),
)

private fun tenantColor(hex: String?, fallback: Color): Color {
    val normalized = hex?.trim()?.removePrefix("#")?.takeIf { it.matches(Regex("[0-9a-fA-F]{6}")) } ?: return fallback
    return runCatching { Color(android.graphics.Color.parseColor("#$normalized")) }.getOrDefault(fallback)
}

private fun linear(channel: Float): Double {
    val value = channel.toDouble()
    return if (value <= .04045) value / 12.92 else Math.pow((value + .055) / 1.055, 2.4)
}

private fun luminance(color: Color) = .2126 * linear(color.red) + .7152 * linear(color.green) + .0722 * linear(color.blue)
private fun contrast(first: Color, second: Color): Double {
    val light = maxOf(luminance(first), luminance(second))
    val dark = minOf(luminance(first), luminance(second))
    return (light + .05) / (dark + .05)
}

private fun foregroundOn(color: Color): Color =
    if (contrast(color, Color.White) >= contrast(color, KoraTokens.Ink)) Color.White else KoraTokens.Ink

private fun readableAccent(color: Color, background: Color, minimum: Double = 4.5): Color {
    if (contrast(color, background) >= minimum) return color
    val target = if (luminance(background) > .5) Color.Black else Color.White
    var low = 0f
    var high = 1f
    repeat(18) {
        val mid = (low + high) / 2f
        val candidate = androidx.compose.ui.graphics.lerp(color, target, mid)
        if (contrast(candidate, background) >= minimum) high = mid else low = mid
    }
    return androidx.compose.ui.graphics.lerp(color, target, high)
}

private fun generatedAccent(primary: Color): Color {
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(primary.toArgb(), hsl)
    hsl[0] = (hsl[0] + 8f) % 360f
    hsl[1] = (hsl[1] * .72f).coerceIn(.18f, .38f)
    hsl[2] = (hsl[2] * .92f).coerceIn(.32f, .46f)
    return Color(android.graphics.Color.HSVToColor(hsl))
}

private fun lightScheme(brand: Color, accent: Color) = lightColorScheme(
    primary = readableAccent(brand, Color.White),
    onPrimary = foregroundOn(readableAccent(brand, Color.White)),
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFFF1F5F9),
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF334155),
    tertiary = readableAccent(accent, Color.White),
    onTertiary = foregroundOn(readableAccent(accent, Color.White)),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8FAFC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFEFF3F8),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFDBE3EC),
    error = Color(0xFFB42318),
)

private fun darkScheme(brand: Color, accent: Color) = darkColorScheme(
    primary = readableAccent(brand, Color(0xFF171717)),
    onPrimary = foregroundOn(readableAccent(brand, Color(0xFF171717))),
    primaryContainer = Color(0xFF30343B),
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = Color(0xFF292D33),
    onSecondary = Color(0xFFE2E8F0),
    secondaryContainer = Color(0xFF30343B),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = readableAccent(accent, Color(0xFF171717)),
    onTertiary = foregroundOn(readableAccent(accent, Color(0xFF171717))),
    background = Color(0xFF171717),
    onBackground = Color(0xFFFAFAFA),
    surface = Color(0xFF262626),
    surfaceContainerLowest = Color(0xFF171717),
    surfaceContainerLow = Color(0xFF262626),
    surfaceContainer = Color(0xFF2A2A2A),
    surfaceContainerHigh = Color(0xFF303030),
    surfaceContainerHighest = Color(0xFF383838),
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF303030),
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color(0xFF525252),
    outlineVariant = Color(0xFF404040),
    error = Color(0xFFF87171),
)

@Composable
fun KoraTheme(primaryColor: String? = null, accentColor: String? = null, mode: String? = null, content: @Composable () -> Unit) {
    val brand = tenantColor(primaryColor, KoraTokens.Brand)
    val accent = tenantColor(accentColor, generatedAccent(brand))
    val darkMode = when (mode?.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkMode) darkScheme(brand, accent) else lightScheme(brand, accent),
        typography = KoraTypography,
        content = content,
    )
}
