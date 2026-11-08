package com.icompound.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * ICompound color tokens, Material 3 shape.
 * Source: ICompound Design System.dc.html / ICompound Design System (Markdown)
 *
 * Every value below traces to a token in the source doc (§1 Brand, §2 Color,
 * §7 App shell). Where the requested structure has a field with no ICompound
 * equivalent, it's mapped to the closest existing token and flagged inline —
 * nothing here is an invented brand color.
 */

//helper functions that allow to access colors without writing the full expression.
//ex  MaterialTheme.colorScheme.primary == colorPrimary()

@Composable
fun colorPrimary() = MaterialTheme.colorScheme.primary

@Composable
fun colorText() = MaterialTheme.colorScheme.onBackground

@Composable
fun colorTextField() = MaterialTheme.colorScheme.onSurface

@Composable
fun colorTextHint() = ColorLightTokens.PlaceholderColor

@Composable
fun colorWhite() = Color.White

object ColorLightTokens {
    // Primary — Action blue (§1 Brand)
    val Primary = Color(0xFF1F4263)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD2E9F9)
    val OnPrimaryContainer = Color(0xFF001F2E)
    val PrimaryHover = Color(0xFF015A95)   // §1 "Action · hover, shade 15%"
    val PrimaryActive = Color(0xFF014876)  // pressed state, one tonal step darker

    // Secondary — muted blue-grey (no second brand hue in source; derived from neutral scale)
    val Secondary = Color(0xFF142B40)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFF1F4F5)
    val OnSecondaryContainer = Color(0xFF0C1B24)

    // Tertiary — Accent teal (§1 Brand: "active nav, highlights, selection")
    val Tertiary = Color(0xFF145A66)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFEEF3F7)
    val OnTertiaryContainer = Color(0xFF00201F)

    // Backgrounds (§2 "Surface & text tokens")
    val Background = Color(0xFFF8FAFB) // "Canvas / body"
    val OnBackground = Color(0xFF212529)

    // Surfaces
    val Surface = Color(0xFFFFFFFF) // "Surface — Cards, topbar, sidebar-content"
    val OnSurface = Color(0xFF212529)
    val SurfaceVariant = Color(0xFFEFF2F7) // "Border — Card & divider lines"
    val OnSurfaceVariant = Color(0xFF495057)
    val CardBackground = Color(0xFFFFFFFF) // cards render on Surface per §2

    val InverseSurface = Color(0xFF021528) // "Sidebar navy" — the app's one dark surface
    val InverseOnSurface = Color(0xFFF8F8FB)
    val InversePrimary = Color(0xFFA3D4F2)

    // Semantic Colors (§2 "Semantic / status")
    val Error = Color(0xFFB42318)          // deepened from brand danger #f46a6a for AA contrast
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    val Success = Color(0xFFB8E3C8) // §2 "$success"
    val OnSuccess = Color(0xFF216744)

    val Warning = Color(0xFFF1B44C) // §2 "$warning"
    val OnWarning = Color(0xFF2A2D2D) // dark text required for contrast on gold

    val Info = Color(0xFF50A5F1) // §2 "$info"
    val OnInfo = Color(0xFFFFFFFF)

    // Borders & Lines
    val Outline = Color(0xFF74788D)       // neutral-600
    val OutlineVariant = Color(0xFFCED4DA) // neutral-400 / "Border · strong"
    val Border = Color(0xFFB9B9B9)         // §2 "Border" — card & divider lines
    val Line = Color(0xFFEFF2F7)
    val Scrim = Color(0x73021528)          // §6 modal scrim: rgba(2,21,40,.45), navy-tinted not black

    // Surface containers (Material3 1.1+ tonal scale)
    val SurfaceBright = Color(0xFFFFFFFF)
    val SurfaceDim = Color(0xFFD8DAE0)
    val SurfaceContainer = Color(0xFFF2F3F7)
    val SurfaceContainerLow = Color(0xFFF8F8FB)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerHigh = Color(0xFFECEEF3)
    val SurfaceContainerHighest = Color(0xFFE6E8EE)

    // Text Colors (§3 Typography + §2 "Surface & text tokens")
    val TextPrimary = Color(0xFF212529)   // "Text · heading"
    val TextBold = Color(0xFF212529)      // Poppins 600 headings
    val TextRegular = Color(0xFF495057)   // "Text · body"
    val TextHint = Color(0xFF74788D)       // "Text · muted"
    val TextHeading = Color(0xFF0B1F33)   // §2: "often #0b1f33 for display"

    // Icon Colors (§7 App shell: sidebar icon states)
    val IconColor = Color(0xFF6A7187) // sidebar inactive icon
    val IconTint = Color(0xFFADB5BD)  // neutral-500, lighter icon tint

    val GreyBackground = Color(0xFFADB5BD)  // neutral-500, lighter icon tint
    val LightGray = Color(0xFFF2F2F7) // neutral-400
    val Gray = Color(0xFF8E8E93)
    val GrayBorder = Color(0xFFF0F0F0)

    val GrayBackground = Color(0xFFC7C7CC)

    // Additional brand accents
    // NOTE: none of these six exist as named tokens in the KaiTech doc. Mapped
    // to the closest real KaiTech token for backward-compatible naming only.
    val NavyBlue = Color(0xFF021528)          // = InverseSurface (sidebar navy)
    val SupportiveOrange = Color(0xFFF1B44C)  // = Warning

    val OrangeBackground = Color(0xFFFFC693)            // = Warning
    val OrangeDark = Color(0xFF8D2833)            // = Warning
    val Orange = Color(0xFFFF8D28)            // = Warning
    val Green = Color(0xFF00A09C)             // = Success
    val greenBackground = Color(0x9C00A0A0)             // = Success
    val CompletedGreen = Color(0xFF1F9468)    // §6 soft-pill "Active" text tone
    val SecondaryGreen = Color(0xFF34C38F)    // = Success
    val Purple = Color(0xFF543592)            // not in KaiTech spec — no source token; kept as-is if used elsewhere
    val Red = Color(0xE1FFB2B2)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedLight = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedDark = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedErrorBackground = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val Current = Color(0xFFF1B44C)           // = Warning

    // Legacy placeholder color
    val PlaceholderColor = Color(0xFF686868) // = TextHint
    val TextFieldColor = Color.White   // §6 Forms: disabled input bg
    val TextPrimaryGrayColor = Color(0xFF6D7682)
    val TextDescriptionColor = Color(0xFF404750)
    val DarkPrimary = Color(0xFF00598F)
    val NavyDark = Color(0xFF111C2C)
    val ColorPrimaryLightAccent = Color(0xFFF0F3FF)
    val TextSecondary = Color(0xFF434654)
    val BorderLight = Color(0xFFE3E6D6)
    val GreenText = Color(0xFF137333)

    // Hero banner gradient (enroll/diploma details hero cards)
    val HeroGradientStart = Color(0xFF04385E)
    val HeroGradientEnd = Color(0xFF0170BA)
    val ChipBorder = Color(0x33FFFFFF)

    val OnMedia = Color(0xFFFFFFFF)

    val OnMediaMuted = Color(0xE6FFFFFF)// 90%

    val MediaScrim = Color(0xFF000000)// base of image gradients

    val MediaGlass = Color(0x1FFFFFFF)// 12%, tile fill

    val MediaGlassBorder = Color(0x2EFFFFFF)      // 18%

    val MediaGlassChip = Color(0x26FFFFFF)        // 15%, icon chip behind tile icons

    val MediaIndicatorInactive = Color(0xFFFFFFFF)

    val MediaButtonContainer = Color(0xFFFFFFFF)
}


object ColorDarkTokens  {
    // Primary — Action blue (§1 Brand)
    val Primary = Color(0xFF1F4263)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFFD2E9F9)
    val OnPrimaryContainer = Color(0xFF001F2E)
    val PrimaryHover = Color(0xFF015A95)   // §1 "Action · hover, shade 15%"
    val PrimaryActive = Color(0xFF014876)  // pressed state, one tonal step darker

    // Secondary — muted blue-grey (no second brand hue in source; derived from neutral scale)
    val Secondary = Color(0xFF142B40)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFF1F4F5)
    val OnSecondaryContainer = Color(0xFF0C1B24)

    // Tertiary — Accent teal (§1 Brand: "active nav, highlights, selection")
    val Tertiary = Color(0xFF145A66)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFFEEF3F7)
    val OnTertiaryContainer = Color(0xFF00201F)

    // Backgrounds (§2 "Surface & text tokens")
    val Background = Color(0xFF080D24) // "Canvas / body"
    val OnBackground = Color(0xFF212529)

    // Surfaces
    val Surface = Color(0xFFFFFFFF) // "Surface — Cards, topbar, sidebar-content"
    val OnSurface = Color(0xFF212529)
    val SurfaceVariant = Color(0xFFEFF2F7) // "Border — Card & divider lines"
    val OnSurfaceVariant = Color(0xFF495057)
    val CardBackground = Color(0xFFFFFFFF) // cards render on Surface per §2

    val InverseSurface = Color(0xFF021528) // "Sidebar navy" — the app's one dark surface
    val InverseOnSurface = Color(0xFFF8F8FB)
    val InversePrimary = Color(0xFFA3D4F2)

    // Semantic Colors (§2 "Semantic / status")
    val Error = Color(0xFFB42318)          // deepened from brand danger #f46a6a for AA contrast
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    val Success = Color(0xFFB8E3C8) // §2 "$success"
    val OnSuccess = Color(0xFF216744)

    val Warning = Color(0xFFF1B44C) // §2 "$warning"
    val OnWarning = Color(0xFF2A2D2D) // dark text required for contrast on gold

    val Info = Color(0xFF50A5F1) // §2 "$info"
    val OnInfo = Color(0xFFFFFFFF)

    // Borders & Lines
    val Outline = Color(0xFF74788D)       // neutral-600
    val OutlineVariant = Color(0xFFCED4DA) // neutral-400 / "Border · strong"
    val Border = Color(0xFFB9B9B9)         // §2 "Border" — card & divider lines
    val Line = Color(0xFFEFF2F7)
    val Scrim = Color(0x73021528)          // §6 modal scrim: rgba(2,21,40,.45), navy-tinted not black

    // Surface containers (Material3 1.1+ tonal scale)
    val SurfaceBright = Color(0xFFFFFFFF)
    val SurfaceDim = Color(0xFFD8DAE0)
    val SurfaceContainer = Color(0xFFF2F3F7)
    val SurfaceContainerLow = Color(0xFFF8F8FB)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerHigh = Color(0xFFECEEF3)
    val SurfaceContainerHighest = Color(0xFFE6E8EE)

    // Text Colors (§3 Typography + §2 "Surface & text tokens")
    val TextPrimary = Color(0xFF212529)   // "Text · heading"
    val TextBold = Color(0xFF212529)      // Poppins 600 headings
    val TextRegular = Color(0xFF495057)   // "Text · body"
    val TextHint = Color(0xFF74788D)       // "Text · muted"
    val TextHeading = Color(0xFF0B1F33)   // §2: "often #0b1f33 for display"

    // Icon Colors (§7 App shell: sidebar icon states)
    val IconColor = Color(0xFF6A7187) // sidebar inactive icon
    val IconTint = Color(0xFFADB5BD)  // neutral-500, lighter icon tint

    val GreyBackground = Color(0xFFADB5BD)  // neutral-500, lighter icon tint
    val LightGray = Color(0xFFF2F2F7) // neutral-400
    val Gray = Color(0xFF8E8E93)
    val GrayBorder = Color(0xFFF0F0F0)

    val GrayBackground = Color(0xFFC7C7CC)

    // Additional brand accents
    // NOTE: none of these six exist as named tokens in the KaiTech doc. Mapped
    // to the closest real KaiTech token for backward-compatible naming only.
    val NavyBlue = Color(0xFF021528)          // = InverseSurface (sidebar navy)
    val SupportiveOrange = Color(0xFFF1B44C)  // = Warning

    val OrangeBackground = Color(0xFFFFC693)            // = Warning
    val OrangeDark = Color(0xFF8D2833)            // = Warning
    val Orange = Color(0xFFFF8D28)            // = Warning
    val Green = Color(0xFF00A09C)             // = Success
    val greenBackground = Color(0x9C00A0A0)             // = Success
    val CompletedGreen = Color(0xFF1F9468)    // §6 soft-pill "Active" text tone
    val SecondaryGreen = Color(0xFF34C38F)    // = Success
    val Purple = Color(0xFF543592)            // not in KaiTech spec — no source token; kept as-is if used elsewhere
    val Red = Color(0xE1FFB2B2)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedLight = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedDark = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val RedErrorBackground = Color(0xFFFF383C)               // brand "danger" base (soft-pill tone, not the M3 Error role)
    val Current = Color(0xFFF1B44C)           // = Warning

    // Legacy placeholder color
    val PlaceholderColor = Color(0xFF686868) // = TextHint
    val TextFieldColor = Color.White   // §6 Forms: disabled input bg
    val TextPrimaryGrayColor = Color(0xFF6D7682)
    val TextDescriptionColor = Color(0xFF404750)
    val DarkPrimary = Color(0xFF00598F)
    val NavyDark = Color(0xFF111C2C)
    val ColorPrimaryLightAccent = Color(0xFFF0F3FF)
    val TextSecondary = Color(0xFF434654)
    val BorderLight = Color(0xFFE3E6D6)
    val GreenText = Color(0xFF137333)

    // Hero banner gradient (enroll/diploma details hero cards)
    val HeroGradientStart = Color(0xFF04385E)
    val HeroGradientEnd = Color(0xFF0170BA)
    val ChipBorder = Color(0x33FFFFFF)

    val OnMedia = Color(0xFFFFFFFF)

    val OnMediaMuted = Color(0xE6FFFFFF)// 90%

    val MediaScrim = Color(0xFF000000)// base of image gradients

    val MediaGlass = Color(0x1FFFFFFF)// 12%, tile fill

    val MediaGlassBorder = Color(0x2EFFFFFF)      // 18%

    val MediaGlassChip = Color(0x26FFFFFF)        // 15%, icon chip behind tile icons

    val MediaIndicatorInactive = Color(0xFFFFFFFF)

    val MediaButtonContainer = Color(0xFFFFFFFF)
}