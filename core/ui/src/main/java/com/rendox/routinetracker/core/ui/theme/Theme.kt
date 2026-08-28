package com.rendox.routinetracker.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.rendox.routinetracker.core.ui.helpers.LocalLocale
import com.rendox.routinetracker.core.ui.helpers.getLocale

@Composable
fun RoutineTrackerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    disableDynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        ThemeManager.init(context)
        FontManager.init(context)
    }

    val themeState by ThemeManager.themeState.collectAsState()
    val fontOption by FontManager.currentFontOption.collectAsState()
    val customFontFamily by FontManager.customFontFamily.collectAsState()

    val isDark = when (themeState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val dynamicColorIsSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val isDynamic = themeState.colorPalette == ColorPalette.DYNAMIC && dynamicColorIsSupported && !disableDynamicColor

    val baseColors: ColorScheme = when {
        isDynamic && isDark -> dynamicDarkColorScheme(context)
        isDynamic && !isDark -> dynamicLightColorScheme(context)
        else -> when (themeState.colorPalette) {
            ColorPalette.DYNAMIC,
            ColorPalette.ROUTINE_FLOW -> if (isDark) RoutineFlowDarkColors else RoutineFlowLightColors
            ColorPalette.CATPPUCCIN -> if (isDark) CatppuccinDarkColors else CatppuccinLightColors
            ColorPalette.NORD -> if (isDark) NordDarkColors else NordLightColors
            ColorPalette.DRACULA -> if (isDark) DraculaDarkColors else DraculaLightColors
            ColorPalette.TOKYO_NIGHT -> if (isDark) TokyoNightDarkColors else TokyoNightLightColors
            ColorPalette.SUNSET_CORAL -> if (isDark) SunsetCoralDarkColors else SunsetCoralLightColors
        }
    }

    val finalColors = baseColors.applyAmoledOverride(isDark = isDark, isAmoled = themeState.isAmoledBlack)
    val routineStatusColors = if (isDark) routineStatusColorsDark else routineStatusColorsLight

    val activeFontFamily = remember(fontOption, customFontFamily) {
        FontManager.getFontFamily(fontOption)
    }
    val dynamicTypography = remember(activeFontFamily) {
        FontManager.createDynamicTypography(activeFontFamily)
    }

    CompositionLocalProvider(
        LocalRoutineStatusColors provides routineStatusColors,
        LocalLocale provides getLocale(),
    ) {
        MaterialTheme(
            colorScheme = finalColors,
            typography = dynamicTypography,
            content = content,
        )
    }
}