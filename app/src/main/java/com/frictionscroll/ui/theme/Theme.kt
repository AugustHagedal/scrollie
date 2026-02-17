package com.frictionscroll.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WarmCream = Color(0xFFF5EDE0)
private val WarmBeige = Color(0xFFE8DCC8)
private val Taupe = Color(0xFF8B7D6E)
private val DarkTaupe = Color(0xFF5C524A)
private val MutedSage = Color(0xFF9CAF94)
private val SoftRose = Color(0xFFC4A4A0)
private val OffWhite = Color(0xFFFAF7F2)
private val MutedRed = Color(0xFFB85C5C)

private val WarmDarkBrown = Color(0xFF2C2622)
private val WarmDarkSurface = Color(0xFF3D352E)

private val LightColorScheme = lightColorScheme(
    primary = Taupe,
    onPrimary = OffWhite,
    secondary = MutedSage,
    onSecondary = OffWhite,
    tertiary = SoftRose,
    onTertiary = OffWhite,
    background = WarmCream,
    onBackground = DarkTaupe,
    surface = WarmBeige,
    onSurface = DarkTaupe,
    surfaceVariant = WarmBeige,
    onSurfaceVariant = Taupe,
    error = MutedRed,
    onError = OffWhite,
)

private val DarkColorScheme = darkColorScheme(
    primary = Taupe,
    onPrimary = OffWhite,
    secondary = MutedSage,
    onSecondary = OffWhite,
    tertiary = SoftRose,
    onTertiary = OffWhite,
    background = WarmDarkBrown,
    onBackground = WarmCream,
    surface = WarmDarkSurface,
    onSurface = WarmCream,
    surfaceVariant = WarmDarkSurface,
    onSurfaceVariant = WarmBeige,
    error = MutedRed,
    onError = OffWhite,
)

@Composable
fun FrictionScrollTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
