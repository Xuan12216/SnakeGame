package com.xuan.snakegame.util

import com.xuan.snakegame.`object`.GameState

class AppUtils {
    companion object {
        fun isInvincible(gameState: GameState): Boolean {
            return gameState.invincibleCountdownTime > 1000
        }
    }
}