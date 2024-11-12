package com.xuan.snakegame.`object`.snake

import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.FoodType
import com.xuan.snakegame.`object`.GameState
import com.xuan.snakegame.util.AppUtils

class SnakeQLearningAI {
    // Q-Table: State -> (Action -> Value)
    private val qTable = mutableMapOf<String, MutableMap<Direction, Double>>()
    private val directions = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)

    // Hyperparameters
    private val learningRate = 0.1 // α
    private val discountFactor = 0.9 // γ
    private var epsilon = 0.1 // ε (Exploration rate)

    // 获取状态表示 (头部位置 + 食物位置 + 当前方向)
    fun getState(gameState: GameState): String {
        val head = gameState.snake.first()
        val food = gameState.food.position
        return "${head.first},${head.second}:${food.first},${food.second}:${gameState.direction}"
    }

    // 获取动作的 Q 值，若没有则初始化
    private fun getQValue(state: String, action: Direction): Double {
        return qTable.getOrPut(state) { mutableMapOf() }.getOrDefault(action, 0.0)
    }

    // 更新 Q 值
    fun updateQValue(state: String, action: Direction, reward: Double, nextState: String) {
        val oldQ = getQValue(state, action)
        val nextMaxQ = directions.maxOfOrNull { getQValue(nextState, it) } ?: 0.0
        val newQ = oldQ + learningRate * (reward + discountFactor * nextMaxQ - oldQ)
        qTable[state]?.set(action, newQ)
    }

    // 设置探索率 (用于训练过程的调整)
    fun setEpsilon(newEpsilon: Double) {
        epsilon = newEpsilon
    }

    //=====

    fun evaluateDirections(gameState: GameState): Direction {
        val head = gameState.snake.first()
        val food = gameState.food.position
        val directions = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
        val validDirections = mutableMapOf<Direction, Double>()

        val isInvincible = AppUtils.isInvincible(gameState) // 假设 GameState 有一个 isInvincible 标记

        for (direction in directions) {
            val newHead = when (direction) {
                Direction.UP -> Pair(head.first, head.second - 1)
                Direction.DOWN -> Pair(head.first, head.second + 1)
                Direction.LEFT -> Pair(head.first - 1, head.second)
                Direction.RIGHT -> Pair(head.first + 1, head.second)
                Direction.CENTER -> continue
            }

            // 无敌模式下不检查碰撞，普通模式下需要检查碰撞
            if (!isInvincible && isCollision(newHead, gameState)) {
                validDirections[direction] = -1000.0 // 普通模式下，撞墙或撞蛇身评分最低
            } else {
                // 计算距离评分 (越靠近食物分数越高)
                val distanceScore = 100.0 / (manhattanDistance(newHead, food) + 1)

                // 根据 Q-Table 的值进行评分
                val state = getState(gameState)
                val qValueScore = getQValue(state, direction)

                // 如果有道具食物，根据类型调整评分
                var bonusScore = 0.0
                gameState.bonusFood?.let { bonusFood ->
                    if (newHead == bonusFood.position) {
                        bonusScore = when (bonusFood.type) {
                            FoodType.SPEED_UP -> 50.0
                            FoodType.SLOW_DOWN -> 20.0
                            FoodType.INVINCIBLE -> 100.0
                            FoodType.SHORTEN -> 30.0
                            else -> 0.0
                        }
                    }
                }

                // 无敌模式下额外奖励 (如更高的探索分数)
                val invincibleBonus = if (isInvincible) 20.0 else 0.0

                // 计算总评分
                val totalScore = distanceScore + qValueScore + bonusScore + invincibleBonus

                validDirections[direction] = totalScore
            }
        }

        // 返回评分最高的方向
        return validDirections.maxByOrNull { it.value }?.key ?: Direction.UP
    }

    private fun isCollision(position: Pair<Int, Int>, gameState: GameState): Boolean {
        return position.first < 0 ||
                position.first >= gameState.gridSize ||
                position.second < 0 ||
                position.second >= gameState.gridSize ||
                position in gameState.snake
    }

    private fun manhattanDistance(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Int {
        return kotlin.math.abs(pos1.first - pos2.first) + kotlin.math.abs(pos1.second - pos2.second)
    }
}