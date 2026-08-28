package com.rendox.routinetracker.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. ROUTINEFLOW (Ocean Cyan)
// ==========================================
val RoutineFlowLightColors = lightColorScheme(
    primary = Color(0xFF0E7490),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFF8FE),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF0891B2),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC7F3FE),
    onSecondaryContainer = Color(0xFF001F26),
    tertiary = Color(0xFF059669),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFA7F3D0),
    onTertiaryContainer = Color(0xFF002114),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFF94A3B8),
)

val RoutineFlowDarkColors = darkColorScheme(
    primary = Color(0xFF22D3EE),
    onPrimary = Color(0xFF003641),
    primaryContainer = Color(0xFF004E5E),
    onPrimaryContainer = Color(0xFFCFF8FE),
    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF003641),
    secondaryContainer = Color(0xFF004E5E),
    onSecondaryContainer = Color(0xFFC7F3FE),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF003822),
    tertiaryContainer = Color(0xFF005234),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF1F5F9),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
)

// ==========================================
// 2. CATPPUCCIN (Latte / Mocha)
// ==========================================
val CatppuccinLightColors = lightColorScheme(
    primary = Color(0xFF8839EF), // Mauve
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEDCFF),
    onPrimaryContainer = Color(0xFF2E004E),
    secondary = Color(0xFF1E66F5), // Blue
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE5FF),
    onSecondaryContainer = Color(0xFF001A41),
    tertiary = Color(0xFFD20F39), // Red
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD9),
    onTertiaryContainer = Color(0xFF410008),
    background = Color(0xFFEFF1F5), // Base
    onBackground = Color(0xFF4C4F69), // Text
    surface = Color(0xFFE6E9EF), // Mantle
    onSurface = Color(0xFF4C4F69),
    surfaceVariant = Color(0xFFCCD0DA), // Surface0
    onSurfaceVariant = Color(0xFF5C5F77),
    outline = Color(0xFF9CA0B0),
)

val CatppuccinDarkColors = darkColorScheme(
    primary = Color(0xFFCBA6F7), // Mauve
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEEDCFF),
    secondary = Color(0xFF89B4FA), // Blue
    onSecondary = Color(0xFF003063),
    secondaryContainer = Color(0xFF00478B),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFF38BA8), // Red
    onTertiary = Color(0xFF5C0012),
    tertiaryContainer = Color(0xFF7E1325),
    onTertiaryContainer = Color(0xFFFFD9DD),
    background = Color(0xFF181825), // Mantle
    onBackground = Color(0xFFCDD6F4), // Text
    surface = Color(0xFF1E1E2E), // Base
    onSurface = Color(0xFFCDD6F4),
    surfaceVariant = Color(0xFF313244), // Surface0
    onSurfaceVariant = Color(0xFFA6ADC8),
    outline = Color(0xFF585B70),
)

// ==========================================
// 3. NORD
// ==========================================
val NordLightColors = lightColorScheme(
    primary = Color(0xFF5E81AC), // Nord10
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E2FF),
    onPrimaryContainer = Color(0xFF001A41),
    secondary = Color(0xFF81A1C1), // Nord9
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE5FF),
    onSecondaryContainer = Color(0xFF001D33),
    tertiary = Color(0xFF88C0D0), // Nord8
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBAEAFF),
    onTertiaryContainer = Color(0xFF001F29),
    background = Color(0xFFECEFF4), // Nord6
    onBackground = Color(0xFF2E3440), // Nord0
    surface = Color(0xFFE5E9F0), // Nord5
    onSurface = Color(0xFF2E3440),
    surfaceVariant = Color(0xFFD8DEE9), // Nord4
    onSurfaceVariant = Color(0xFF4C566A), // Nord3
    outline = Color(0xFF7B889B),
)

val NordDarkColors = darkColorScheme(
    primary = Color(0xFF88C0D0), // Nord8
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D62),
    onPrimaryContainer = Color(0xFFBAEAFF),
    secondary = Color(0xFF81A1C1), // Nord9
    onSecondary = Color(0xFF003353),
    secondaryContainer = Color(0xFF164B70),
    onSecondaryContainer = Color(0xFFCEE5FF),
    tertiary = Color(0xFFB48EAD), // Nord15
    onTertiary = Color(0xFF431B3E),
    tertiaryContainer = Color(0xFF5B3155),
    onTertiaryContainer = Color(0xFFFFD6F9),
    background = Color(0xFF242933),
    onBackground = Color(0xFFECEFF4),
    surface = Color(0xFF2E3440), // Nord0
    onSurface = Color(0xFFECEFF4),
    surfaceVariant = Color(0xFF3B4252), // Nord1
    onSurfaceVariant = Color(0xFFD8DEE9),
    outline = Color(0xFF4C566A),
)

// ==========================================
// 4. DRACULA
// ==========================================
val DraculaLightColors = lightColorScheme(
    primary = Color(0xFF7A4EDB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADBFF),
    onPrimaryContainer = Color(0xFF24005A),
    secondary = Color(0xFFE0429C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD8E7),
    onSecondaryContainer = Color(0xFF3B0021),
    tertiary = Color(0xFF009AA6),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF67F6FF),
    onTertiaryContainer = Color(0xFF002023),
    background = Color(0xFFF8F8F2),
    onBackground = Color(0xFF282A36),
    surface = Color(0xFFEDECE6),
    onSurface = Color(0xFF282A36),
    surfaceVariant = Color(0xFFDFDFD6),
    onSurfaceVariant = Color(0xFF44475A),
    outline = Color(0xFF6272A4),
)

val DraculaDarkColors = darkColorScheme(
    primary = Color(0xFFBD93F9), // Purple
    onPrimary = Color(0xFF2F006D),
    primaryContainer = Color(0xFF4C218B),
    onPrimaryContainer = Color(0xFFEADBFF),
    secondary = Color(0xFFFF79C6), // Pink
    onSecondary = Color(0xFF5D0039),
    secondaryContainer = Color(0xFF810051),
    onSecondaryContainer = Color(0xFFFFD8E7),
    tertiary = Color(0xFF8BE9FD), // Cyan
    onTertiary = Color(0xFF00363B),
    tertiaryContainer = Color(0xFF004F56),
    onTertiaryContainer = Color(0xFF67F6FF),
    background = Color(0xFF1E1F29),
    onBackground = Color(0xFFF8F8F2),
    surface = Color(0xFF282A36),
    onSurface = Color(0xFFF8F8F2),
    surfaceVariant = Color(0xFF44475A),
    onSurfaceVariant = Color(0xFFBFBFBA),
    outline = Color(0xFF6272A4),
)

// ==========================================
// 5. TOKYO NIGHT
// ==========================================
val TokyoNightLightColors = lightColorScheme(
    primary = Color(0xFF34548A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7E2FF),
    onPrimaryContainer = Color(0xFF001A40),
    secondary = Color(0xFF6D4DA6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEDDCFF),
    onSecondaryContainer = Color(0xFF270057),
    tertiary = Color(0xFF00687A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFACEBFA),
    onTertiaryContainer = Color(0xFF001F26),
    background = Color(0xFFE6E7EE),
    onBackground = Color(0xFF1A1B26),
    surface = Color(0xFFDCDEE7),
    onSurface = Color(0xFF1A1B26),
    surfaceVariant = Color(0xFFCCD0DF),
    onSurfaceVariant = Color(0xFF343B58),
    outline = Color(0xFF565F89),
)

val TokyoNightDarkColors = darkColorScheme(
    primary = Color(0xFF7AA2F7), // Blue
    onPrimary = Color(0xFF002D6E),
    primaryContainer = Color(0xFF1A448E),
    onPrimaryContainer = Color(0xFFD7E2FF),
    secondary = Color(0xFFBB9AF7), // Purple
    onSecondary = Color(0xFF3D1C76),
    secondaryContainer = Color(0xFF54348E),
    onSecondaryContainer = Color(0xFFEDDCFF),
    tertiary = Color(0xFF7DCFFF), // Cyan
    onTertiary = Color(0xFF003540),
    tertiaryContainer = Color(0xFF004E5C),
    onTertiaryContainer = Color(0xFFACEBFA),
    background = Color(0xFF16161E),
    onBackground = Color(0xFFC0CAF5),
    surface = Color(0xFF1A1B26),
    onSurface = Color(0xFFC0CAF5),
    surfaceVariant = Color(0xFF24283B),
    onSurfaceVariant = Color(0xFF9AA5CE),
    outline = Color(0xFF565F89),
)

// ==========================================
// 6. SUNSET CORAL
// ==========================================
val SunsetCoralLightColors = lightColorScheme(
    primary = Color(0xFFE11D48),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9DE),
    onPrimaryContainer = Color(0xFF3F0013),
    secondary = Color(0xFFEA580C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCE),
    onSecondaryContainer = Color(0xFF370E00),
    tertiary = Color(0xFFD97706),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEAC),
    onTertiaryContainer = Color(0xFF2B1700),
    background = Color(0xFFFFF1F2),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFE4E6),
    onSurface = Color(0xFF201A1B),
    surfaceVariant = Color(0xFFF4DCDD),
    onSurfaceVariant = Color(0xFF524344),
    outline = Color(0xFF857374),
)

val SunsetCoralDarkColors = darkColorScheme(
    primary = Color(0xFFFF6B6B),
    onPrimary = Color(0xFF5E001F),
    primaryContainer = Color(0xFF860030),
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = Color(0xFFFFA07A),
    onSecondary = Color(0xFF5A1A00),
    secondaryContainer = Color(0xFF7E2A00),
    onSecondaryContainer = Color(0xFFFFDBCE),
    tertiary = Color(0xFFFFD166),
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF623F00),
    onTertiaryContainer = Color(0xFFFFDEAC),
    background = Color(0xFF171520),
    onBackground = Color(0xFFECE0E1),
    surface = Color(0xFF1F1D2B),
    onSurface = Color(0xFFECE0E1),
    surfaceVariant = Color(0xFF2D293E),
    onSurfaceVariant = Color(0xFFD7C1C2),
    outline = Color(0xFF9F8C8D),
)

fun ColorScheme.applyAmoledOverride(isDark: Boolean, isAmoled: Boolean): ColorScheme {
    return if (isDark && isAmoled) {
        this.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceVariant = Color(0xFF121212),
        )
    } else {
        this
    }
}
