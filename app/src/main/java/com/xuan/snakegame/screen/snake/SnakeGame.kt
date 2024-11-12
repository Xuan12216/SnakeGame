package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.xuan.snakegame.viewModel.SnakeViewModel
import com.xuan.snakegame.`object`.Direction

@Composable
fun SnakeGame(innerPadding: PaddingValues? = null) {

    val viewModel: SnakeViewModel = viewModel(factory = SnakeViewModel.getFactory(LocalContext.current))
    val gameState by viewModel.gameState.collectAsState()

    // State to control the visibility of the setting dialog
    var isSettingDialogOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (innerPadding != null) Modifier.padding(innerPadding) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Settings
        SettingBar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            isClickSetting = { isSettingDialogOpen = true },
            isAi1Enabled = gameState.isAIMode1,
            onAiToggle1 = viewModel::toggleAIMode,
            isAi2Enabled = gameState.isAIMode2,
            onAiToggle2 = viewModel::toggleAIMode2
        )

        // Score Board
        ScoreBoard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            gameState = gameState
        )

        // Game Area
        GameArea(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(1f)
                .background(Color.LightGray),
            gridSize = gameState.gridSize,
            gameState = gameState
        )

        // Control Area
        ControlArea(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
                .padding(horizontal = 16.dp),
            gameState = gameState,
            onDirectionChange = viewModel::changeDirection
        )
    }

    // 遊戲結束對話框
    if (gameState.isGameOver) {
        GameOverDialog(
            gameState = gameState,
            onRestartGame = { viewModel.restartGame() }
        )
    }

    // Setting Dialog
    if (isSettingDialogOpen) {
        viewModel.changeDirection(Direction.CENTER)
        SettingDialog(
            currentGridSize = gameState.gridSize,
            currentGameSpeed = gameState.gameSpeed,
            isOpen = gameState.isOpen,
            onDismiss = { isSettingDialogOpen = false },
            onSave = viewModel::updateGameSettings
        )
    }
}