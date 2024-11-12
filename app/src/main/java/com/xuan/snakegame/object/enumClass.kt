package com.xuan.snakegame.`object`

enum class Direction {
    UP, DOWN, LEFT, RIGHT, CENTER
}

enum class FoodType {
    NORMAL,      // 普通食物
    SPEED_UP,    // 加速道具
    SLOW_DOWN,   // 減速道具
    INVINCIBLE,  // 無敵道具
    SHORTEN      // 縮短道具
}