package com.rendox.routinetracker.core.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ThemeMode(val title: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark"),
}

enum class ColorPalette(
    val title: String,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val previewTertiary: Color,
) {
    DYNAMIC(
        title = "Material You",
        previewPrimary = Color(0xFF6750A4),
        previewSecondary = Color(0xFF625B71),
        previewTertiary = Color(0xFF7D5260),
    ),
    ROUTINE_FLOW(
        title = "RoutineFlow (Cyan)",
        previewPrimary = Color(0xFF0E7490),
        previewSecondary = Color(0xFF06B6D4),
        previewTertiary = Color(0xFF10B981),
    ),
    CATPPUCCIN(
        title = "Catppuccin",
        previewPrimary = Color(0xFFCBA6F7),
        previewSecondary = Color(0xFF89B4FA),
        previewTertiary = Color(0xFFF38BA8),
    ),
    NORD(
        title = "Nord",
        previewPrimary = Color(0xFF88C0D0),
        previewSecondary = Color(0xFF81A1C1),
        previewTertiary = Color(0xFF5E81AC),
    ),
    DRACULA(
        title = "Dracula",
        previewPrimary = Color(0xFFBD93F9),
        previewSecondary = Color(0xFFFF79C6),
        previewTertiary = Color(0xFF8BE9FD),
    ),
    TOKYO_NIGHT(
        title = "Tokyo Night",
        previewPrimary = Color(0xFF7AA2F7),
        previewSecondary = Color(0xFFBB9AF7),
        previewTertiary = Color(0xFF7DCFFF),
    ),
    SUNSET_CORAL(
        title = "Sunset Coral",
        previewPrimary = Color(0xFFFF6B6B),
        previewSecondary = Color(0xFFFFA07A),
        previewTertiary = Color(0xFFFFD166),
    );

    val isDynamicAvailable: Boolean
        get() = this != DYNAMIC || Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.ROUTINE_FLOW,
    val isAmoledBlack: Boolean = false,
)

object ThemeManager {
    private const val PREFS_NAME = "routineflow_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_PALETTE = "key_color_palette"
    private const val KEY_AMOLED = "key_amoled_black"

    private var prefs: SharedPreferences? = null
    private val _themeState = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _themeState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadFromPrefs()
        }
    }

    private fun loadFromPrefs() {
        val p = prefs ?: return
        val modeStr = p.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val paletteStr = p.getString(KEY_PALETTE, ColorPalette.ROUTINE_FLOW.name) ?: ColorPalette.ROUTINE_FLOW.name
        val isAmoled = p.getBoolean(KEY_AMOLED, false)

        val mode = try { ThemeMode.valueOf(modeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        val palette = try { ColorPalette.valueOf(paletteStr) } catch (_: Exception) { ColorPalette.ROUTINE_FLOW }

        _themeState.update {
            ThemeState(
                themeMode = mode,
                colorPalette = palette,
                isAmoledBlack = isAmoled,
            )
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeState.update { it.copy(themeMode = mode) }
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.name)?.apply()
    }

    fun setColorPalette(palette: ColorPalette) {
        _themeState.update { it.copy(colorPalette = palette) }
        prefs?.edit()?.putString(KEY_PALETTE, palette.name)?.apply()
    }

    fun setAmoledBlack(enabled: Boolean) {
        _themeState.update { it.copy(isAmoledBlack = enabled) }
        prefs?.edit()?.putBoolean(KEY_AMOLED, enabled)?.apply()
    }
}
