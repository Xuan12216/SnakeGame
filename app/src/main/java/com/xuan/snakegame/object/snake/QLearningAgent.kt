package com.xuan.snakegame.`object`.snake

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xuan.snakegame.`object`.Constant
import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.GameState
import com.xuan.snakegame.util.AppUtils
import com.xuan.snakegame.util.SharedPreferencesUtils
import kotlin.math.abs
import kotlin.random.Random

class QLearningAgent (context: Context){
    private val prefs = SharedPreferencesUtils(context)
    private val gson = Gson()
    private var qTable = loadQTable()
    private val learningRate = 0.1     // 學習率，決定新信息的權重
    private val discountFactor = 0.9   // 折扣因子，決定未來獎勵的重要性
    private val epsilon = 0.1          // 探索率，決定隨機探索的概率
    // 用於記錄最近的蛇頭位置，檢測循環行為
    private val recentPositions = LinkedHashSet<Pair<Int, Int>>()
    private val maxRecentSize = 5

    companion object {
        private const val SAVE_INTERVAL = 100 // 每100步保存一次
        private var stepCount = 0
    }

    // 將Q表轉換為JSON字符串
    private fun qTableToJson(): String {
        return gson.toJson(qTable)
    }

    // 從JSON字符串加載Q表
    private fun loadQTable(): MutableMap<String, MutableMap<Direction, Double>> {
        val json = prefs.getString(Constant.Q_TABLE, "")
        if (json.isEmpty()) {
            return mutableMapOf()
        }

        val type = object : TypeToken<MutableMap<String, MutableMap<Direction, Double>>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            mutableMapOf()
        }
    }

    // 保存Q表
    private fun saveQTable() {
        try {
            val json = qTableToJson()
            prefs.saveString(Constant.Q_TABLE, json)
        }
        catch (e: Exception) { e.printStackTrace() }
    }

    // 定期保存Q表
    private fun checkAndSave() {
        stepCount++
        if (stepCount % SAVE_INTERVAL == 0) {
            saveQTable()
        }
    }

    // 手動保存Q表的方法（可在遊戲結束時調用）
    fun forceSave() {
        saveQTable()
    }

    //============================================================

    // 獲取狀態的特征表示
    fun getState(gameState: GameState): String {
        val head = gameState.snake.first()
        val food = gameState.food.position
        val bonusFood = gameState.bonusFood?.position
        val isInvincible = AppUtils.isInvincible(gameState)

        // 計算食物相對於蛇頭的方向
        val foodDeltaX = food.first - head.first
        val foodDeltaY = food.second - head.second

        // 計算bonus food相對於蛇頭的方向（如果存在）
        val bonusFoodInfo = if (bonusFood != null) {
            "${sign(bonusFood.first - head.first)},${sign(bonusFood.second - head.second)}"
        }
        else "0,0"

        // 檢查四個方向是否有危險（墻壁或蛇身）
        val dangerUp = if (isInvincible) false else isDanger(head, Direction.UP, gameState)
        val dangerDown = if (isInvincible) false else isDanger(head, Direction.DOWN, gameState)
        val dangerLeft = if (isInvincible) false else isDanger(head, Direction.LEFT, gameState)
        val dangerRight = if (isInvincible) false else isDanger(head, Direction.RIGHT, gameState)

        // 返回狀態字符串
        return "${sign(foodDeltaX)},${sign(foodDeltaY)},$bonusFoodInfo,$dangerUp,$dangerDown,$dangerLeft,$dangerRight,${if (isInvincible) 1 else 0}"
    }

    // 輔助函數：將數值簡化為方向指示(-1, 0, 1)
    private fun sign(value: Int): Int = when {
        value > 0 -> 1
        value < 0 -> -1
        else -> 0
    }

    // 檢查某個方向是否危險
    private fun isDanger(head: Pair<Int, Int>, direction: Direction, gameState: GameState): Boolean {
        val nextPos = AppUtils.getNextPosition(head, direction)
        return if (gameState.isOpen) {
            AppUtils.isCollisionBody(nextPos, gameState)// 在開放模式下，只需檢查是否會撞到蛇身
        }
        else AppUtils.isCollision(nextPos, gameState)// 在封閉模式下，檢查墻壁和蛇身
    }

    // 選擇下一個動作
    fun getNextDirection(gameState: GameState): Direction {
        val state = getState(gameState)
        val head = gameState.snake.first()

        // 如果當前狀態未見過，初始化Q值
        if (state !in qTable) {
            qTable[state] = mutableMapOf(
                Direction.UP to 0.0,
                Direction.DOWN to 0.0,
                Direction.LEFT to 0.0,
                Direction.RIGHT to 0.0
            )
        }

        checkAndSave() // 檢查是否需要保存Q表

        // 獲取所有安全的移動方向
        val safeDirections = Direction.entries
            .filter { it != Direction.CENTER }
            .filter { direction ->
                val nextPos = AppUtils.getNextPosition(head, direction)

                if (AppUtils.isInvincible(gameState)) true
                else if (gameState.isOpen) !AppUtils.isCollisionBody(nextPos, gameState)//檢查是否會撞到蛇身
                else !AppUtils.isCollision(nextPos, gameState)//檢查是否會發生一般碰撞
            }

        // ε-貪心策略：有一定概率隨機探索
        var test2: MutableMap<Direction, Double>? = null
        val direction =  if (Random.nextDouble() < epsilon) {
            // 從安全方向中隨機選擇
            if (safeDirections.isNotEmpty()) {
                safeDirections.random()
            } else {
                // 如果沒有安全方向，則選擇任意方向（可能會導致遊戲結束）
                Direction.entries.filter { it != Direction.CENTER }.random()
            }
        } else {
            // 從安全方向中選擇Q值最大的動作
            if (safeDirections.isNotEmpty()) {
                test2 = qTable[state]
                safeDirections.maxByOrNull { qTable[state]?.get(it) ?: 0.0 } ?: Direction.UP
            } else {
                test2 = qTable[state]
                // 如果沒有安全方向，選擇Q值最大的方向
                qTable[state]?.maxByOrNull { it.value }?.key ?: Direction.UP
            }
        }

        println("TestXuan: $safeDirections: direction: $direction: test2: $test2: state: $state: size: ${qTable.size}")

        return direction
    }

    // 更新Q值
    fun updateQValue(state: String, action: Direction, reward: Double, nextState: String) {
        // 如果下一個狀態未見過，初始化Q值
        if (!qTable.containsKey(nextState)) {
            qTable[nextState] = mutableMapOf(
                Direction.UP to 0.0,
                Direction.DOWN to 0.0,
                Direction.LEFT to 0.0,
                Direction.RIGHT to 0.0
            )
        }

        // 獲取當前狀態-動作對的Q值
        val currentQ = qTable[state]?.get(action) ?: 0.0

        // 獲取下一個狀態的最大Q值
        val maxNextQ = qTable[nextState]?.values?.maxOrNull() ?: 0.0

        // Q-learning更新公式
        val newQ = currentQ + learningRate * (reward + discountFactor * maxNextQ - currentQ)

        // 更新Q表
        qTable[state]?.put(action, newQ)
    }

    // 計算獎勵
    fun calculateReward(gameState: GameState, newHead: Pair<Int, Int>): Double {
        var reward = 0.0
        val foodList = listOfNotNull(gameState.food.position, gameState.bonusFood?.position)

        // 處理食物和bonus food的獎勵
        foodList.forEach { food ->
            // 如果吃到食物，給予獎勵
            if (newHead == food) {
                reward += 1.0
                if (AppUtils.isInvincible(gameState)) reward += 0.5
            }

            // 根據與食物的距離給予獎勵或懲罰
            val oldDistance = manhattanDistance(gameState.snake.first(), food)
            val newDistance = manhattanDistance(newHead, food)
            reward += (oldDistance - newDistance) * 0.1
        }

        // 如果遊戲結束，給予懲罰
        if (gameState.isGameOver) {
            reward -= if (AppUtils.isCollisionBody(newHead, gameState)) 2.0 else 1.0
        }

        // 檢測循環狀態並給予懲罰
        if (isInLoop(newHead)) {
            reward -= 0.5
        }

        return reward
    }

    // 計算曼哈頓距離
    private fun manhattanDistance(pos1: Pair<Int, Int>, pos2: Pair<Int, Int>): Int {
        return abs(pos1.first - pos2.first) + abs(pos1.second - pos2.second)
    }

    // 檢測蛇頭位置是否進入循環狀態
    private fun isInLoop(newHead: Pair<Int, Int>): Boolean {
        // 將新位置加入隊列中
        if (recentPositions.size >= maxRecentSize) {
            recentPositions.remove(recentPositions.first()) // 移除最舊的位置
        }
        // 如果添加失敗說明位置重覆，進入循環狀態
        if (!recentPositions.add(newHead)) {
            return true
        }

        return false
    }
}