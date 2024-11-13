package com.xuan.snakegame.util

import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.FoodType
import com.xuan.snakegame.`object`.GameState
import kotlin.math.abs

class AppUtils {
    companion object {
        fun isInvincible(gameState: GameState): Boolean {
            return gameState.invincibleCountdownTime > 1000
        }

        fun getFoodType(gameState: GameState): String {
            val type: String = when(gameState.bonusFood?.type) {
                FoodType.NORMAL -> "普通"
                FoodType.SPEED_UP -> "加速"
                FoodType.SLOW_DOWN -> "減速"
                FoodType.INVINCIBLE -> "無敵"
                else -> "縮短"
            }
            return type
        }

        fun getNextPosition(current: Pair<Int, Int>, direction: Direction): Pair<Int, Int> {
            return when (direction) {
                Direction.UP -> Pair(current.first, current.second - 1)
                Direction.DOWN -> Pair(current.first, current.second + 1)
                Direction.LEFT -> Pair(current.first - 1, current.second)
                Direction.RIGHT -> Pair(current.first + 1, current.second)
                else -> current
            }
        }

        // 計算remain時間的函數
        fun calculateRemainTime(endTime: Long, currentTime: Long): Long {
            return if (endTime != 0L) endTime - currentTime else 0L
        }

        fun isCollisionBody(position: Pair<Int, Int>, gameState: GameState): Boolean {
            if (!gameState.isOpen) {
                // 如果不是開放模式,直接檢查位置是否在蛇身上
                return position in gameState.snake
            }

            // 在開放模式下,需要檢查所有可能的等效位置
            val width = gameState.gridSize
            val height = gameState.gridSize

            // 計算所有可能的等效位置
            val equivalentPositions = listOf(
                position,
                // 水平方向的等效位置
                Pair(
                    if (position.first >= width) position.first % width
                    else if (position.first < 0) width - (abs(position.first) % width)
                    else position.first,
                    position.second
                ),
                // 垂直方向的等效位置
                Pair(
                    position.first,
                    if (position.second >= height) position.second % height
                    else if (position.second < 0) height - (abs(position.second) % height)
                    else position.second
                ),
                // 對角方向的等效位置
                Pair(
                    if (position.first >= width) position.first % width
                    else if (position.first < 0) width - (abs(position.first) % width)
                    else position.first,
                    if (position.second >= height) position.second % height
                    else if (position.second < 0) height - (abs(position.second) % height)
                    else position.second
                )
            ).distinct()

            // 檢查所有等效位置是否與蛇身重疊
            return equivalentPositions.any { pos ->
                // 對蛇身的每個部分也要考慮其等效位置
                gameState.snake.any { snakePos ->
                    // 計算蛇身部分的等效位置
                    val snakeEquivPositions = listOf(
                        snakePos,
                        Pair(snakePos.first % width, snakePos.second),
                        Pair(snakePos.first, snakePos.second % height),
                        Pair(snakePos.first % width, snakePos.second % height)
                    ).distinct()

                    // 檢查是否有任何等效位置重疊
                    pos in snakeEquivPositions
                }
            }
        }

        fun isCollision(position: Pair<Int, Int>, gameState: GameState): Boolean {
            return position.first < 0 ||
                    position.first >= gameState.gridSize ||
                    position.second < 0 ||
                    position.second >= gameState.gridSize ||
                    position in gameState.snake
        }
    }
}