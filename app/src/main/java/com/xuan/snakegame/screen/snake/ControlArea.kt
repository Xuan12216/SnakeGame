package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xuan.snakegame.R
import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.GameState

@Composable
fun ControlArea(
    gameState: GameState,
    onDirectionChange: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // 使用 BoxWithConstraints 來獲取可用空間
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // 計算按鈕大小
            var buttonSize = minOf((maxWidth.value - 10.dp.value) / 3, (maxHeight.value - 10.dp.value) / 3).dp
            if (buttonSize > 80.dp) buttonSize = 80.dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                DirectionButton(R.drawable.rounded_arrow_upward_alt_24, Direction.UP, !gameState.isGameOver, onDirectionChange, buttonSize)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(vertical = 5.dp)
                ) {
                    DirectionButton(R.drawable.rounded_arrow_left_alt_24, Direction.LEFT, !gameState.isGameOver, onDirectionChange, buttonSize)
                    DirectionButton(R.drawable.round_stop_24, Direction.CENTER, !gameState.isGameOver, onDirectionChange, buttonSize)
                    DirectionButton(R.drawable.rounded_arrow_right_alt_24, Direction.RIGHT, !gameState.isGameOver, onDirectionChange, buttonSize)
                }

                DirectionButton(R.drawable.rounded_arrow_downward_alt_24, Direction.DOWN, !gameState.isGameOver, onDirectionChange, buttonSize)
            }
        }
    }
}

@Composable
fun DirectionButton(
    iconId: Int,
    direction: Direction,
    enabled: Boolean,
    onDirectionChange: (Direction) -> Unit,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                enabled = enabled,
                onClick = { onDirectionChange(direction) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = "icon",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}