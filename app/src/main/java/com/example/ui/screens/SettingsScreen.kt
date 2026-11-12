package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.ThemeMode
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val currentThemeMode = try {
        ThemeMode.valueOf(settings.themeMode)
    } catch (_: Exception) {
        ThemeMode.SYSTEM
    }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Theme Section
            SectionHeader(title = "Appearance & Theme", icon = Icons.Default.Palette)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeOptionRow(
                        title = "Follow System",
                        subtitle = "Adapts to device day & night settings",
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        previewColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = { settingsViewModel.setThemeMode(ThemeMode.SYSTEM) },
                        testTag = "theme_system"
                    )

                    ThemeOptionRow(
                        title = "Light Theme",
                        subtitle = "Clean daylight high-contrast palette",
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        previewColor = Color(0xFFF8FAFC),
                        borderColor = Color(0xFFCBD5E1),
                        onClick = { settingsViewModel.setThemeMode(ThemeMode.LIGHT) },
                        testTag = "theme_light"
                    )

                    ThemeOptionRow(
                        title = "Dark Theme",
                        subtitle = "Modern slate blue for low light",
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        previewColor = Color(0xFF1E293B),
                        onClick = { settingsViewModel.setThemeMode(ThemeMode.DARK) },
                        testTag = "theme_dark"
                    )

                    ThemeOptionRow(
                        title = "AMOLED Black",
                        subtitle = "Pure #000000 pixels for OLED battery saving",
                        isSelected = currentThemeMode == ThemeMode.AMOLED,
                        previewColor = Color(0xFF000000),
                        onClick = { settingsViewModel.setThemeMode(ThemeMode.AMOLED) },
                        testTag = "theme_amoled"
                    )
                }
            }

            // Gameplay & Assists Section
            SectionHeader(title = "Gameplay & Rules", icon = Icons.Default.Rule)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    SettingToggleRow(
                        title = "3 Mistakes Limit",
                        subtitle = "Game ends if you make 3 errors",
                        checked = settings.maxMistakes > 0,
                        onCheckedChange = { isChecked ->
                            settingsViewModel.setMaxMistakes(if (isChecked) 3 else 0)
                        },
                        testTag = "setting_mistakes_limit"
                    )

                    SettingToggleRow(
                        title = "Highlight Duplicates",
                        subtitle = "Red warning on conflicting numbers",
                        checked = settings.highlightDuplicates,
                        onCheckedChange = { settingsViewModel.toggleHighlightDuplicates(it) },
                        testTag = "setting_highlight_duplicates"
                    )

                    SettingToggleRow(
                        title = "Highlight Same Digits",
                        subtitle = "Highlights all matching numbers on board",
                        checked = settings.highlightSameNumbers,
                        onCheckedChange = { settingsViewModel.toggleHighlightSameNumbers(it) },
                        testTag = "setting_highlight_same"
                    )

                    SettingToggleRow(
                        title = "Auto-Clear Pencil Notes",
                        subtitle = "Removes notes in row/column/block when solved",
                        checked = settings.autoClearNotes,
                        onCheckedChange = { settingsViewModel.toggleAutoClearNotes(it) },
                        testTag = "setting_auto_clear_notes"
                    )

                    SettingToggleRow(
                        title = "Digit-First Input by Default",
                        subtitle = "Pick digit first, then tap multiple cells",
                        checked = settings.digitFirstMode,
                        onCheckedChange = { settingsViewModel.toggleDigitFirstMode(it) },
                        testTag = "setting_digit_first"
                    )

                    SettingToggleRow(
                        title = "Show Timer",
                        subtitle = "Display live elapsed solving duration",
                        checked = settings.showTimer,
                        onCheckedChange = { settingsViewModel.toggleShowTimer(it) },
                        testTag = "setting_show_timer"
                    )

                    SettingToggleRow(
                        title = "Vibration & Haptics",
                        subtitle = "Subtle tactile feedback for board taps and errors",
                        checked = settings.vibrationEnabled,
                        onCheckedChange = { settingsViewModel.toggleVibration(it) },
                        testTag = "setting_vibration"
                    )
                }
            }

            // About Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sudoku Master v1.0",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Offline SQLite database persistence with Room. Procedural Sudoku puzzle generation with guaranteed unique solutions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    previewColor: Color,
    borderColor: Color? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .then(
                        if (borderColor != null) Modifier.border(1.dp, borderColor, CircleShape) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = if (previewColor == Color.Black || previewColor == Color(0xFF1E293B)) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
