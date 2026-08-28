package com.rendox.routinetracker.core.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rendox.routinetracker.core.data.backup.BackupManager
import com.rendox.routinetracker.core.database.RoutineTrackerDatabase
import com.rendox.routinetracker.core.ui.helpers.HapticsHelper
import com.rendox.routinetracker.core.ui.theme.AppIconManager
import com.rendox.routinetracker.core.ui.theme.AppIconOption
import com.rendox.routinetracker.core.ui.theme.ColorPalette
import com.rendox.routinetracker.core.ui.theme.FontManager
import com.rendox.routinetracker.core.ui.theme.FontOption
import com.rendox.routinetracker.core.ui.theme.GlobalFontScale
import com.rendox.routinetracker.core.ui.theme.ThemeManager
import com.rendox.routinetracker.core.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatformTools

private enum class SettingsScreenCategory {
    APPEARANCE,
    TYPOGRAPHY,
    APP_ICON,
    BACKUP,
    SPONSOR,
    ABOUT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeState by ThemeManager.themeState.collectAsStateWithLifecycle()
    val currentFontOption by FontManager.currentFontOption.collectAsStateWithLifecycle()
    val currentGlobalScale by FontManager.currentGlobalFontScale.collectAsStateWithLifecycle()
    val currentTaskFontSize by FontManager.agendaTaskFontSize.collectAsStateWithLifecycle()
    val currentAppIcon by AppIconManager.currentIcon.collectAsStateWithLifecycle()

    var expandedCategory by remember { mutableStateOf<SettingsScreenCategory?>(null) }

    val database = remember {
        try {
            KoinPlatformTools.defaultContext().get().get<RoutineTrackerDatabase>()
        } catch (_: Exception) {
            null
        }
    }

    // Export backup launcher
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null && database != null) {
            scope.launch {
                try {
                    val result = BackupManager.exportBackup(context, database, uri)
                    if (result.isSuccess) {
                        HapticsHelper.performCelebration(context)
                        Toast.makeText(context, "✅ Backup exported successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ Export failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ Export error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Restore backup launcher
    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null && database != null) {
            scope.launch {
                try {
                    val result = BackupManager.importBackup(context, database, uri)
                    if (result.isSuccess) {
                        HapticsHelper.performCelebration(context)
                        Toast.makeText(context, "✅ Restored ${result.getOrNull()} habits successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "❌ Restore failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ Restore error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Custom Font import launcher
    val customFontLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            val result = FontManager.importCustomFont(context, uri)
            if (result.isSuccess) {
                Toast.makeText(context, "Custom font loaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to load custom font", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        HapticsHelper.performClick(context)
        Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        )
                        Text(
                            text = "Personalize your RoutineFlow experience",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. APPEARANCE & THEMES
            CategoryCard(
                title = "Appearance & Themes",
                subtitle = "Theme modes, AMOLED black & curated palettes",
                icon = Icons.Default.Settings,
                isExpanded = expandedCategory == SettingsScreenCategory.APPEARANCE,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.APPEARANCE) null else SettingsScreenCategory.APPEARANCE
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    // Theme Mode
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Theme Mode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = themeState.themeMode == mode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { ThemeManager.setThemeMode(mode) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(12.dp),
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = mode.title,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // AMOLED Black
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pure Black (AMOLED)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = "Pitch black backgrounds for OLED battery savings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = themeState.isAmoledBlack,
                            onCheckedChange = { ThemeManager.setAmoledBlack(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Color Palettes Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Color Palette",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )

                        val palettes = ColorPalette.entries
                        for (i in palettes.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val p1 = palettes[i]
                                PaletteItem(
                                    modifier = Modifier.weight(1f),
                                    palette = p1,
                                    isSelected = themeState.colorPalette == p1,
                                    onClick = { ThemeManager.setColorPalette(p1) },
                                )
                                if (i + 1 < palettes.size) {
                                    val p2 = palettes[i + 1]
                                    PaletteItem(
                                        modifier = Modifier.weight(1f),
                                        palette = p2,
                                        isSelected = themeState.colorPalette == p2,
                                        onClick = { ThemeManager.setColorPalette(p2) },
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 2. TYPOGRAPHY & FONTS
            CategoryCard(
                title = "Typography & In-App Fonts",
                subtitle = "Presets, font scaling, task size & custom TTF/OTF",
                icon = Icons.Default.Edit,
                isExpanded = expandedCategory == SettingsScreenCategory.TYPOGRAPHY,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.TYPOGRAPHY) null else SettingsScreenCategory.TYPOGRAPHY
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Global App Font Scale
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Global App Font Scale",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            GlobalFontScale.entries.forEach { scale ->
                                val isSelected = currentGlobalScale == scale
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { FontManager.setGlobalFontScale(scale) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        text = when (scale) {
                                            GlobalFontScale.SMALL -> "85%"
                                            GlobalFontScale.DEFAULT -> "100%"
                                            GlobalFontScale.LARGE -> "115%"
                                            GlobalFontScale.EXTRA_LARGE -> "130%"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Home Screen Habit Title Font Size
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Home Task Title Size",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            )
                            Text(
                                text = "${currentTaskFontSize}sp",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        val taskSizes = listOf(16, 18, 20, 22, 24)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            taskSizes.forEach { sizeSp ->
                                val isSelected = currentTaskFontSize == sizeSp
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { FontManager.setAgendaTaskFontSize(sizeSp) }
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                        ),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        text = "${sizeSp}sp",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Font Presets
                    Text(
                        text = "Font Presets",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    )

                    FontOption.entries.filter { it != FontOption.CUSTOM }.forEach { option ->
                        val isSelected = currentFontOption == option
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { FontManager.setFontOption(context, option) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = FontManager.getFontFamily(option),
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = option.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    // Import Custom Font Button
                    OutlinedButton(
                        onClick = { customFontLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-otf", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = "📁 Import Custom Font (.ttf / .otf)")
                    }
                }
            }

            // 3. LAUNCHER APP ICONS
            CategoryCard(
                title = "App Icon Style",
                subtitle = "Match Nothing / NAM, Material You or AMOLED",
                icon = Icons.Default.Star,
                isExpanded = expandedCategory == SettingsScreenCategory.APP_ICON,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.APP_ICON) null else SettingsScreenCategory.APP_ICON
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppIconOption.entries.forEach { iconOption ->
                        val isSelected = currentAppIcon == iconOption
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { AppIconManager.setAppIcon(context, iconOption) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(14.dp),
                                ),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Faithful Vector-Style Canvas Icon Preview
                                IconCanvasPreview(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    iconOption = iconOption,
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = iconOption.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = iconOption.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. BACKUP & DATA STORAGE
            CategoryCard(
                title = "Backup & Data Storage",
                subtitle = "Offline JSON export & import",
                icon = Icons.Default.Lock,
                isExpanded = expandedCategory == SettingsScreenCategory.BACKUP,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.BACKUP) null else SettingsScreenCategory.BACKUP
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "Your habit data is 100% private and stored locally. You can export complete relational backups into standard JSON files.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { exportBackupLauncher.launch("RoutineFlow_Backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text = "Export JSON", maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { restoreBackupLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(text = "Restore JSON", maxLines = 1)
                        }
                    }
                }
            }

            // 5. SUPPORT & SPONSOR (@AizenIndex)
            CategoryCard(
                title = "Support & Sponsor",
                subtitle = "Support lead maintainer @AizenIndex",
                icon = Icons.Default.Favorite,
                isExpanded = expandedCategory == SettingsScreenCategory.SPONSOR,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.SPONSOR) null else SettingsScreenCategory.SPONSOR
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "RoutineFlow is free, open-source, and privacy-focused with zero ads. If it helps your daily discipline, consider supporting ongoing development!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // GitHub Sponsors
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AizenIndex"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = "💖 Sponsor via GitHub (@AizenIndex)")
                    }
                }
            }

            // 6. ABOUT ROUTINEFLOW
            CategoryCard(
                title = "About RoutineFlow",
                subtitle = "Version, maintainers & open-source license",
                icon = Icons.Default.Info,
                isExpanded = expandedCategory == SettingsScreenCategory.ABOUT,
                onToggle = {
                    expandedCategory = if (expandedCategory == SettingsScreenCategory.ABOUT) null else SettingsScreenCategory.ABOUT
                },
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "RoutineFlow v1.1.0",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "GPL v3",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }

                    Text(
                        text = "A modernized, privacy-first habit planner with adaptive scheduling, custom themes, streak analytics, and offline backups.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    // Maintainer Card (Clean layout, no text wrapping)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "A",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontSize = 18.sp,
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "AizenIndex",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                    Text(
                                        text = "Lead Maintainer & Modernizer",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AizenIndex"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                            ) {
                                Text(text = "github.com/AizenIndex ↗")
                            }
                        }
                    }

                    Text(
                        text = "❤️ Original Architecture: Daniel Rendox",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/AizenIndex/RoutineFlow"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = "🌐 View Repository on GitHub")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                RoundedCornerShape(18.dp),
            )
            .animateContentSize(animationSpec = tween(250)),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(if (isExpanded) 3.dp else 1.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle,
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation),
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    content()
                }
            }
        }
    }
}

@Composable
private fun PaletteItem(
    modifier: Modifier = Modifier,
    palette: ColorPalette,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(palette.previewPrimary))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(palette.previewSecondary))
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(palette.previewTertiary))

                Spacer(modifier = Modifier.weight(1f))

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Text(
                text = palette.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun IconCanvasPreview(
    modifier: Modifier = Modifier,
    iconOption: AppIconOption,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Background
        drawRect(color = iconOption.previewBg)

        when (iconOption) {
            AppIconOption.DEFAULT -> {
                // Ocean Cyan with Dual White Progress Rings
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    radius = w * 0.38f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.08f),
                )
                drawArc(
                    color = Color(0xFF22D3EE),
                    startAngle = -90f,
                    sweepAngle = 240f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.38f, cy - h * 0.38f),
                    size = androidx.compose.ui.geometry.Size(w * 0.76f, h * 0.76f),
                    style = Stroke(width = w * 0.08f),
                )
                drawCircle(
                    color = Color(0xFF34D399),
                    radius = w * 0.12f,
                    center = Offset(cx, cy),
                )
            }

            AppIconOption.NOTHING_NAM -> {
                // Nothing Dot-Matrix with Signature Red Accent Dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = w * 0.36f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.06f),
                )
                drawCircle(
                    color = Color(0xFFD71921),
                    radius = w * 0.14f,
                    center = Offset(cx, cy),
                )
            }

            AppIconOption.MATERIAL_YOU -> {
                // Soft Pastel Monet Arcs
                drawCircle(
                    color = iconOption.previewAccent.copy(alpha = 0.25f),
                    radius = w * 0.36f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.09f),
                )
                drawArc(
                    color = iconOption.previewAccent,
                    startAngle = 45f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.36f, cy - h * 0.36f),
                    size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.72f),
                    style = Stroke(width = w * 0.09f),
                )
            }

            AppIconOption.AMOLED_BLACK -> {
                // Pure Black with Pure White Geometry
                drawCircle(
                    color = Color.White,
                    radius = w * 0.35f,
                    center = Offset(cx, cy),
                    style = Stroke(width = w * 0.07f),
                )
                drawCircle(
                    color = Color.White,
                    radius = w * 0.10f,
                    center = Offset(cx, cy),
                )
            }
        }
    }
}
