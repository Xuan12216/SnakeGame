package com.xuan.snakegame.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xuan.snakegame.`object`.Constant
import com.xuan.snakegame.`object`.Direction
import com.xuan.snakegame.`object`.Food
import com.xuan.snakegame.`object`.FoodType
import com.xuan.snakegame.`object`.GameState
import com.xuan.snakegame.`object`.snake.QLearningAgent
import com.xuan.snakegame.`object`.snake.SnakeAI
import com.xuan.snakegame.util.AppUtils
import com.xuan.snakegame.util.SharedPreferencesUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SnakeViewModel(context: Context) : ViewModel() {

    private val mPrefs = SharedPreferencesUtils(context)
    //=====
    private val _gameState = MutableStateFlow(
        GameState(
            snake = listOf(Pair(5, 5), Pair(4, 5)),
            food = Food(Pair(10, 10), type = FoodType.NORMAL),
            direction = Direction.CENTER,
            highScore = mPrefs.getInt(Constant.HIGH_SCORE, 0),
            gridSize = mPrefs.getInt(Constant.GRID_SIZE, 25),
            gameSpeed = mPrefs.getLong(Constant.GAME_SPEED, 225L),
            isOpen = mPrefs.getBoolean(Constant.OPEN_CLOSED, false)
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()
    //=====
    private var gameJob: Job? = null
    private var remainTime = 0L
    private var remainTimeInVincible = 0L
    //=====
    private val snakeAI = SnakeAI()//规则驱动的算法 (Rule-Based Algorithm)
    private val qLearningAgent = QLearningAgent(context)
    //=====
    init { startGame() }

    private fun startGame() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (!_gameState.value.isGameOver) {
                delay(_gameState.value.gameSpeed)
                updateGame()
            }
        }
    }

    fun changeDirection(newDirection: Direction) {
        if (!_gameState.value.isGameOver) {
            _gameState.update { it.copy(direction = newDirection) }
        }
    }

    fun restartGame() {
        val newHighScore = maxOf(_gameState.value.score, _gameState.value.highScore)
        mPrefs.saveInt(Constant.HIGH_SCORE, newHighScore)

        // 如果是AI模式2，保存Q表
        if (_gameState.value.isAIMode2) {
            qLearningAgent.forceSave()
        }

        _gameState.update {
            GameState(
                snake = listOf(Pair(5, 5), Pair(4, 5)),
                food = Food(Pair(10, 10), type = FoodType.NORMAL),
                direction = Direction.CENTER,
                highScore = newHighScore,
                gridSize = mPrefs.getInt(Constant.GRID_SIZE, 25),
                gameSpeed = mPrefs.getLong(Constant.GAME_SPEED, 225L),
                isOpen = mPrefs.getBoolean(Constant.OPEN_CLOSED, false)
            )
        }
        startGame()
    }

    fun updateGameSettings(newGridSize: Int, newGameSpeed: Long, isOpen: Boolean) {

        mPrefs.updateSnakeGameSettings(newGridSize, newGameSpeed, isOpen)
        restartGame()
    }

    private fun updateGame() {
        _gameState.update { currentState ->
            if (currentState.isGameOver) return@update currentState

            val currentTime = System.currentTimeMillis()

            // 处理方向为 CENTER 时，更新 bonusFood 和 invincible 的结束时间
            if (currentState.direction == Direction.CENTER) {
                // 获取remain时间
                if (remainTime == 0L) remainTime = AppUtils.calculateRemainTime(currentState.bonusFoodEndTime, currentTime)
                if (remainTimeInVincible == 0L) remainTimeInVincible = AppUtils.calculateRemainTime(currentState.invincibleTime, currentTime)

                // 计算新的结束时间
                val newBonusFoodEndTime = if(currentState.bonusFoodEndTime > 0L) currentTime + remainTime else 0L
                val newInvincibleTime = if(currentState.invincibleTime > 0L) currentTime + remainTimeInVincible else 0L

                return@update currentState.copy(
                    invincibleTime = newInvincibleTime,
                    bonusFoodEndTime = newBonusFoodEndTime
                )
            }
            else {
                remainTime = 0L
                remainTimeInVincible = 0L
            }

            // 刷新countdownTime
            val newCountdownTime = currentState.bonusFoodEndTime - currentTime

            // 判断无敌时间
            val isInvincible = currentTime < currentState.invincibleTime
            val newInvincibleCountdownTime = currentState.invincibleTime - currentTime

            // 处理蛇头的位置
            val head = currentState.snake.first()
            var newHead = AppUtils.getNextPosition(head, currentState.direction, currentState)

            // 判断碰撞
            if (_gameState.value.isOpen) {
                newHead = Pair((newHead.first + _gameState.value.gridSize) % _gameState.value.gridSize, (newHead.second + _gameState.value.gridSize) % _gameState.value.gridSize)
                if (AppUtils.isCollisionBody(newHead, _gameState.value) && !isInvincible){
                    return@update currentState.copy(isGameOver = true)
                }
            }
            else if (AppUtils.isCollision(newHead, _gameState.value)) {
                if (!isInvincible) return@update currentState.copy(isGameOver = true)
            }

            // 处理蛇的身体
            var newSnake = mutableListOf(newHead)
            newSnake.addAll(
                if (newHead == currentState.food.position) currentState.snake
                else currentState.snake.dropLast(1)
            )

            // 處理食物
            var newFood = currentState.food
            var newBonusFood = currentState.bonusFood
            var newScore = currentState.score
            var newBonusFoodEndTime = currentState.bonusFoodEndTime
            var newInvincibleTime = currentState.invincibleTime
            var gameSpeed = currentState.gameSpeed

            if (newHead == currentState.food.position) {
                newScore += 1
                newFood = generateRandomFood(newSnake, _gameState.value.gridSize, _gameState.value.gridSize)

                if (null == newBonusFood) {
                    newBonusFood = generateRandomFood(newSnake, _gameState.value.gridSize, _gameState.value.gridSize, isNormalFood = false)

                    if (newBonusFood.type == FoodType.NORMAL) newBonusFood = null
                    else newBonusFoodEndTime = currentTime + 5000
                }
            }

            //5秒後消失
            if (newBonusFood != null  && currentTime > newBonusFoodEndTime) {
                newBonusFood = null
                newBonusFoodEndTime = 0L
            }

            // 處理道具食物
            if (newBonusFood != null && newHead == newBonusFood.position) {
                when (newBonusFood.type) {
                    FoodType.SPEED_UP -> {
                        gameSpeed -= 25
                        if (gameSpeed < 50) gameSpeed = 50
                    }
                    FoodType.SLOW_DOWN -> {
                        gameSpeed += 25
                        if (gameSpeed > 275) gameSpeed = 275
                    }
                    FoodType.INVINCIBLE -> newInvincibleTime = currentTime + 10000
                    FoodType.SHORTEN -> if (newSnake.size > 1) newSnake = newSnake.dropLast(1).toMutableList()
                    else -> {}
                }
                newBonusFood = null
                newBonusFoodEndTime = 0L
            }

            currentState.copy(
                snake = newSnake,
                food = newFood,
                bonusFood = newBonusFood,
                score = newScore,
                invincibleTime = newInvincibleTime,
                bonusFoodEndTime = newBonusFoodEndTime,
                countdownTime = newCountdownTime,
                invincibleCountdownTime = newInvincibleCountdownTime,
                gameSpeed = gameSpeed
            )
        }
    }

    private fun generateRandomFood(
        snake: List<Pair<Int, Int>>,
        mapWidth: Int,
        mapHeight: Int,
        isNormalFood: Boolean = true
    ): Food {
        var foodPosition: Pair<Int, Int>
        do {
            foodPosition = Pair((0 until mapWidth).random(), (0 until mapHeight).random())
        } while (snake.contains(foodPosition))

        val foodType = if (isNormalFood) {
            FoodType.NORMAL
        }
        else {
            when ((0..100).random()) {
                in 0..70 -> FoodType.NORMAL
                in 71..80 -> FoodType.SPEED_UP
                in 81..90 -> FoodType.SPEED_UP
                in 91..95 -> FoodType.INVINCIBLE
                else -> FoodType.SHORTEN
            }
        }

        return Food(foodPosition, foodType)
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
        // 在ViewModel被清除时保存Q表
        if (_gameState.value.isAIMode2) {
            qLearningAgent.forceSave()
        }
    }

    //===================================================
    //规则驱动的算法 (Rule-Based Algorithm)

    fun toggleAIMode() {
        _gameState.update {
            it.copy(
                isAIMode1 = !it.isAIMode1,
                isAIMode2 = false
            )
        }

        if (_gameState.value.isAIMode1) startAIControl()
        else changeDirection(Direction.CENTER)
    }

    private fun startAIControl() {
        viewModelScope.launch {
            while (_gameState.value.isAIMode1 && !_gameState.value.isGameOver) {
                val nextDirection = snakeAI.getNextDirection(_gameState.value)
                changeDirection(nextDirection)
                delay(_gameState.value.gameSpeed)
            }
        }
    }

    //===================================================
    //Q-Learning，可以讓 AI 自動學習，而無需手動編寫複雜的規則。

    fun toggleAIMode2() {
        _gameState.update {
            it.copy(
                isAIMode2 = !it.isAIMode2,
                isAIMode1 = false
            )
        }

        if (_gameState.value.isAIMode2) startAIControl2()
        else changeDirection(Direction.CENTER)
    }

    // 启动Q-learning AI控制
    private fun startAIControl2() {
        viewModelScope.launch {
            while (_gameState.value.isAIMode2) {
                val currentState = _gameState.value

                // 获取当前状态
                val state = qLearningAgent.getState(currentState)

                // 选择动作
                val action = qLearningAgent.getNextDirection(currentState)

                // 获取新状态
                val newState = _gameState.value
                val newHead = AppUtils.getNextPosition(currentState.snake.first(), action, _gameState.value)

                // 计算奖励并更新Q值
                val reward = qLearningAgent.calculateReward(newState, newHead)
                qLearningAgent.updateQValue(state, action, reward, qLearningAgent.getState(newState))

                // 执行动作
                changeDirection(action)

                if (_gameState.value.isGameOver) break

                // 等待游戏更新
                delay(currentState.gameSpeed)
            }
        }
    }

    //===================================================

    companion object {
        fun getFactory(context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SnakeViewModel(context) as T
            }
        }
    }
}