package dev.faizksen.kursor.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class KursorColors(
    val rateUp: Color,
    val rateUpContainer: Color,
    val rateDown: Color,
    val rateDownContainer: Color,
    val stale: Color,
    val staleContainer: Color,
)

internal val LightKursorColors = KursorColors(
    rateUp = Green,
    rateUpContainer = MintLight,
    rateDown = Red,
    rateDownContainer = DustyRose,
    stale = BrownMustard,
    staleContainer = Beige,
)

internal val DarkKursorColors = KursorColors(
    rateUp = GreenLight,
    rateUpContainer = GreenDark,
    rateDown = RedLight,
    rateDownContainer = BrownDark,
    stale = Mustard,
    staleContainer = WoodBark,
)

internal val LocalKursorColors = staticCompositionLocalOf { LightKursorColors }

internal val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = White,
    primaryContainer = MintLight,
    onPrimaryContainer = Green,
    inversePrimary = GreenLight,
    secondary = Green,
    onSecondary = White,
    secondaryContainer = MintLight,
    onSecondaryContainer = Green,
    tertiary = BrownMustard,
    onTertiary = White,
    tertiaryContainer = Beige,
    onTertiaryContainer = BrownMustard,
    background = Alabaster,
    onBackground = Onyx,
    surface = White,
    onSurface = Onyx,
    surfaceVariant = WhiteSand,
    onSurfaceVariant = DirtyGrey,
    surfaceTint = Green,
    inverseSurface = Onyx,
    inverseOnSurface = PlatinumGrey,
    error = Red,
    onError = White,
    errorContainer = DustyRose,
    onErrorContainer = Red,
    outline = DirtyGrey,
    outlineVariant = FoggyDay,
    surfaceBright = White,
    surfaceDim = WhiteSand,
    surfaceContainerLowest = White,
    surfaceContainerLow = White,
    surfaceContainer = White,
    surfaceContainerHigh = WhiteSand,
    surfaceContainerHighest = WhiteSand,
)

internal val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = GreenLight,
    onPrimary = GreenBlack,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenLight,
    inversePrimary = Green,
    secondary = GreenLight,
    onSecondary = GreenBlack,
    secondaryContainer = GreenDark,
    onSecondaryContainer = GreenLight,
    tertiary = Mustard,
    onTertiary = GreenBlack,
    tertiaryContainer = WoodBark,
    onTertiaryContainer = Mustard,
    background = Charcoal,
    onBackground = PlatinumGrey,
    surface = CocosBlack,
    onSurface = PlatinumGrey,
    surfaceVariant = Coal,
    onSurfaceVariant = PaleOlive,
    surfaceTint = GreenLight,
    inverseSurface = PlatinumGrey,
    inverseOnSurface = Onyx,
    error = RedLight,
    onError = GreenBlack,
    errorContainer = BrownDark,
    onErrorContainer = RedLight,
    outline = PaleOlive,
    outlineVariant = DarkKhaki,
    surfaceBright = Coal,
    surfaceDim = Charcoal,
    surfaceContainerLowest = Charcoal,
    surfaceContainerLow = CocosBlack,
    surfaceContainer = CocosBlack,
    surfaceContainerHigh = Coal,
    surfaceContainerHighest = Coal,
)
