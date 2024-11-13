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
import com.xuan.snakegame.`object`.GameState
import com.xuan.snakegame.util.AppUtils

@Composable
fun ScoreBoard(
    modifier: Modifier,
    gameState: GameState
) {
    Column(modifier = modifier) {

        //INVINCIBLE
        ScoreRow(
            leftText = "無敵時間: ${if (gameState.invincibleCountdownTime > 0) "${gameState.invincibleCountdownTime / 1000}s" else "-"}",
            rightText = "",
        )

        // Display bonus food type and countdown
        val type: String = AppUtils.getFoodType(gameState)
        ScoreRow(
            leftText = "TYPE: ${if (gameState.bonusFood != null) type else "-"}",
            rightText = "COUNTDOWN: ${if (gameState.countdownTime > 0) "${gameState.countdownTime / 1000}s" else "-"}",
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