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
    NOTHING_DOT("Nothing Dotted", "Nothing OS dot-matrix style", "ROUTINEFLOW 123"),
    NOTHING_SANS("Nothing Sans", "Tech-clean minimal monospace", "RoutineFlow 123"),
    SAMSUNG_ONE("Samsung One UI", "Curved modern One UI style", "RoutineFlow 123"),
    CLAUDE_SERIF("Claude Serif", "Serene editorial serif", "RoutineFlow 123"),
    CUSTOM("Custom Font", "User imported TTF / OTF file", "RoutineFlow 123");
}

object FontManager {
    private const val PREFS_NAME = "routine_flow_font_prefs"
    private const val KEY_FONT_OPTION = "selected_font_option"
    private const val CUSTOM_FONT_FILENAME = "custom_font.ttf"

    private val _currentFontOption = MutableStateFlow(FontOption.DEFAULT)
    val currentFontOption: StateFlow<FontOption> = _currentFontOption.asStateFlow()

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
            FontOption.GOOGLE_SANS -> FontFamily.SansSerif
            FontOption.NOTHING_DOT -> FontFamily.Monospace
            FontOption.NOTHING_SANS -> FontFamily.Monospace
            FontOption.SAMSUNG_ONE -> FontFamily.SansSerif
            FontOption.CLAUDE_SERIF -> FontFamily.Serif
            FontOption.CUSTOM -> _customFontFamily.value ?: FontFamily.Default
        }
    }

    fun createDynamicTypography(fontFamily: FontFamily): Typography {
        return Typography(
            displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp),
            displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
            displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
            headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp),
            headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp),
            headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
            titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
            titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
            bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
            bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
            bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
            labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
            labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
            labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        )
    }
}
