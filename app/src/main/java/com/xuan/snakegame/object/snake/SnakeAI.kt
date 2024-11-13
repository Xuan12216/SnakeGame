package com.xuan.snakegame.`object`.snake

import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.GameState
import com.xuan.snakegame.util.AppUtils
import kotlin.math.abs

class SnakeAI {

    // 檢查移動是否安全
    private fun isSafeMove(
        newHead: Pair<Int, Int>,
        gameState: GameState
    ): Boolean {
        // 如果是開放邊界模式
        if (gameState.isOpen) {
            val adjustedHead = Pair(
                (newHead.first + gameState.gridSize) % gameState.gridSize,
                (newHead.second + gameState.gridSize) % gameState.gridSize
            )
            return AppUtils.isInvincible(gameState) || !gameState.snake.contains(adjustedHead)
        }

        // 檢查是否撞牆
        if (newHead.first < 0 || newHead.first >= gameState.gridSize ||
            newHead.second < 0 || newHead.second >= gameState.gridSize) {
            return false
        }

        // 檢查是否撞到自己
        return AppUtils.isInvincible(gameState) || !gameState.snake.contains(newHead)
    }

    // 計算兩點間的曼哈頓距離，考慮環繞情況
    private fun manhattanDistance(p1: Pair<Int, Int>, p2: Pair<Int, Int>, gameState: GameState): Int {
        if (!gameState.isOpen) {
            return abs(p1.first - p2.first) + abs(p1.second - p2.second)
        }

        // 在開放模式下考慮環繞距離
        val dx = minOf(
            abs(p1.first - p2.first),
            abs(p1.first - p2.first + gameState.gridSize),
            abs(p1.first - p2.first - gameState.gridSize)
        )
        val dy = minOf(
            abs(p1.second - p2.second),
            abs(p1.second - p2.second + gameState.gridSize),
            abs(p1.second - p2.second - gameState.gridSize)
        )
        return dx + dy
    }

    // 主要的AI決策邏輯
    fun getNextDirection(gameState: GameState): Direction {
        val head = gameState.snake.first()
        val food = gameState.food.position
        val bonusFood = gameState.bonusFood
        val isInvincible = AppUtils.isInvincible(gameState)

        // 所有可能的方向
        val possibleDirections = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)

        // 判斷是否有道具食物且距離較近
        val targetFood = if (bonusFood != null &&
            manhattanDistance(head, bonusFood.position, gameState) < manhattanDistance(head, food, gameState)) {
            bonusFood.position
        } else {
            food
        }

        // 評估每個方向
        val directionScores = possibleDirections.map { direction ->
            val nextPos = AppUtils.getNextPosition(head, direction)
            val adjustedPos = if (gameState.isOpen) {
                Pair(
                    (nextPos.first + gameState.gridSize) % gameState.gridSize,
                    (nextPos.second + gameState.gridSize) % gameState.gridSize
                )
            } else {
                nextPos
            }

            // 計算分數
            val score = if (!isSafeMove(nextPos, gameState)) {
                Int.MIN_VALUE
            } else {
                // 基於距離的分數（距離越近分數越高）
                val distanceScore = -manhattanDistance(adjustedPos, targetFood, gameState) * 10

                // 空間評估分數（避免進入死路）
                val spaceScore = if (isInvincible) {
                    // 無敵模式下降低空間評估的權重
                    evaluateSpace(adjustedPos, gameState) * 2
                } else {
                    evaluateSpace(adjustedPos, gameState) * 5
                }

                distanceScore + spaceScore
            }

            direction to score
        }

        // 選擇最佳方向
        val bestDirection = directionScores.maxByOrNull { it.second }

        // 如果所有方向都不安全且處於無敵狀態，選擇距離目標最近的方向
        return if (bestDirection?.second == Int.MIN_VALUE && isInvincible) {
            possibleDirections.minByOrNull { direction ->
                val nextPos = AppUtils.getNextPosition(head, direction)
                if (gameState.isOpen) {
                    val adjustedPos = Pair(
                        (nextPos.first + gameState.gridSize) % gameState.gridSize,
                        (nextPos.second + gameState.gridSize) % gameState.gridSize
                    )
                    manhattanDistance(adjustedPos, targetFood, gameState)
                } else {
                    manhattanDistance(nextPos, targetFood, gameState)
                }
            } ?: Direction.CENTER
        } else {
            bestDirection?.first ?: Direction.CENTER
        }
    }

    // 評估某個位置周圍的可用空間
    private fun evaluateSpace(
        position: Pair<Int, Int>,
        gameState: GameState
    ): Int {
        var spaceCount = 0
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(position)
        visited.add(position)

        while (queue.isNotEmpty() && visited.size < 8) {  // 只檢查周圍8個格子
            val current = queue.removeFirst()
            spaceCount++

            // 檢查四個方向
            listOf(
                Pair(current.first + 1, current.second),
                Pair(current.first - 1, current.second),
                Pair(current.first, current.second + 1),
                Pair(current.first, current.second - 1)
            ).forEach { next ->
                if (!visited.contains(next) && isSafeMove(next, gameState)) {
                    visited.add(next)
                    queue.add(next)
                }
            }
        }

        return spaceCount
    }
}