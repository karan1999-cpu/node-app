package com.cybersync.nodd.ui.theme

import android.content.res.AssetManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Same palette as the plan document: cobalt for actions, sun yellow for the "nod", cool paper neutrals.
private val LightColors = lightColorScheme(
    primary = Color(0xFF3242E6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1E4FD),
    onPrimaryContainer = Color(0xFF1E2A9E),
    secondary = Color(0xFFFFC83D),
    onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFFFFF3CC),
    onSecondaryContainer = Color(0xFF4A3500),
    tertiary = Color(0xFF0E7F57),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDCF2E8),
    onTertiaryContainer = Color(0xFF0B5E41),
    background = Color(0xFFF1F2F7),
    onBackground = Color(0xFF141833),
    surface = Color(0xFFF1F2F7),
    onSurface = Color(0xFF141833),
    surfaceVariant = Color(0xFFE6E8F2),
    onSurfaceVariant = Color(0xFF575D7C),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFE6E8F2),
    outline = Color(0xFFB9BDD0),
    outlineVariant = Color(0xFFD6D9E6),
    error = Color(0xFFB42E2B),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFBE2E1),
    onErrorContainer = Color(0xFF8C1F1C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8491FF),
    onPrimary = Color(0xFF0C0F1E),
    primaryContainer = Color(0xFF212A5E),
    onPrimaryContainer = Color(0xFFC9CEFF),
    secondary = Color(0xFFFFD25A),
    onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFF3A3013),
    onSecondaryContainer = Color(0xFFFFE7A3),
    tertiary = Color(0xFF58CD9E),
    onTertiary = Color(0xFF0C0F1E),
    tertiaryContainer = Color(0xFF123527),
    onTertiaryContainer = Color(0xFF9FE5C6),
    background = Color(0xFF0C0F1E),
    onBackground = Color(0xFFE8EAF6),
    surface = Color(0xFF0C0F1E),
    onSurface = Color(0xFFE8EAF6),
    surfaceVariant = Color(0xFF1C2140),
    onSurfaceVariant = Color(0xFF9DA3C3),
    surfaceContainerLowest = Color(0xFF151931),
    surfaceContainerLow = Color(0xFF151931),
    surfaceContainer = Color(0xFF151931),
    surfaceContainerHigh = Color(0xFF1C2140),
    surfaceContainerHighest = Color(0xFF1C2140),
    outline = Color(0xFF4A5180),
    outlineVariant = Color(0xFF2A3053),
    error = Color(0xFFFF8A82),
    onError = Color(0xFF3F1A1C),
    errorContainer = Color(0xFF3F1A1C),
    onErrorContainer = Color(0xFFFFB4AE),
)

/** QR codes are always dark modules on white so every camera can read them, in either theme. */
val QrInk = Color(0xFF10132B)

private val NoddShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private fun noddTypography(assets: AssetManager): Typography {
    fun family(file: String, vararg weights: FontWeight) =
        FontFamily(weights.map { Font("fonts/$file", assets, it) })

    val display = family("BricolageGrotesque.ttf", FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)
    val body = family("Figtree.ttf", FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)
    val mono = family("JetBrainsMono.ttf", FontWeight.SemiBold)

    return Typography(
        displayMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-1.4).sp),
        headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 27.sp, letterSpacing = (-0.2).sp),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 25.sp),
        titleMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.9.sp),
    )
}

@Composable
fun NoddTheme(content: @Composable () -> Unit) {
    val assets = LocalContext.current.assets
    val typography = remember(assets) { noddTypography(assets) }
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = typography,
        shapes = NoddShapes,
        content = content,
    )
}
