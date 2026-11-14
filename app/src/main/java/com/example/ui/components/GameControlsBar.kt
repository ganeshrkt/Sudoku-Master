package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameControlsBar(
    canUndo: Boolean,
    undoCount: Int,
    isPencilMode: Boolean,
    isDigitFirstMode: Boolean,
    onUndoClick: () -> Unit,
    onPencilToggle: () -> Unit,
    onHintClick: () -> Unit,
    onDigitFirstToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Undo
        ControlButton(
            icon = Icons.AutoMirrored.Filled.Undo,
            label = "Undo",
            enabled = canUndo,
            badge = if (canUndo && undoCount > 0) undoCount.toString() else null,
            onClick = onUndoClick,
            testTag = "control_undo"
        )

        // Pencil Notes
        ControlButton(
            icon = Icons.Default.Create,
            label = "Notes",
            enabled = true,
            isActive = isPencilMode,
            badge = if (isPencilMode) "ON" else null,
            onClick = onPencilToggle,
            testTag = "control_notes"
        )

        // Digit-First toggle
        ControlButton(
            icon = Icons.Default.TouchApp,
            label = if (isDigitFirstMode) "Digit 1st" else "Cell 1st",
            enabled = true,
            isActive = isDigitFirstMode,
            onClick = onDigitFirstToggle,
            testTag = "control_digit_first"
        )

        // Hint
        ControlButton(
            icon = Icons.Default.AutoAwesome,
            label = "Hint",
            enabled = true,
            onClick = onHintClick,
            testTag = "control_hint"
        )
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    isActive: Boolean = false,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val contentColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isActive -> primaryColor
        else -> MaterialTheme.colorScheme.onSurface
    }

    val bgColor = if (isActive) primaryColor.copy(alpha = 0.14f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (badge != null) {
                        Badge(
                            containerColor = if (isActive) primaryColor else MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        ) {
                            Text(text = badge, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
