package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.xuan.snakegame.R

@Composable
fun SettingBar(
    modifier: Modifier,
    isClickSetting : () -> Unit,
    isAi1Enabled: Boolean,
    isAi2Enabled: Boolean,
    onAiToggle1: () -> Unit,
    onAiToggle2: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AI 托管開關
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onAiToggle1() }
                .padding(end = 8.dp)
        ) {
            Text(
                text = "AI託管",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = isAi1Enabled,
                onCheckedChange = { onAiToggle1() },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // AI 托管開關QLearning
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onAiToggle2() }
                .padding(end = 8.dp)
        ) {
            Text(
                text = "AI託管( QLearning )",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = isAi2Enabled,
                onCheckedChange = { onAiToggle2() },
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // 設置按鈕
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = isClickSetting),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.round_settings_24),
                contentDescription = "icon",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}