package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mytheclipse.quizbattle.databinding.ActivityBattleResultBinding

class BattleResultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBattleResultBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val isVictory = intent?.getBooleanExtra(EXTRA_IS_VICTORY, false) ?: false

        setupUI(isVictory)
        setupListeners()
    }
    
    private fun setupUI(isVictory: Boolean) {
        try {
            if (isVictory) {
                binding.resultTitleTextView.text = getString(R.string.victory)
                binding.resultTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            } else {
                binding.resultTitleTextView.text = getString(R.string.defeat)
                binding.resultTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.defeat_red))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            binding.resultTitleTextView.text = if (isVictory) "Victory!" else "Defeat"
        }
    }
    
    private fun setupListeners() {
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    companion object {
        const val EXTRA_IS_VICTORY = "extra_is_victory"
    }
}
