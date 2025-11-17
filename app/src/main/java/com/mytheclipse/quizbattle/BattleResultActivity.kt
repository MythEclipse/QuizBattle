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
        
        val isVictory = intent.getBooleanExtra(EXTRA_IS_VICTORY, false)
        
        setupUI(isVictory)
        setupListeners()
    }
    
    private fun setupUI(isVictory: Boolean) {
        if (isVictory) {
            binding.resultTitleTextView.text = getString(R.string.victory)
            binding.resultTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.primary_blue))
            binding.scoreValueTextView.text = "1000" // TODO: Get actual score
        } else {
            binding.resultTitleTextView.text = getString(R.string.defeat)
            binding.resultTitleTextView.setTextColor(ContextCompat.getColor(this, R.color.defeat_red))
            binding.scoreValueTextView.text = "500" // TODO: Get actual score
        }
    }
    
    private fun setupListeners() {
        binding.rematchButton.setOnClickListener {
            startActivity(Intent(this, BattleActivity::class.java))
            finish()
        }
        
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }
    }
    
    companion object {
        const val EXTRA_IS_VICTORY = "extra_is_victory"
    }
}
