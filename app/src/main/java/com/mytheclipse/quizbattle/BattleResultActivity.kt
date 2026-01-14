package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.mytheclipse.quizbattle.databinding.ActivityBattleResultBinding

/**
 * Displays battle results with victory/defeat status and earned rewards
 */
class BattleResultActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityBattleResultBinding
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        val isVictory = intent?.getBooleanExtra(EXTRA_IS_VICTORY, false) ?: false
        
        setupUI(isVictory)
        setupClickListeners()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupUI(isVictory: Boolean) {
        displayResultTitle(isVictory)
        displayRewards()
    }
    
    private fun displayResultTitle(isVictory: Boolean) {
        with(binding.resultTitleTextView) {
            text = getString(if (isVictory) R.string.victory else R.string.defeat)
            setTextColor(
                ContextCompat.getColor(
                    this@BattleResultActivity,
                    if (isVictory) R.color.primary_blue else R.color.defeat_red
                )
            )
        }
    }
    
    private fun displayRewards() {
        val rewards = extractRewards()
        
        with(binding) {
            correctAnswersTextView.text = PLACEHOLDER
            
            pointsEarnedTextView.text = formatReward(rewards.points)
            coinsEarnedTextView.text = formatReward(rewards.coins)
            xpEarnedTextView.text = formatReward(rewards.exp)
            
            pointsEarnedTextView.setTextColor(
                ContextCompat.getColor(
                    this@BattleResultActivity,
                    if (rewards.points > 0) R.color.primary_blue else R.color.text_secondary
                )
            )
        }
    }
    
    private fun extractRewards(): Rewards {
        return Rewards(
            points = intent.getIntExtra(EXTRA_EARNED_POINTS, DEFAULT_REWARD),
            coins = intent.getIntExtra(EXTRA_EARNED_COINS, DEFAULT_REWARD),
            exp = intent.getIntExtra(EXTRA_EARNED_EXP, DEFAULT_REWARD)
        )
    }
    
    private fun formatReward(value: Int): String {
        return if (value > 0) "+$value" else "0"
    }
    
    private fun setupClickListeners() {
        binding.backToMenuButton.setOnClickListener { 
            withDebounce { navigateToMain() } 
        }
        
        binding.playAgainButton.setOnClickListener {
            withDebounce { navigateToBattle() }
        }
    }
    
    private fun navigateToBattle() {
        val intent = android.content.Intent(this, BattleActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
    
    // endregion
    
    // region Data Classes
    
    private data class Rewards(
        val points: Int,
        val coins: Int,
        val exp: Int
    )
    
    // endregion
    
    companion object {
        const val EXTRA_IS_VICTORY = "extra_is_victory"
        private const val EXTRA_EARNED_POINTS = "EARNED_POINTS"
        private const val EXTRA_EARNED_COINS = "EARNED_COINS"
        private const val EXTRA_EARNED_EXP = "EARNED_EXP"
        
        private const val PLACEHOLDER = "-"
        private const val DEFAULT_REWARD = 0
    }
}
