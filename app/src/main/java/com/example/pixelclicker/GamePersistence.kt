package com.example.pixelclicker

import android.content.Context
import android.content.SharedPreferences

class GamePersistence(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pixel_clicker_prefs", Context.MODE_PRIVATE)

    fun save(state: GameState) {
        prefs.edit()
            .putLong("gold", state.gold)
            .putLong("ore", state.ore)
            .putLong("fish", state.fish)
            .putLong("xp", state.experience)
            .putInt("level", state.level)
            .putInt("stamina", state.stamina)
            .putInt("maxStamina", state.maxStamina)
            .putInt("miningLevel", state.miningLevel)
            .putInt("fishingLevel", state.fishingLevel)
            .putInt("fightingLevel", state.fightingLevel)
            .putLong("lastTickMillis", state.lastTickMillis)
            .apply()
    }

    fun load(): GameState? {
        if (!prefs.contains("gold")) return null
        return GameState(
            gold = prefs.getLong("gold", 0L),
            ore = prefs.getLong("ore", 0L),
            fish = prefs.getLong("fish", 0L),
            experience = prefs.getLong("xp", 0L),
            level = prefs.getInt("level", 1),
            stamina = prefs.getInt("stamina", 10),
            maxStamina = prefs.getInt("maxStamina", 10),
            miningLevel = prefs.getInt("miningLevel", 1),
            fishingLevel = prefs.getInt("fishingLevel", 1),
            fightingLevel = prefs.getInt("fightingLevel", 1),
            lastTickMillis = prefs.getLong("lastTickMillis", System.currentTimeMillis())
        )
    }
}