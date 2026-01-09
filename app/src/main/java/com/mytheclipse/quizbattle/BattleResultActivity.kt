package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mytheclipse.quizbattle.databinding.ActivityBattleResultBinding

class BattleResultActivity : BaseActivity() {
    
    private lateinit var binding: ActivityBattleResultBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
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
            
            // Get rewards data
            val earnedPoints = intent.getIntExtra("EARNED_POINTS", 0)
            val earnedCoins = intent.getIntExtra("EARNED_COINS", 0)
            val earnedExp = intent.getIntExtra("EARNED_EXP", 0)
            
            // Display stats
            // Note: Correct Answers count is not currently passed from BattleActivity correctly (it passes opponent health as score?)
            // We'll leave it 0 or hide it if not available, OR implement passing it later. 
            // For now let's focus on rewards.
            binding.correctAnswersTextView.text = "-" // Placeholder
            
            binding.pointsEarnedTextView.text = "+$earnedPoints"
            binding.coinsEarnedTextView.text = "+$earnedCoins"
            binding.xpEarnedTextView.text = "+$earnedExp"

            if (earnedPoints > 0) {
                 binding.pointsEarnedTextView.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            } else {
                 binding.pointsEarnedTextView.text = "0"
                 binding.pointsEarnedTextView.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
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
