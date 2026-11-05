package com.kora.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.ExperimentalTextApi
import com.kora.mobile.R
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Kora MVP's neutral surfaces, shared typography rhythm, and tenant brand accent. */
object KoraTokens {
    // Shared design contract from the mobile reference. Tenant branding may
    // replace Brand/Accent, while the neutral palette remains consistent.
    val Brand = Color(0xFF334155)
    val Accent = Color(0xFF334155)
    val Ink = Color(0xFF16191D)
    val MutedInk = Color(0xFF667080)
    val Canvas = Color(0xFFF6F7F9)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSoft = Color(0xFFE8EDF4)
    val Border = Color(0xFFE3E6EA)
    val BorderStrong = Color(0xFFCFD4DB)
    val Danger = Color(0xFFC9302C)
    val Success = Color(0xFF1D8A5A)
    val Warning = Color(0xFFB8791F)
    val WarningBg = Color(0xFFFBEFD8)
    val WarningText = Color(0xFF8A5200)
    val WarningBgDark = Color(0xFF49391F)
    val WarningTextDark = Color(0xFFFFD58B)
    val WarningBorder = Color(0xFFE8C98F)
    val WarningBorderDark = Color(0xFF806334)
    val SuccessTextDark = Color(0xFF61C995)
    val MascotAntennaReady = Color(0xFFE8A64A)
    val MascotOffline = Color(0xFF9AA3B0)
    val MascotCheek = Color(0xFFF4B9A8)
    val Info = Color(0xFF0369A1)
    val Scrim = Color(0x800A0C0E)

    val SpaceXs: Dp = 4.dp
    val SpaceSm: Dp = 6.dp
    val SpaceS: Dp = 8.dp
    val SpaceM: Dp = 12.dp
    val SpaceL: Dp = 16.dp
    val SpaceXl: Dp = 24.dp
    val SpaceXxl: Dp = 32.dp
    val FieldLabelGap: Dp = 6.dp
    val CardGap: Dp = 10.dp
    val CardPaddingHorizontal: Dp = 14.dp
    val CardPaddingVertical: Dp = 12.dp
    val TagRadius: Dp = 4.dp
    val TagPaddingHorizontal: Dp = 8.dp
    val TagPaddingVertical: Dp = 1.dp
    val Space1: Dp = SpaceXs
    val Space2: Dp = SpaceS
    val Space3: Dp = SpaceM
    val Space4: Dp = SpaceL
    val Space5: Dp = SpaceXl
    val Space6: Dp = SpaceXxl

    val TopBar: Dp = 56.dp
    val BottomBar: Dp = 64.dp
    val TouchTarget: Dp = 44.dp
    val FieldHeight: Dp = 48.dp
    val ButtonHeight: Dp = 48.dp
    val SecondaryButtonHeight: Dp = 44.dp
    val CompactButtonHeight: Dp = 40.dp
    val ChipHeight: Dp = 36.dp
    val AskButton: Dp = 52.dp
    val StatusDot: Dp = 10.dp
    val StatusHalo: Dp = 16.dp
    val Hairline: Dp = 1.dp
    val FocusRing: Dp = 2.dp
    val RadiusControl: Dp = 10.dp
    val RadiusField: Dp = 10.dp
    val RadiusCard: Dp = 12.dp
    val RadiusSheet: Dp = 18.dp
}



/** Central measurement scale for Compose screens. Use semantic KoraTokens aliases for the shared scale; keep screen-specific dimensions here. */
object KoraMeasures {
    val dp0_5 = 0.5.dp
    val dp0 = 0.dp
    val dp1 = KoraTokens.Hairline
    val dp2 = KoraTokens.FocusRing
    val dp3 = 3.dp
    val dp4 = KoraTokens.SpaceXs
    val dp5 = 5.dp
    val dp6 = KoraTokens.SpaceSm
    val dp7 = 7.dp
    val dp8 = KoraTokens.SpaceS
    val dp9 = 9.dp
    val dp10 = KoraTokens.CardGap
    val dp11 = 11.dp
    val dp12 = KoraTokens.SpaceM
    val dp13 = 13.dp
    val dp14 = KoraTokens.CardPaddingHorizontal
    val dp15 = 15.dp
    val dp16 = KoraTokens.SpaceL
    val dp17 = 17.dp
    val dp18 = 18.dp
    val dp19 = 19.dp
    val dp20 = 20.dp
    val dp22 = 22.dp
    val dp24 = KoraTokens.SpaceXl
    val dp26 = 26.dp
    val dp28 = 28.dp
    val dp30 = 30.dp
    val dp32 = 32.dp
    val dp34 = 34.dp
    val dp36 = 36.dp
    val dp38 = 38.dp
    val dp40 = KoraTokens.CompactButtonHeight
    val dp42 = 42.dp
    val dp44 = KoraTokens.TouchTarget
    val dp46 = 46.dp
    val dp48 = KoraTokens.FieldHeight
    val dp52 = KoraTokens.AskButton
    val dp54 = 54.dp
    val dp60 = 60.dp
    val dp62 = 62.dp
    val dp64 = KoraTokens.BottomBar
    val dp72 = 72.dp
    val dp76 = 76.dp
    val dp84 = 84.dp
    val dp96 = 96.dp
    val dp104 = 104.dp
    val dp140 = 140.dp
    val dp160 = 160.dp
    val dp164 = 164.dp
    val dp180 = 180.dp
    val dp200 = 200.dp
    val dp312 = 312.dp
    val dp400 = 400.dp
    val dp440 = 440.dp
    val dp450 = 450.dp
    val dp460 = 460.dp
    val dp480 = 480.dp
    val dp500 = 500.dp
    val dp520 = 520.dp
    val dp600 = 600.dp
    val dp620 = 620.dp
    val dp680 = 680.dp
    val dp720 = 720.dp
    val sp1_8 = 1.8.sp
    val sp10 = 10.sp
    val sp11 = 11.sp
    val sp12 = 12.sp
    val sp12_5 = 12.5.sp
    val sp13 = 13.sp
    val sp17 = 17.sp
    val sp18 = 18.sp
    val sp19 = 19.sp
    val sp20 = 20.sp
    val sp22 = 22.sp
    val sp23 = 23.sp
    val sp24 = 24.sp
    val sp25 = 25.sp
    val sp26 = 26.sp
    val sp28 = 28.sp
    val sp29 = 29.sp
}

/** Named brand palettes are design presets; a tenant's saved colors remain the source of truth. */
data class KoraPaletteFamily(
    val name: String,
    val primaryHex: String,
    val accentHex: String,
    val preview: List<Color>,
)

object KoraPaletteFamilies {
    val choices = listOf(
        KoraPaletteFamily("Slate", "#334155", "#0F766E", listOf(Color(0xFF334155), Color(0xFF5E718B), Color(0xFF919AA9), Color(0xFF1D8A5A), Color(0xFFB8791F))),
        KoraPaletteFamily("Ocean", "#1D4ED8", "#0891B2", listOf(Color(0xFF1D4ED8), Color(0xFF0891B2), Color(0xFF94A3B8), Color(0xFF059669), Color(0xFFD97706))),
        KoraPaletteFamily("Forest", "#166534", "#0F766E", listOf(Color(0xFF166534), Color(0xFF0F766E), Color(0xFF94A3B8), Color(0xFF15803D), Color(0xFFB45309))),
        KoraPaletteFamily("Plum", "#6D28D9", "#A21CAF", listOf(Color(0xFF6D28D9), Color(0xFFA21CAF), Color(0xFFA1A1AA), Color(0xFF059669), Color(0xFFB45309))),
    )

    fun find(primary: String?): KoraPaletteFamily? = choices.firstOrNull {
        it.primaryHex.equals(primary?.trim(), ignoreCase = true)
    }
}

/** Dark counterpart of the shared palette; tenant mode selects the palette, never individual screens. */
private object KoraDarkTokens {
    val Brand = Color(0xFFCBD5E1)
    val Ink = Color(0xFFF4F5F6)
    val MutedInk = Color(0xFFB0B6C0)
    val Canvas = Color(0xFF171819)
    val Surface = Color(0xFF222426)
    val SurfaceSoft = Color(0xFF30343B)
    val Border = Color(0xFF3D4147)
    val BorderStrong = Color(0xFF535861)
    val Success = KoraTokens.SuccessTextDark
    val Warning = Color(0xFFE8B45B)
    val WarningBg = KoraTokens.WarningBgDark
    val WarningText = KoraTokens.WarningTextDark
    val Danger = Color(0xFFFF7772)
    val SelectedBg = Color(0xFF35404F)
    val Scrim = Color(0x99000000)
}

val KoraShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(18.dp),
)

@OptIn(ExperimentalTextApi::class)
private val KoraFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

private val KoraTypography = Typography(
    headlineSmall = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontFamily = KoraFontFamily),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontFamily = KoraFontFamily),
    titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontFamily = KoraFontFamily),
    titleSmall = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontFamily = KoraFontFamily),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontFamily = KoraFontFamily),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontFamily = KoraFontFamily),
    bodySmall = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontFamily = KoraFontFamily),
    labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontFamily = KoraFontFamily),
    labelMedium = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontFamily = KoraFontFamily),
    labelSmall = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontFamily = KoraFontFamily),
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
    primaryContainer = KoraTokens.SurfaceSoft,
    onPrimaryContainer = KoraTokens.Ink,
    secondary = KoraTokens.Canvas,
    onSecondary = KoraTokens.Ink,
    secondaryContainer = KoraTokens.SurfaceSoft,
    onSecondaryContainer = KoraTokens.Brand,
    tertiary = readableAccent(accent, Color.White),
    onTertiary = foregroundOn(readableAccent(accent, Color.White)),
    background = KoraTokens.Canvas,
    onBackground = KoraTokens.Ink,
    surface = KoraTokens.Surface,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFBFC),
    surfaceContainer = KoraTokens.Canvas,
    surfaceContainerHigh = Color(0xFFEFF1F4),
    surfaceContainerHighest = KoraTokens.SurfaceSoft,
    onSurface = KoraTokens.Ink,
    surfaceVariant = KoraTokens.Canvas,
    onSurfaceVariant = KoraTokens.MutedInk,
    outline = KoraTokens.BorderStrong,
    outlineVariant = KoraTokens.Border,
    error = KoraTokens.Danger,
)

private fun darkScheme(brand: Color, accent: Color) = darkColorScheme(
    primary = readableAccent(brand, KoraDarkTokens.Surface),
    onPrimary = foregroundOn(readableAccent(brand, KoraDarkTokens.Surface)),
    primaryContainer = KoraDarkTokens.SelectedBg,
    onPrimaryContainer = KoraDarkTokens.Ink,
    secondary = KoraDarkTokens.SurfaceSoft,
    onSecondary = KoraDarkTokens.Ink,
    secondaryContainer = KoraDarkTokens.SelectedBg,
    onSecondaryContainer = KoraDarkTokens.Ink,
    tertiary = readableAccent(accent, KoraDarkTokens.Surface),
    onTertiary = foregroundOn(readableAccent(accent, KoraDarkTokens.Surface)),
    background = KoraDarkTokens.Canvas,
    onBackground = KoraDarkTokens.Ink,
    surface = KoraDarkTokens.Surface,
    surfaceContainerLowest = KoraDarkTokens.Canvas,
    surfaceContainerLow = KoraDarkTokens.Surface,
    surfaceContainer = Color(0xFF272A2D),
    surfaceContainerHigh = KoraDarkTokens.SurfaceSoft,
    surfaceContainerHighest = Color(0xFF383D44),
    onSurface = KoraDarkTokens.Ink,
    surfaceVariant = KoraDarkTokens.SurfaceSoft,
    onSurfaceVariant = KoraDarkTokens.MutedInk,
    outline = KoraDarkTokens.BorderStrong,
    outlineVariant = KoraDarkTokens.Border,
    error = KoraDarkTokens.Danger,
)

@Composable
fun KoraTheme(primaryColor: String? = null, accentColor: String? = null, mode: String? = null, content: @Composable () -> Unit) {
    val brand = tenantColor(primaryColor, KoraTokens.Brand)
    val accent = tenantColor(accentColor, generatedAccent(brand))
    val darkMode = when (mode?.lowercase()) {
        "dark" -> true
        "light" -> false
        "system" -> isSystemInDarkTheme()
        else -> false
    }
    MaterialTheme(
        colorScheme = if (darkMode) darkScheme(brand, accent) else lightScheme(brand, accent),
        typography = KoraTypography,
        shapes = KoraShapes,
        content = content,
    )
}
