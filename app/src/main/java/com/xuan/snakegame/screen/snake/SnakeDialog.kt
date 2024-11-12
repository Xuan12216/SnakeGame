package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.xuan.snakegame.`object`.GameState

@Composable
fun GameOverDialog(
    gameState: GameState,
    onRestartGame: (Int) -> Unit
) {
    Dialog(onDismissRequest = { }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Game Over title
                Text(
                    text = "Game Over",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Score
                Text(
                    text = "Score: ${gameState.score}",
                    style = MaterialTheme.typography.bodyLarge
                )

                // New High Score
                if (gameState.score > gameState.highScore) {
                    Text(
                        text = "New High Score!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restart button
                Button(
                    onClick = {
                        // 更新最高分
                        val newHighScore = maxOf(gameState.score, gameState.highScore)
                        // 重置遊戲狀態
                        onRestartGame(newHighScore)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重新開始")
                }
            }
        }
    }
}

@Composable
fun SettingDialog(
    currentGridSize: Int,
    currentGameSpeed: Long,
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Long, Boolean) -> Unit
) {
    val speedOptions = listOf(300L, 275L, 250L, 225L, 200L, 175L, 150L, 125L, 100L, 75L, 50L)
    val initialSpeedIndex = speedOptions.indexOfFirst { it == currentGameSpeed }.coerceAtLeast(0)

    var gridSize by remember { mutableIntStateOf(currentGridSize) }
    var speedIndex by remember { mutableIntStateOf(initialSpeedIndex) }
    var isSwitchOn by remember { mutableStateOf(isOpen) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Game Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Open / Close Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("OPEN / CLOSED", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isSwitchOn,
                        onCheckedChange = { isSwitchOn = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Size
                Text("Grid Size: $gridSize", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = gridSize.toFloat(),
                    onValueChange = { gridSize = it.toInt() },
                    valueRange = 15f..50f, // Adjust slider range as needed
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Game Speed
                //250L, 225L, 200L, 175L, 150L, 125L, 100L, 75L, 50L, 25L
                Text("Game Speed: ${speedIndex}x", style = MaterialTheme.typography.bodyLarge)
                Slider(
                    value = (speedIndex).toFloat(),
                    onValueChange = { speedIndex = it.toInt() },
                    valueRange = 1f..10f,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save and Cancel buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(onClick = {
                        onSave(gridSize, speedOptions[speedIndex], isSwitchOn)
                        onDismiss()
                    }) {
                        Text("Save & Reset Game")
                    }
                }
            }
        }
    }
}