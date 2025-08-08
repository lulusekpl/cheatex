package com.example.pixelclicker

import kotlin.random.Random
import kotlin.math.max
import kotlin.math.min

/**
 * Core clicker game state and mechanics.
 */
data class GameState(
    var gold: Long = 0,
    var ore: Long = 0,
    var fish: Long = 0,
    var experience: Long = 0,
    var level: Int = 1,
    var stamina: Int = 10,
    var maxStamina: Int = 10,
    var miningLevel: Int = 1,
    var fishingLevel: Int = 1,
    var fightingLevel: Int = 1,
    var lastTickMillis: Long = System.currentTimeMillis()
) {

    companion object {
        const val STAMINA_COST_MINING = 2
        const val STAMINA_COST_FISHING = 2
        const val STAMINA_COST_FIGHTING = 4

        private const val STAMINA_REGEN_PER_SECOND = 1
    }

    data class ActionResult(val description: String)

    fun onTick(currentTimeMillis: Long = System.currentTimeMillis()) {
        val elapsedMs = currentTimeMillis - lastTickMillis
        if (elapsedMs <= 0) return
        val elapsedSeconds = (elapsedMs / 1000L).toInt()
        if (elapsedSeconds > 0) {
            // Passive stamina regen
            stamina = min(maxStamina, stamina + elapsedSeconds * STAMINA_REGEN_PER_SECOND)

            // Very small idle income based on levels
            val idleGold = (miningLevel + fishingLevel + fightingLevel) / 3
            gold += idleGold * elapsedSeconds

            // XP trickle
            experience += elapsedSeconds.toLong()
            recalculateLevel()

            lastTickMillis = currentTimeMillis
        }
    }

    fun performMining(): ActionResult {
        if (stamina < STAMINA_COST_MINING) return ActionResult("Too tired to mine!")
        stamina -= STAMINA_COST_MINING

        val baseYield = Random.nextInt(1, 3 + 1)
        val bonus = miningLevel / 3
        val total = baseYield + bonus
        ore += total

        // 10% chance for a gem worth gold
        val foundGem = Random.nextInt(100) < 10 + (miningLevel / 5)
        val gemGold = if (foundGem) Random.nextInt(5, 15) * miningLevel else 0
        if (gemGold > 0) gold += gemGold

        val xp = 2 + miningLevel / 2
        experience += xp
        recalculateLevel()

        return if (gemGold > 0) ActionResult("Mined $total ore and a gem (+$gemGold gold)")
        else ActionResult("Mined $total ore")
    }

    fun performFishing(): ActionResult {
        if (stamina < STAMINA_COST_FISHING) return ActionResult("Too tired to fish!")
        stamina -= STAMINA_COST_FISHING

        val baseYield = Random.nextInt(1, 2 + 1)
        val bonus = fishingLevel / 4
        val total = baseYield + bonus
        fish += total

        // 5% chance treasure chest
        val treasure = Random.nextInt(100) < 5 + (fishingLevel / 6)
        val treasureGold = if (treasure) Random.nextInt(10, 30) * max(1, fishingLevel / 2) else 0
        if (treasureGold > 0) gold += treasureGold

        val xp = 2 + fishingLevel / 2
        experience += xp
        recalculateLevel()

        return if (treasureGold > 0) ActionResult("Caught $total fish and treasure (+$treasureGold gold)")
        else ActionResult("Caught $total fish")
    }

    fun performFighting(): ActionResult {
        if (stamina < STAMINA_COST_FIGHTING) return ActionResult("Too tired to fight!")
        stamina -= STAMINA_COST_FIGHTING

        // Simple enemy model
        val enemyLevel = max(1, level + Random.nextInt(-1, 2))
        val enemyHp = 5 + enemyLevel * 3
        val playerDmg = 3 + fightingLevel * 2
        val rounds = (enemyHp + playerDmg - 1) / playerDmg

        val win = Random.nextInt(100) < 70 + (fightingLevel * 2)
        return if (win) {
            val rewardGold = 5L * enemyLevel + Random.nextInt(0, 6)
            val rewardXp = 4L + enemyLevel
            gold += rewardGold
            experience += rewardXp
            recalculateLevel()
            ActionResult("Defeated L$enemyLevel foe in $rounds rounds (+$rewardGold gold, +$rewardXp XP)")
        } else {
            val lossGold = min(5, gold.toInt()).toLong()
            gold -= lossGold
            ActionResult("Defeated by L$enemyLevel foe (-$lossGold gold)")
        }
    }

    private fun recalculateLevel() {
        // Level curve: level up every 50 XP increasing by 10 each level
        var required = 50L
        var newLevel = 1
        var xpLeft = experience
        while (xpLeft >= required) {
            xpLeft -= required
            newLevel += 1
            required += 10
        }
        if (newLevel != level) {
            val delta = newLevel - level
            level = newLevel
            // Reward max stamina and skill levels softly on level ups
            maxStamina += delta
            miningLevel += (delta + 1) / 2
            fishingLevel += (delta + 1) / 2
            fightingLevel += (delta + 1) / 2
        }
    }
}