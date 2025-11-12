package com.mytheclipse.quizbattle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.R

// Typography based on Figma designs
// Using system fonts as fallback since custom fonts need to be added separately
val OpenSans = FontFamily.SansSerif // Open Sans fallback
val PoppinsFontFamily = FontFamily.SansSerif // Poppins fallback
val JainiPurvaFontFamily = FontFamily.Cursive // Jaini Purva fallback for logo

// DigitalDisco font family
val DigitalDiscoFontFamily = FontFamily(
    Font(R.font.digitaldisco, FontWeight.Normal),
    Font(R.font.digitaldisco_thin, FontWeight.Thin)
)

val Typography = Typography(
    // Display - Large titles (Quiz Battle logo)
    displayLarge = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 64.sp,
        lineHeight = 84.sp
    ),
    displayMedium = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 40.sp,
        lineHeight = 52.sp
    ),
    
    // Headings
    headlineLarge = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 23.sp
    ),
    
    // Titles
    titleLarge = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    
    // Body text
    bodyLarge = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    
    // Labels
    labelLarge = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DigitalDiscoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 16.sp
    )
)
