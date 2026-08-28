package com.rendox.routinetracker.core.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

enum class FontOption(
    val title: String,
    val subtitle: String,
    val sampleText: String,
) {
    DEFAULT("Default System", "Clean Android Roboto", "RoutineFlow 123"),
    GOOGLE_SANS("Google Sans", "Modern geometric sans-serif", "RoutineFlow 123"),
    INTER_GEIST("Inter / Geist", "Modern high-legibility UI sans", "RoutineFlow 123"),
    OUTFIT("Outfit", "Geometric rounded typography", "RoutineFlow 123"),
    NOTHING_DOT("Nothing Dotted", "Nothing OS dot-matrix style", "ROUTINEFLOW 123"),
    NOTHING_SANS("Nothing Sans", "Tech-clean minimal monospace", "RoutineFlow 123"),
    JETBRAINS_MONO("JetBrains Mono", "Developer coding monospace", "RoutineFlow 123"),
    SAMSUNG_ONE("Samsung One UI", "Curved modern One UI style", "RoutineFlow 123"),
    CLAUDE_SERIF("Claude Serif", "Serene editorial serif", "RoutineFlow 123"),
    CUSTOM("Custom Font", "User imported TTF / OTF file", "RoutineFlow 123");
}

enum class GlobalFontScale(val title: String, val scale: Float) {
    SMALL("Small (85%)", 0.85f),
    DEFAULT("Default (100%)", 1.0f),
    LARGE("Large (115%)", 1.15f),
    EXTRA_LARGE("Extra Large (130%)", 1.30f);
}

object FontManager {
    private const val PREFS_NAME = "routine_flow_font_prefs"
    private const val KEY_FONT_OPTION = "selected_font_option"
    private const val KEY_GLOBAL_FONT_SCALE = "selected_global_font_scale"
    private const val KEY_AGENDA_TASK_FONT_SIZE = "selected_agenda_task_font_size"
    private const val CUSTOM_FONT_FILENAME = "custom_font.ttf"

    private val _currentFontOption = MutableStateFlow(FontOption.DEFAULT)
    val currentFontOption: StateFlow<FontOption> = _currentFontOption.asStateFlow()

    private val _currentGlobalFontScale = MutableStateFlow(GlobalFontScale.DEFAULT)
    val currentGlobalFontScale: StateFlow<GlobalFontScale> = _currentGlobalFontScale.asStateFlow()

    private val _agendaTaskFontSize = MutableStateFlow(20)
    val agendaTaskFontSize: StateFlow<Int> = _agendaTaskFontSize.asStateFlow()

    private val _customFontFamily = MutableStateFlow<FontFamily?>(null)
    val customFontFamily: StateFlow<FontFamily?> = _customFontFamily.asStateFlow()

    private var sharedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedOption = sharedPrefs?.getString(KEY_FONT_OPTION, FontOption.DEFAULT.name) ?: FontOption.DEFAULT.name
            _currentFontOption.value = try {
                FontOption.valueOf(savedOption)
            } catch (_: Exception) {
                FontOption.DEFAULT
            }

            val savedScale = sharedPrefs?.getString(KEY_GLOBAL_FONT_SCALE, GlobalFontScale.DEFAULT.name) ?: GlobalFontScale.DEFAULT.name
            _currentGlobalFontScale.value = try {
                GlobalFontScale.valueOf(savedScale)
            } catch (_: Exception) {
                GlobalFontScale.DEFAULT
            }

            _agendaTaskFontSize.value = sharedPrefs?.getInt(KEY_AGENDA_TASK_FONT_SIZE, 20) ?: 20

            // Check if custom font file exists
            val customFile = File(context.filesDir, CUSTOM_FONT_FILENAME)
            if (customFile.exists() && customFile.length() > 0) {
                try {
                    _customFontFamily.value = FontFamily(Font(customFile))
                } catch (_: Exception) {
                    _customFontFamily.value = null
                }
            }
        }
    }

    fun setFontOption(context: Context, option: FontOption) {
        _currentFontOption.value = option
        sharedPrefs?.edit()?.putString(KEY_FONT_OPTION, option.name)?.apply()
    }

    fun setGlobalFontScale(scale: GlobalFontScale) {
        _currentGlobalFontScale.value = scale
        sharedPrefs?.edit()?.putString(KEY_GLOBAL_FONT_SCALE, scale.name)?.apply()
    }

    fun setAgendaTaskFontSize(sizeSp: Int) {
        _agendaTaskFontSize.value = sizeSp
        sharedPrefs?.edit()?.putInt(KEY_AGENDA_TASK_FONT_SIZE, sizeSp)?.apply()
    }

    fun importCustomFont(context: Context, uri: Uri): Result<Unit> {
        return try {
            val customFile = File(context.filesDir, CUSTOM_FONT_FILENAME)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(customFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (customFile.exists() && customFile.length() > 0) {
                _customFontFamily.value = FontFamily(Font(customFile))
                setFontOption(context, FontOption.CUSTOM)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to copy custom font file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFontFamily(option: FontOption): FontFamily {
        return when (option) {
            FontOption.DEFAULT -> FontFamily.Default
            FontOption.GOOGLE_SANS,
            FontOption.INTER_GEIST,
            FontOption.OUTFIT,
            FontOption.SAMSUNG_ONE -> FontFamily.SansSerif
            FontOption.NOTHING_DOT,
            FontOption.NOTHING_SANS,
            FontOption.JETBRAINS_MONO -> FontFamily.Monospace
            FontOption.CLAUDE_SERIF -> FontFamily.Serif
            FontOption.CUSTOM -> _customFontFamily.value ?: FontFamily.Default
        }
    }

    fun createDynamicTypography(fontFamily: FontFamily, scale: Float = 1.0f): Typography {
        fun s(baseSp: Float): androidx.compose.ui.unit.TextUnit = (baseSp * scale).sp
        return Typography(
            displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(57f), lineHeight = s(64f)),
            displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(45f), lineHeight = s(52f)),
            displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(36f), lineHeight = s(44f)),
            headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(32f), lineHeight = s(40f)),
            headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(28f), lineHeight = s(36f)),
            headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(24f), lineHeight = s(32f)),
            titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = s(22f), lineHeight = s(28f)),
            titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = s(16f), lineHeight = s(24f), letterSpacing = 0.15.sp),
            titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = s(14f), lineHeight = s(20f), letterSpacing = 0.1.sp),
            bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(16f), lineHeight = s(24f), letterSpacing = 0.5.sp),
            bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(14f), lineHeight = s(20f), letterSpacing = 0.25.sp),
            bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = s(12f), lineHeight = s(16f), letterSpacing = 0.4.sp),
            labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = s(14f), lineHeight = s(20f), letterSpacing = 0.1.sp),
            labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = s(12f), lineHeight = s(16f), letterSpacing = 0.5.sp),
            labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = s(11f), lineHeight = s(16f), letterSpacing = 0.5.sp),
        )
    }
}
