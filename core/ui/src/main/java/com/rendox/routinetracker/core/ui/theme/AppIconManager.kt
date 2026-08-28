package com.rendox.routinetracker.core.ui.theme

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppIconOption(
    val title: String,
    val subtitle: String,
    val aliasName: String,
    val previewBg: Color,
    val previewAccent: Color,
) {
    DEFAULT(
        title = "Default (Teal)",
        subtitle = "Ocean Cyan & White Rings",
        aliasName = "com.rendox.routinetracker.app.MainActivityDefault",
        previewBg = Color(0xFF0E7490),
        previewAccent = Color(0xFFFFFFFF),
    ),
    NOTHING_NAM(
        title = "Nothing / NAM",
        subtitle = "Nothing Dot-Matrix & Red Dot",
        aliasName = "com.rendox.routinetracker.app.MainActivityNothing",
        previewBg = Color(0xFF121212),
        previewAccent = Color(0xFFD71921),
    ),
    MATERIAL_YOU(
        title = "Material You",
        subtitle = "Dynamic Monet Soft Pastel",
        aliasName = "com.rendox.routinetracker.app.MainActivityMaterialYou",
        previewBg = Color(0xFFD3E4FF),
        previewAccent = Color(0xFF004D8A),
    ),
    AMOLED_BLACK(
        title = "AMOLED Black",
        subtitle = "Pitch Black & Pure White",
        aliasName = "com.rendox.routinetracker.app.MainActivityAmoled",
        previewBg = Color(0xFF000000),
        previewAccent = Color(0xFFFFFFFF),
    );
}

object AppIconManager {
    private const val PREFS_NAME = "routineflow_app_icon_prefs"
    private const val KEY_SELECTED_ICON = "key_selected_icon"

    private var prefs: SharedPreferences? = null
    private val _currentIcon = MutableStateFlow(AppIconOption.DEFAULT)
    val currentIcon: StateFlow<AppIconOption> = _currentIcon.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedName = prefs?.getString(KEY_SELECTED_ICON, AppIconOption.DEFAULT.name) ?: AppIconOption.DEFAULT.name
            val savedOption = try {
                AppIconOption.valueOf(savedName)
            } catch (_: Exception) {
                AppIconOption.DEFAULT
            }
            _currentIcon.update { savedOption }
        }
    }

    fun setAppIcon(context: Context, option: AppIconOption) {
        _currentIcon.update { option }
        prefs?.edit()?.putString(KEY_SELECTED_ICON, option.name)?.apply()

        val pm = context.packageManager
        AppIconOption.entries.forEach { item ->
            try {
                val component = ComponentName(context.packageName, item.aliasName)
                val newState = if (item == option) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                pm.setComponentEnabledSetting(
                    component,
                    newState,
                    PackageManager.DONT_KILL_APP,
                )
            } catch (_: Exception) {
                // Ignore in case of non-existent alias during testing
            }
        }
    }
}
