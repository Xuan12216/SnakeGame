package com.xuan.snakegame.screen.snake

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.xuan.snakegame.`object`.FoodType
import com.xuan.snakegame.`object`.GameState

@Composable
fun GameArea(
    gameState: GameState,
    gridSize: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // 計算每個格子的大小
        val cellSizePx = size.width / gridSize

        // 畫蛇身
        gameState.snake.drop(1).forEach { (x, y) ->
            drawRect(
                color = Color.Black,
                topLeft = Offset(x * cellSizePx, y * cellSizePx),
                size = Size(cellSizePx, cellSizePx)
            )
        }

        // 畫蛇頭
        gameState.snake.firstOrNull()?.let { (x, y) ->
            // 畫頭部方塊
            drawRect(
                color = Color.Black,
                topLeft = Offset(x * cellSizePx, y * cellSizePx),
                size = Size(cellSizePx, cellSizePx)
            )

            // 畫眼睛
            val eyeRadius = cellSizePx * 0.1f  // 眼睛大小為格子的15%
            val eyeOffset = cellSizePx * 0.25f  // 眼睛距離格子邊緣的距離為25%
            val eyeYOffset = cellSizePx * 0.5f

            // 左眼
            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(
                    x * cellSizePx + eyeOffset,
                    y * cellSizePx + eyeYOffset
                )
            )

            // 右眼
            drawCircle(
                color = Color.White,
                radius = eyeRadius,
                center = Offset(
                    (x + 1) * cellSizePx - eyeOffset,
                    y * cellSizePx + eyeYOffset
                )
            )
        }

        // 畫食物
        drawRect(
            color = Color.Red,
            topLeft = Offset(
                gameState.food.position.first * cellSizePx,
                gameState.food.position.second * cellSizePx
            ),
            size = Size(cellSizePx, cellSizePx)
        )

        //bonusFood
        if (null != gameState.bonusFood) {
            val bonusFoodColor = when (gameState.bonusFood.type) {
                FoodType.SPEED_UP -> Color.Blue
                FoodType.SLOW_DOWN -> Color.Green
                FoodType.INVINCIBLE -> Color.Yellow
                FoodType.SHORTEN -> Color.Magenta
                else -> Color.Red
            }

            drawRect(
                color = bonusFoodColor,
                topLeft = Offset(
                    gameState.bonusFood.position.first * cellSizePx,
                    gameState.bonusFood.position.second * cellSizePx
                ),
                size = Size(cellSizePx, cellSizePx)
            )
        }
    }
}