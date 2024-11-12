package com.xuan.snakegame.util

import android.content.Context
import android.content.SharedPreferences
import com.xuan.snakegame.`object`.Constant

class SharedPreferencesUtils (context: Context){

    private val mPrefs: SharedPreferences = context.getSharedPreferences(Constant.XUAN, Context.MODE_PRIVATE)

    fun getInt(key: String, defaultValue: Int): Int {
        return mPrefs.getInt(key, defaultValue)
    }

    fun saveInt(key: String, value: Int) {
        mPrefs.edit().putInt(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return mPrefs.getBoolean(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        mPrefs.edit().putBoolean(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return mPrefs.getLong(key, defaultValue)
    }

    fun saveLong(key: String, value: Long) {
        mPrefs.edit().putLong(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return mPrefs.getString(key, defaultValue) ?: defaultValue
    }

    fun saveString(key: String, value: String) {
        mPrefs.edit().putString(key, value).apply()
    }

    fun clear() {
        mPrefs.edit().clear().apply()
    }

    fun remove(key: String) {
        mPrefs.edit().remove(key).apply()
    }

    fun contains(key: String): Boolean {
        return mPrefs.contains(key)
    }

    fun getAll(): Map<String, *> {
        return mPrefs.all
    }

    fun edit(): SharedPreferences.Editor {
        return mPrefs.edit()
    }

    fun updateSnakeGameSettings(newGridSize: Int, newGameSpeed: Long, isOpen: Boolean) {
        saveInt(Constant.GRID_SIZE, newGridSize)
        saveLong(Constant.GAME_SPEED, newGameSpeed)
        saveBoolean(Constant.OPEN_CLOSED, isOpen)
    }
}