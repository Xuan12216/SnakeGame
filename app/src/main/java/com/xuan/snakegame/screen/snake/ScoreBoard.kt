package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.xuan.snakegame.`object`.FoodType
import com.xuan.snakegame.`object`.GameState

@Composable
fun ScoreBoard(
    modifier: Modifier,
    gameState: GameState
) {
    Column(modifier = modifier) {

        //INVINCIBLE
        ScoreRow(
            leftText = if (gameState.invincibleCountdownTime > 0) "無敵時間: ${gameState.invincibleCountdownTime / 1000}s" else "",
            rightText = "",
        )

        // Display bonus food type and countdown
        var type = ""
        type = when(gameState.bonusFood?.type) {
            FoodType.NORMAL -> "普通"
            FoodType.SPEED_UP -> "加速"
            FoodType.SLOW_DOWN -> "減速"
            FoodType.INVINCIBLE -> "無敵"
            else -> ""
        }
        ScoreRow(
            leftText = if (gameState.bonusFood != null) "TYPE: ${type}" else "",
            rightText = if (gameState.countdownTime > 0) "COUNTDOWN: ${gameState.countdownTime / 1000}s" else "",
            rightTextColor = if (gameState.countdownTime > 0) Color.Red else LocalContentColor.current
        )

        // Display score and high score
        ScoreRow(
            leftText = "SCORE: ${gameState.score}",
            rightText = "HIGH SCORE: ${gameState.highScore}"
        )
    }
}

@Composable
fun ScoreRow(
    leftText: String,
    rightText: String,
    rightTextColor: Color = LocalContentColor.current
) {
    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        Text(
            modifier = Modifier.padding(end = 16.dp),
            text = leftText,
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = rightText,
            color = rightTextColor,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}