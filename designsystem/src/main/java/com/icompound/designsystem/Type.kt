package com.icompound.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * KaiTech → Material 3 Typography
 * Source: KaiTech Design System §3 "Typography"
 *
 * Doc scale (9 styles):
 *   Display 40/300 · H1 32/600 · H2 26/600 · H3 23/600 · H4 20/600 ·
 *   H5 16/600 · H6/label 13/600 (uppercase) · Body 13/400 · Small 11/400
 *
 * Rule applied: "Headings = 600 at tight 1.2 line-height; body = 13px / 1.5"
 *   -> Display/H1–H6/label use line-height = size * 1.2
 *   -> Body/Small use line-height = size * 1.5
 *
 * NOTE: Display's weight is 300 (Light), not Bold — corrected from the
 * previous version of this file, which used FontWeight.Bold for both
 * displayLarge and headlineLarge.
 *
 * NOTE: H6/label is uppercase per the doc. TextStyle has no text-transform
 * property in Compose — apply `.uppercase()` on the string at the call site
 * wherever labelLarge/Medium/Small is used.
 */

// Define the custom FontFamily
val poppinsFont = FontFamily(
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

val KaiTechTypography = Typography(
    // Display — doc: "Display 40/300"
    displayLarge = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.Light, // 300, not Bold
        fontSize = 40.sp,
        lineHeight = 48.sp, // 40 * 1.2
        letterSpacing = 0.sp
    ),

    // Headline — doc: H1 32/600, H2 26/600
    headlineLarge = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold, // 600, not Bold
        fontSize = 32.sp,
        lineHeight = 38.sp, // 32 * 1.2
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 31.sp, // 26 * 1.2
        letterSpacing = 0.sp
    ),

    // Title — doc: H3 23/600, H4 20/600, H5 16/600
    titleLarge = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 23.sp,
        lineHeight = 28.sp, // 23 * 1.2
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp, // 20 * 1.2
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 19.sp, // 16 * 1.2
        letterSpacing = 0.sp
    ),

    // Body — doc: "Body 13/400". Doc has one body size; bodyMedium reuses it
    // since there's no distinct doc value for a middle body tier.
    bodyLarge = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp, // 13 * 1.5, rounded
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // doc: "Small 11/400"
    bodySmall = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 17.sp, // 11 * 1.5, rounded
        letterSpacing = 0.sp
    ),

    // Label — doc: "H6 / label 13/600 (uppercase for labels)".
    // Doc defines only one label size; labelMedium/labelSmall are
    // interpolated smaller steps at the same weight, not literal doc values.
    labelLarge = TextStyle(
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp, // 13 * 1.2
        letterSpacing = 0.5.sp // slight tracking, conventional for uppercase labels
    ),
    labelMedium = TextStyle( // interpolated, not a literal doc row
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle( // interpolated, not a literal doc row
        fontFamily = poppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)