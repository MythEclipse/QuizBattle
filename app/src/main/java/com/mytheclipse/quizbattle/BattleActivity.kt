package com.mytheclipse.quizbattle

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.mytheclipse.quizbattle.databinding.ActivityBattleBinding
import com.mytheclipse.quizbattle.viewmodel.BattleViewModel
import kotlinx.coroutines.launch

class BattleActivity : BaseActivity() {
    
    private lateinit var binding: ActivityBattleBinding
    private val battleViewModel: BattleViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    // Sound Manager
    private lateinit var soundManager: com.mytheclipse.quizbattle.util.SoundManager
    
    // Animation drawables
    private var knightAnimation: AnimationDrawable? = null
    private var goblinAnimation: AnimationDrawable? = null
    
    // Track previous animation states to avoid unnecessary restarts
    private var lastKnightAnimRes: Int = -1
    private var lastGoblinAnimRes: Int = -1
    private var lastKnightTranslation: Float = 0f
    private var lastGoblinTranslation: Float = 0f
    private var advanceScheduled: Boolean = false
    private var isNavigatingToResult: Boolean = false
    private var playerScore: Int = 0
    private var opponentScore: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        soundManager = com.mytheclipse.quizbattle.util.SoundManager(this)
        
        setupCharacterAnimations()
        setupAnswerButtons()
        observeBattleState()
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("BattleActivity", "onResume called - playing music")
        soundManager.playBattleMusic()
    }
    
    override fun onPause() {
        super.onPause()
        soundManager.pauseMusic()
    }
    // onDestroy is at the bottom, we will remove the duplicate one there

    
    private fun setupCharacterAnimations() {
        // Start knight idle animation
        binding.knightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
        knightAnimation = binding.knightImageView.background as? AnimationDrawable
        knightAnimation?.start()
        
        // Start goblin idle animation
        binding.goblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
        goblinAnimation = binding.goblinImageView.background as? AnimationDrawable
        goblinAnimation?.start()
    }
    
    private fun setupAnswerButtons() {
        val answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
        
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!battleViewModel.state.value.isAnswered) {
                    stopTimer()
                    battleViewModel.answerQuestion(index)
                }
            }
        }
    }
    
    private fun observeBattleState() {
        lifecycleScope.launch {
            battleViewModel.state.collect { state ->
                // Loading state
                binding.loadingProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Error handling
                state.error?.let { error ->
                    Toast.makeText(this@BattleActivity, error, Toast.LENGTH_LONG).show()
                }
                
                // Game over - wait for death animation to complete before navigating
                if (state.isGameOver && !isNavigatingToResult) {
                    isNavigatingToResult = true
                    
                    val isVictory = when {
                        state.opponentHealth <= 0 && state.playerHealth > 0 -> true
                        state.playerHealth <= 0 && state.opponentHealth > 0 -> false
                        else -> state.playerHealth > state.opponentHealth
                    }
                    
                    // Calculate death animation duration
                    // Knight dead: 6 frames × 150ms = 900ms
                    // Goblin dead: 10 frames × 80ms = 800ms
                    val deathAnimationDelay = 1500L
                    
                    handler.postDelayed({
                        if (!isFinishing) {
                            val intent = Intent(this@BattleActivity, BattleResultActivity::class.java).apply {
                                putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }, deathAnimationDelay)
                    
                    return@collect
                }
                
                // Auto-advance to next question after showing result for a moment
                if (state.isAnswered && !advanceScheduled) {
                    advanceScheduled = true
                    handler.postDelayed({
                        // Only proceed if still answered and not game over
                        val s = battleViewModel.state.value
                        if (s.isAnswered && !s.isGameOver) {
                            battleViewModel.nextQuestion()
                        }
                    }, 1000)
                } else if (!state.isAnswered) {
                    // Reset schedule flag when ready for new question
                    advanceScheduled = false
                }

                // Update UI
                if (state.questions.isNotEmpty()) {
                    updateQuestion(state)
                    updateHealthBars(state)
                    updateCharacterAnimations(state)
                    
                    // Start timer if not answered
                    if (!state.isAnswered) {
                        startTimer()
                    }
                }
            }
        }
    }
    
    private fun updateQuestion(state: com.mytheclipse.quizbattle.viewmodel.BattleState) {
        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        
        binding.questionProgressTextView.text = "Soal ${state.currentQuestionIndex + 1}/${state.questions.size}"
        binding.questionTextView.text = currentQuestion.text
        
        val answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
        
        currentQuestion.answers.forEachIndexed { index, option ->
            answerButtons.getOrNull(index)?.apply {
                text = option
                isEnabled = !state.isAnswered
                
                // Reset ALL visual states to default
                setBackgroundColor(Color.TRANSPARENT)
                strokeColor = ContextCompat.getColorStateList(context, R.color.primary_blue)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                alpha = 1f
                
                // Show correct/incorrect after answer
                if (state.isAnswered) {
                    state.selectedAnswerIndex.let { selected ->
                        when {
                            index == currentQuestion.correctAnswerIndex -> {
                                // Correct answer - blue background with white text
                                setBackgroundColor(ContextCompat.getColor(context, R.color.primary_blue))
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                            }
                            index == selected && index != currentQuestion.correctAnswerIndex -> {
                                // Wrong selected answer - red background with white text
                                setBackgroundColor(ContextCompat.getColor(context, R.color.primary_red))
                                setTextColor(ContextCompat.getColor(context, R.color.white))
                            }
                            else -> {
                                // Other buttons - keep default style
                                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                            }
                        }
                    }
                } else {
                    // Ensure text is visible when question is not answered
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }
            }
        }
    }
    
    private fun updateHealthBars(state: com.mytheclipse.quizbattle.viewmodel.BattleState) {
        binding.playerHealthBar.progress = state.playerHealth
        binding.playerHealthTextView.text = "HP: ${state.playerHealth}"
        
        binding.opponentHealthBar.progress = state.opponentHealth
        binding.opponentHealthTextView.text = "Opponent HP: ${state.opponentHealth}"
    }
    
    private fun updateCharacterAnimations(state: com.mytheclipse.quizbattle.viewmodel.BattleState) {
        // Calculate knight position and animation
        val knightTranslationX = when {
            state.playerAttacking -> 80f
            state.playerTookDamage -> -20f
            else -> 0f
        }
        
        // Only animate if position changed
        if (knightTranslationX != lastKnightTranslation) {
            binding.knightImageView.animate()
                .translationX(knightTranslationX)
                .setDuration(300)
                .start()
            lastKnightTranslation = knightTranslationX
        }
        
        // Calculate goblin position
        val goblinTranslationX = when {
            state.playerTookDamage -> -80f // Goblin attacks, moves forward (left)
            state.opponentTookDamage -> 20f // Goblin takes hit, recoils back
            else -> 0f
        }
        
        // Only animate if position changed
        if (goblinTranslationX != lastGoblinTranslation) {
            binding.goblinImageView.animate()
                .translationX(goblinTranslationX)
                .setDuration(300)
                .start()
            lastGoblinTranslation = goblinTranslationX
        }
        
        // Determine knight animation
        val knightAnimRes = when {
            state.playerHealth <= 0 -> R.drawable.anim_knight_dead
            state.playerTookDamage -> R.drawable.anim_knight_hurt
            state.playerAttacking -> R.drawable.anim_knight_attack
            else -> R.drawable.anim_knight_idle
        }
        
        // Only change animation if it's different
        if (knightAnimRes != lastKnightAnimRes) {
            val returnToIdle = knightAnimRes != R.drawable.anim_knight_idle && 
                              knightAnimRes != R.drawable.anim_knight_dead
            playKnightAnimation(knightAnimRes, returnToIdle)
            lastKnightAnimRes = knightAnimRes
            
            // Play sounds based on new state
            when {
                state.playerHealth <= 0 -> soundManager.playDieKnight() // Should play only once
                state.playerTookDamage -> soundManager.playHurtKnight()
                state.playerAttacking -> soundManager.playAttackKnight()
            }
            
            // Apply flash effect on damage
            if (state.playerTookDamage) {
                // Vibrate on damage using modern API
                vibrateDevice(200)
                flashCharacter(binding.knightImageView)
            }
        }
        
        // Determine goblin animation
        val goblinAnimRes = when {
            state.opponentHealth <= 0 -> R.drawable.anim_goblin_dead
            state.opponentTookDamage -> R.drawable.anim_goblin_hurt
            state.playerTookDamage -> R.drawable.anim_goblin_attack
            else -> R.drawable.anim_goblin_idle
        }
        
        // Only change animation if it's different
        if (goblinAnimRes != lastGoblinAnimRes) {
            val returnToIdle = goblinAnimRes != R.drawable.anim_goblin_idle && 
                              goblinAnimRes != R.drawable.anim_goblin_dead
            playGoblinAnimation(goblinAnimRes, returnToIdle)
            lastGoblinAnimRes = goblinAnimRes
            
            // Play sounds based on new state
            when {
                state.opponentHealth <= 0 -> soundManager.playDieMonster()
                state.opponentTookDamage -> soundManager.playHurtMonster()
                state.playerTookDamage -> soundManager.playAttackMonster() // Opponent/Goblin attacks when player takes damage
            }
            
            // Apply flash effect on damage
            if (state.opponentTookDamage) {
                flashCharacter(binding.goblinImageView)
            }
        }
    }
    
    private fun flashCharacter(imageView: android.widget.ImageView) {
        // White flash effect when damaged
        imageView.animate()
            .alpha(0.3f)
            .setDuration(150)
            .withEndAction {
                imageView.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun playKnightAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.knightImageView.setBackgroundResource(animationRes)
        knightAnimation = binding.knightImageView.background as? AnimationDrawable
        knightAnimation?.start()
        
        if (returnToIdle) {
            // Calculate animation duration for proper timing
            val duration = when (animationRes) {
                R.drawable.anim_knight_attack -> 500L // 5 frames * 100ms
                R.drawable.anim_knight_hurt -> 200L   // 2 frames * 100ms
                else -> 500L
            }
            
            handler.postDelayed({
                // Reset to idle only if not playing another animation
                if (lastKnightAnimRes == animationRes) {
                    binding.knightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
                    knightAnimation = binding.knightImageView.background as? AnimationDrawable
                    knightAnimation?.start()
                    lastKnightAnimRes = R.drawable.anim_knight_idle
                }
            }, duration)
        }
    }
    
    private fun playGoblinAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.goblinImageView.setBackgroundResource(animationRes)
        goblinAnimation = binding.goblinImageView.background as? AnimationDrawable
        goblinAnimation?.start()
        
        if (returnToIdle) {
            // Calculate animation duration for proper timing
            val duration = when (animationRes) {
                R.drawable.anim_goblin_attack -> 500L // 10 frames * 50ms
                R.drawable.anim_goblin_hurt -> 250L   // 5 frames * 50ms
                else -> 500L
            }
            
            handler.postDelayed({
                // Reset to idle only if not playing another animation
                if (lastGoblinAnimRes == animationRes) {
                    binding.goblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
                    goblinAnimation = binding.goblinImageView.background as? AnimationDrawable
                    goblinAnimation?.start()
                    lastGoblinAnimRes = R.drawable.anim_goblin_idle
                }
            }, duration)
        }
    }
    
    private fun startTimer() {
        stopTimer()
        var timeLeft = 10
        
        timerRunnable = object : Runnable {
            override fun run() {
                binding.timerTextView.text = "⏱ ${timeLeft}s"
                timeLeft--
                
                if (timeLeft >= 0 && !battleViewModel.state.value.isAnswered) {
                    handler.postDelayed(this, 1000)
                } else if (timeLeft < 0 && !battleViewModel.state.value.isAnswered) {
                    battleViewModel.timeUp()
                }
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
    
    private fun navigateToResult(isVictory: Boolean) {
        if (isFinishing) return
        
        val intent = Intent(this, BattleResultActivity::class.java).apply {
            putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
            putExtra("PLAYER_SCORE", playerScore)
            putExtra("OPPONENT_SCORE", opponentScore)
            putExtra("IS_ONLINE", false)
        }
        startActivity(intent)
        finish()
    }
    
    private fun vibrateDevice(durationMs: Long) {
        // Min SDK 26 supports VibrationEffect, so we can simplify
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): Use VibratorManager
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    durationMs,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            // Android 8-11 (API 26-30): Use getSystemService with class reference
            val vibrator = getSystemService(android.os.Vibrator::class.java)
            vibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    durationMs,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        handler.removeCallbacksAndMessages(null) // Cancel all pending handlers
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
