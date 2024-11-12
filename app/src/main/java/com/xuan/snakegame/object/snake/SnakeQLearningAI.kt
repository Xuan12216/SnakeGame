package com.xuan.snakegame.`object`.snake

import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.GameState

class SnakeQLearningAI {
    // Q-Table: State -> (Action -> Value)
    private val qTable = mutableMapOf<String, MutableMap<Direction, Double>>()
    private val learningRate = 0.1
    private val discountFactor = 0.9
    private var explorationRate = 0.3 // 提高初始探索率
    private val explorationDecay = 0.995 // 探索率衰减因子

    //=====

    // 获取下一个动作，使用ε-greedy策略
    fun getNextAction(state: String, validActions: List<Direction>): Direction {
        if (validActions.isEmpty()) return Direction.CENTER

        // 探索：随机选择动作
        if (Math.random() < explorationRate) {
            return validActions.random()
        }

        // 利用：选择最佳动作
        return getBestAction(state, validActions)
    }

    // 获取状态下的最佳动作
    private fun getBestAction(state: String, validActions: List<Direction>): Direction {
        if (!qTable.containsKey(state)) {
            qTable[state] = mutableMapOf()
        }

        val stateActions = qTable[state]!!
        // 为未见过的动作初始化随机的小Q值
        validActions.forEach { action ->
            if (!stateActions.containsKey(action)) {
                stateActions[action] = Math.random() * 0.1
            }
        }

        // 从有效动作中选择Q值最高的
        return validActions.maxByOrNull { stateActions[it] ?: 0.0 } ?: validActions.first()
    }

    // 更新Q值
    fun updateQ(oldState: String, action: Direction, reward: Double, newState: String, validActions: List<Direction>) {
        if (!qTable.containsKey(oldState)) {
            qTable[oldState] = mutableMapOf()
        }
        if (!qTable.containsKey(newState)) {
            qTable[newState] = mutableMapOf()
        }

        val oldStateActions = qTable[oldState]!!
        if (!oldStateActions.containsKey(action)) {
            oldStateActions[action] = 0.0
        }

        // 获取新状态下的最大Q值
        val maxNextQ = if (validActions.isEmpty()) 0.0 else {
            validActions.maxOf { qTable[newState]?.get(it) ?: 0.0 }
        }

        // 更新Q值
        val oldQ = oldStateActions[action] ?: 0.0
        val newQ = oldQ + learningRate * (reward + discountFactor * maxNextQ - oldQ)
        oldStateActions[action] = newQ

        // 衰减探索率
        explorationRate *= explorationDecay
        // 设置探索率下限
        if (explorationRate < 0.01) explorationRate = 0.01
    }

    // 辅助函数：编码游戏状态
    fun encodeState(
        state: GameState,
        relativeX: Int,
        relativeY: Int,
        dangerDirections: Set<Direction>
    ): String {
        return "$relativeX,$relativeY|${dangerDirections.joinToString("")}"
    }

    // 计算两点间距离（考虑边界情况）
    private fun calculateDistance(
        pos1: Pair<Int, Int>,
        pos2: Pair<Int, Int>,
        isOpen: Boolean,
        gridSize: Int
    ): Int {
        val dx = if (isOpen) {
            val rawDx = kotlin.math.abs(pos1.first - pos2.first)
            kotlin.math.min(rawDx, gridSize - rawDx)
        } else {
            kotlin.math.abs(pos1.first - pos2.first)
        }

        val dy = if (isOpen) {
            val rawDy = kotlin.math.abs(pos1.second - pos2.second)
            kotlin.math.min(rawDy, gridSize - rawDy)
        } else {
            kotlin.math.abs(pos1.second - pos2.second)
        }

        return dx + dy
    }

    // 辅助函数：获取下一个位置
    fun getNextPosition(current: Pair<Int, Int>, direction: Direction): Pair<Int, Int> {
        return when (direction) {
            Direction.UP -> Pair(current.first, current.second - 1)
            Direction.DOWN -> Pair(current.first, current.second + 1)
            Direction.LEFT -> Pair(current.first - 1, current.second)
            Direction.RIGHT -> Pair(current.first + 1, current.second)
            else -> current
        }
    }

    // 辅助函数：检查移动是否安全
    fun isSafeMove(position: Pair<Int, Int>, state: GameState): Boolean {
        if (state.isOpen) {
            val adjustedPosition = Pair(
                (position.first + state.gridSize) % state.gridSize,
                (position.second + state.gridSize) % state.gridSize
            )
            return adjustedPosition !in state.snake
        }

        return position.first in 0 until state.gridSize &&
                position.second in 0 until state.gridSize &&
                position !in state.snake
    }

    // 更新后的奖励计算
    fun calculateReward(
        state: GameState,
        nextHead: Pair<Int, Int>,
        food: Pair<Int, Int>
    ): Double {
        return when {
            !isSafeMove(nextHead, state) -> -100.0 // 碰撞惩罚
            nextHead == food -> 100.0 // 吃到食物奖励
            else -> {
                // 计算距离变化
                val currentDistance = calculateDistance(state.snake.first(), food, state.isOpen, state.gridSize)
                val newDistance = calculateDistance(nextHead, food, state.isOpen, state.gridSize)
                when {
                    newDistance < currentDistance -> 2.0  // 增加接近食物的奖励
                    newDistance > currentDistance -> -1.5 // 增加远离食物的惩罚
                    else -> -0.5 // 增加停滞的惩罚
                }
            }
        }
    }

    // 获取更新后的危险方向
    fun getUpdatedDangerDirections(position: Pair<Int, Int>, state: GameState): Set<Direction> {
        return Direction.values().filter { direction ->
            val next = getNextPosition(position, direction)
            !isSafeMove(next, state)
        }.toSet()
    }

}