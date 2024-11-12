package com.xuan.snakegame.`object`

data class GameState(
    val snake: List<Pair<Int, Int>>,
    val food: Food,
    val bonusFood: Food? = null,
    val direction: Direction,
    val isGameOver: Boolean = false,
    val score: Int = 0,
    val highScore: Int = 0,
    val invincibleTime: Long = 0L,
    val invincibleCountdownTime: Long = 0L,
    val bonusFoodEndTime: Long = 0L,
    val countdownTime: Long = 0L,
    val gridSize: Int = 25,
    val gameSpeed: Long = 225L,
    val isOpen: Boolean = false,
    val isAIMode1: Boolean = false,
    val isAIMode2: Boolean = false
)

data class Food(
    val position: Pair<Int, Int>,
    val type: FoodType = FoodType.NORMAL
)