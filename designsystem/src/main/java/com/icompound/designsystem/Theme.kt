package com.icompound.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


// ------------------------------------------------------------
// Material 3 Color Schemes
// ------------------------------------------------------------

private val LightColorScheme = lightColorScheme(
    primary = ColorLightTokens.Primary,
    onPrimary = ColorLightTokens.OnPrimary,
    primaryContainer = ColorLightTokens.PrimaryContainer,
    onPrimaryContainer = ColorLightTokens.OnPrimaryContainer,

    secondary = ColorLightTokens.Secondary,
    onSecondary = ColorLightTokens.OnSecondary,
    secondaryContainer = ColorLightTokens.SecondaryContainer,
    onSecondaryContainer = ColorLightTokens.OnSecondaryContainer,

    tertiary = ColorLightTokens.Tertiary,
    onTertiary = ColorLightTokens.OnTertiary,
    tertiaryContainer = ColorLightTokens.TertiaryContainer,
    onTertiaryContainer = ColorLightTokens.OnTertiaryContainer,

    error = ColorLightTokens.Error,
    onError = ColorLightTokens.OnError,
    errorContainer = ColorLightTokens.ErrorContainer,
    onErrorContainer = ColorLightTokens.OnErrorContainer,

    background = ColorLightTokens.Background,
    onBackground = ColorLightTokens.OnBackground,

    surface = ColorLightTokens.Surface,
    onSurface = ColorLightTokens.OnSurface,
    surfaceVariant = ColorLightTokens.SurfaceVariant,
    onSurfaceVariant = ColorLightTokens.OnSurfaceVariant,

    outline = ColorLightTokens.Outline,
    outlineVariant = ColorLightTokens.OutlineVariant,
    scrim = ColorLightTokens.Scrim,

    inverseSurface = ColorLightTokens.InverseSurface,
    inverseOnSurface = ColorLightTokens.InverseOnSurface,
    inversePrimary = ColorLightTokens.InversePrimary,

    surfaceTint = ColorLightTokens.Primary,
    surfaceDim = ColorLightTokens.SurfaceDim,
    surfaceBright = ColorLightTokens.SurfaceBright,
    surfaceContainerLowest = ColorLightTokens.SurfaceContainerLowest,
    surfaceContainerLow = ColorLightTokens.SurfaceContainerLow,
    surfaceContainer = ColorLightTokens.SurfaceContainer,
    surfaceContainerHigh = ColorLightTokens.SurfaceContainerHigh,
    surfaceContainerHighest = ColorLightTokens.SurfaceContainerHighest,
)

private val DarkColorScheme = darkColorScheme(
    primary = ColorDarkTokens.Primary,
    onPrimary = ColorDarkTokens.OnPrimary,
    primaryContainer = ColorDarkTokens.PrimaryContainer,
    onPrimaryContainer = ColorDarkTokens.OnPrimaryContainer,

    secondary = ColorDarkTokens.Secondary,
    onSecondary = ColorDarkTokens.OnSecondary,
    secondaryContainer = ColorDarkTokens.SecondaryContainer,
    onSecondaryContainer = ColorDarkTokens.OnSecondaryContainer,

    tertiary = ColorDarkTokens.Tertiary,
    onTertiary = ColorDarkTokens.OnTertiary,
    tertiaryContainer = ColorDarkTokens.TertiaryContainer,
    onTertiaryContainer = ColorDarkTokens.OnTertiaryContainer,

    error = ColorDarkTokens.Error,
    onError = ColorDarkTokens.OnError,
    errorContainer = ColorDarkTokens.ErrorContainer,
    onErrorContainer = ColorDarkTokens.OnErrorContainer,

    background = ColorDarkTokens.Background,
    onBackground = ColorDarkTokens.OnBackground,

    surface = ColorDarkTokens.Surface,
    onSurface = ColorDarkTokens.OnSurface,
    surfaceVariant = ColorDarkTokens.SurfaceVariant,
    onSurfaceVariant = ColorDarkTokens.OnSurfaceVariant,

    outline = ColorDarkTokens.Outline,
    outlineVariant = ColorDarkTokens.OutlineVariant,
    scrim = ColorDarkTokens.Scrim,

    inverseSurface = ColorDarkTokens.InverseSurface,
    inverseOnSurface = ColorDarkTokens.InverseOnSurface,
    inversePrimary = ColorDarkTokens.InversePrimary,

    surfaceTint = ColorDarkTokens.Primary,
    surfaceDim = ColorDarkTokens.SurfaceDim,
    surfaceBright = ColorDarkTokens.SurfaceBright,
    surfaceContainerLowest = ColorDarkTokens.SurfaceContainerLowest,
    surfaceContainerLow = ColorDarkTokens.SurfaceContainerLow,
    surfaceContainer = ColorDarkTokens.SurfaceContainer,
    surfaceContainerHigh = ColorDarkTokens.SurfaceContainerHigh,
    surfaceContainerHighest = ColorDarkTokens.SurfaceContainerHighest,
)

// ------------------------------------------------------------
// Extended Colors
// Custom semantic colors not covered by Material 3.
// ------------------------------------------------------------

data class ICompoundExtendedColors(
    val primaryHover: Color,
    val primaryActive: Color,
    val cardBackground: Color,

    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,

    val border: Color,
    val line: Color,

    val textPrimary: Color,
    val textBold: Color,
    val textRegular: Color,
    val textHint: Color,
    val textHeading: Color,

    val iconColor: Color,
    val iconTint: Color,

    val lightGray: Color,
    val gray: Color,
    val grayBackground: Color,
    val grayBorder: Color,

    val navyBlue: Color,

    val supportiveOrange: Color,
    val orangeBackground: Color,
    val orangeDark: Color,
    val orange: Color,

    val green: Color,
    val greenBackground: Color,
    val completedGreen: Color,
    val secondaryGreen: Color,

    val purple: Color,

    val red: Color,
    val redLight: Color,
    val redDark: Color,
    val redErrorBackground: Color,

    val current: Color,

    val placeholderColor: Color,
    val textFieldColor: Color,
    val textPrimaryGrayColor: Color,
    val textDescriptionColor: Color,

    val darkPrimary: Color,
    val navyDark: Color,
    val colorPrimaryLightAccent: Color,

    val textSecondary: Color,
    val borderLight: Color,
    val greenText: Color,

    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val chipBorder: Color,
)

// ------------------------------------------------------------
// Light Extended Colors
// ------------------------------------------------------------

private val LightExtendedColors = ICompoundExtendedColors(
    primaryHover = ColorLightTokens.PrimaryHover,
    primaryActive = ColorLightTokens.PrimaryActive,
    cardBackground = ColorLightTokens.CardBackground,

    success = ColorLightTokens.Success,
    onSuccess = ColorLightTokens.OnSuccess,
    warning = ColorLightTokens.Warning,
    onWarning = ColorLightTokens.OnWarning,
    info = ColorLightTokens.Info,
    onInfo = ColorLightTokens.OnInfo,

    border = ColorLightTokens.Border,
    line = ColorLightTokens.Line,

    textPrimary = ColorLightTokens.TextPrimary,
    textBold = ColorLightTokens.TextBold,
    textRegular = ColorLightTokens.TextRegular,
    textHint = ColorLightTokens.TextHint,
    textHeading = ColorLightTokens.TextHeading,

    iconColor = ColorLightTokens.IconColor,
    iconTint = ColorLightTokens.IconTint,

    lightGray = ColorLightTokens.LightGray,
    gray = ColorLightTokens.Gray,
    grayBackground = ColorLightTokens.GrayBackground,
    grayBorder = ColorLightTokens.GrayBorder,

    navyBlue = ColorLightTokens.NavyBlue,

    supportiveOrange = ColorLightTokens.SupportiveOrange,
    orangeBackground = ColorLightTokens.OrangeBackground,
    orangeDark = ColorLightTokens.OrangeDark,
    orange = ColorLightTokens.Orange,

    green = ColorLightTokens.Green,
    greenBackground = ColorLightTokens.greenBackground,
    completedGreen = ColorLightTokens.CompletedGreen,
    secondaryGreen = ColorLightTokens.SecondaryGreen,

    purple = ColorLightTokens.Purple,

    red = ColorLightTokens.Red,
    redLight = ColorLightTokens.RedLight,
    redDark = ColorLightTokens.RedDark,
    redErrorBackground = ColorLightTokens.RedErrorBackground,

    current = ColorLightTokens.Current,

    placeholderColor = ColorLightTokens.PlaceholderColor,
    textFieldColor = ColorLightTokens.TextFieldColor,
    textPrimaryGrayColor = ColorLightTokens.TextPrimaryGrayColor,
    textDescriptionColor = ColorLightTokens.TextDescriptionColor,

    darkPrimary = ColorLightTokens.DarkPrimary,
    navyDark = ColorLightTokens.NavyDark,
    colorPrimaryLightAccent = ColorLightTokens.ColorPrimaryLightAccent,

    textSecondary = ColorLightTokens.TextSecondary,
    borderLight = ColorLightTokens.BorderLight,
    greenText = ColorLightTokens.GreenText,

    heroGradientStart = ColorLightTokens.HeroGradientStart,
    heroGradientEnd = ColorLightTokens.HeroGradientEnd,
    chipBorder = ColorLightTokens.ChipBorder,
)

// ------------------------------------------------------------
// Dark Extended Colors
// ------------------------------------------------------------

private val DarkExtendedColors = ICompoundExtendedColors(
    primaryHover = ColorDarkTokens.PrimaryHover,
    primaryActive = ColorDarkTokens.PrimaryActive,
    cardBackground = ColorDarkTokens.CardBackground,

    success = ColorDarkTokens.Success,
    onSuccess = ColorDarkTokens.OnSuccess,
    warning = ColorDarkTokens.Warning,
    onWarning = ColorDarkTokens.OnWarning,
    info = ColorDarkTokens.Info,
    onInfo = ColorDarkTokens.OnInfo,

    border = ColorDarkTokens.Border,
    line = ColorDarkTokens.Line,

    textPrimary = ColorDarkTokens.TextPrimary,
    textBold = ColorDarkTokens.TextBold,
    textRegular = ColorDarkTokens.TextRegular,
    textHint = ColorDarkTokens.TextHint,
    textHeading = ColorDarkTokens.TextHeading,

    iconColor = ColorDarkTokens.IconColor,
    iconTint = ColorDarkTokens.IconTint,

    lightGray = ColorDarkTokens.LightGray,
    gray = ColorDarkTokens.Gray,
    grayBackground = ColorDarkTokens.SurfaceVariant,
    grayBorder = ColorDarkTokens.GrayBorder,

    navyBlue = ColorDarkTokens.NavyBlue,

    supportiveOrange = ColorDarkTokens.SupportiveOrange,
    orangeBackground = ColorDarkTokens.Warning,
    orangeDark = ColorDarkTokens.Warning,
    orange = ColorDarkTokens.Orange,

    green = ColorDarkTokens.Green,
    greenBackground = ColorDarkTokens.Success,
    completedGreen = ColorDarkTokens.CompletedGreen,
    secondaryGreen = ColorDarkTokens.SecondaryGreen,

    purple = ColorDarkTokens.Purple,

    red = ColorDarkTokens.Red,
    redLight = ColorDarkTokens.Error,
    redDark = ColorDarkTokens.Error,
    redErrorBackground = ColorDarkTokens.ErrorContainer,

    current = ColorDarkTokens.Current,

    placeholderColor = ColorDarkTokens.PlaceholderColor,
    textFieldColor = ColorDarkTokens.TextFieldColor,
    textPrimaryGrayColor = ColorDarkTokens.TextPrimaryGrayColor,
    textDescriptionColor = ColorDarkTokens.TextDescriptionColor,

    darkPrimary = ColorDarkTokens.DarkPrimary,
    navyDark = ColorDarkTokens.NavyDark,
    colorPrimaryLightAccent = ColorDarkTokens.PrimaryContainer,

    textSecondary = ColorDarkTokens.TextSecondary,
    borderLight = ColorDarkTokens.Border,
    greenText = ColorDarkTokens.Success,

    heroGradientStart = ColorDarkTokens.HeroGradientStart,
    heroGradientEnd = ColorDarkTokens.HeroGradientEnd,
    chipBorder = ColorDarkTokens.ChipBorder,
)


private val LocalICompoundColors =
    staticCompositionLocalOf { LightExtendedColors }
/**
 * Access extended colors from the current theme
 */
object ICompoundTheme {

    val colors: ICompoundExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalICompoundColors.current

    val colorScheme: ColorScheme                      // new
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val spacing: KaiTechAppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalKaiTechAppSpacing.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: KaiTechAppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalKaiTechAppShapes.current

    val radius: KaiTechAppRadius
        @Composable
        @ReadOnlyComposable
        get() = LocalKaiTechAppRadius.current

    val icons: KaiTechAppIcons
        @Composable
        @ReadOnlyComposable
        get() = LocalKaiTechAppIcons.current
}




// ------------------------------------------------------------
// Main Theme
// ------------------------------------------------------------

@Composable
fun ICompoundTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val extendedColors = if (darkTheme) {
        DarkExtendedColors
    } else {
        LightExtendedColors
    }

    CompositionLocalProvider(
        LocalICompoundColors provides extendedColors,
        LocalKaiTechAppSpacing provides KaiTechSpacing,
        LocalKaiTechAppShapes provides KaiTechShapes,
        LocalKaiTechAppRadius provides KaiTechRadius,
        LocalKaiTechAppIcons provides KaiTechAppIcons,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KaiTechTypography,
            content = content,
        )
    }
}