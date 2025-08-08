package com.example.pixelclicker

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.pixelclicker.databinding.ActivityMainBinding
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameState: GameState
    private lateinit var persistence: GamePersistence

    private val uiHandler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            gameState.onTick()
            updateUi()
            persistence.save(gameState)
            uiHandler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        persistence = GamePersistence(this)
        gameState = persistence.load() ?: GameState()
        updateUi()

        binding.btnMine.setOnClickListener {
            val result = gameState.performMining()
            toast(result.description)
            updateUi()
        }
        binding.btnFish.setOnClickListener {
            val result = gameState.performFishing()
            toast(result.description)
            updateUi()
        }
        binding.btnFight.setOnClickListener {
            val result = gameState.performFighting()
            toast(result.description)
            updateUi()
        }
    }

    override fun onStart() {
        super.onStart()
        uiHandler.post(tickRunnable)
    }

    override fun onStop() {
        super.onStop()
        uiHandler.removeCallbacks(tickRunnable)
        persistence.save(gameState)
    }

    private fun updateUi() {
        binding.tvGold.text = "Gold: ${gameState.gold}"
        binding.tvOre.text = "Ore: ${gameState.ore}"
        binding.tvFish.text = "Fish: ${gameState.fish}"
        binding.tvXP.text = "XP: ${gameState.experience} (Lv ${gameState.level})"
        binding.tvStamina.text = "Stamina: ${gameState.stamina}/${gameState.maxStamina}"

        binding.btnMine.isEnabled = gameState.stamina >= GameState.STAMINA_COST_MINING
        binding.btnFish.isEnabled = gameState.stamina >= GameState.STAMINA_COST_FISHING
        binding.btnFight.isEnabled = gameState.stamina >= GameState.STAMINA_COST_FIGHTING
    }

    private fun toast(text: String) {
        android.widget.Toast.makeText(this, text, android.widget.Toast.LENGTH_SHORT).show()
    }
}