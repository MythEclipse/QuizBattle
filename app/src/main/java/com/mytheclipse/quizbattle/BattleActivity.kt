package com.mytheclipse.quizbattle

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.mytheclipse.quizbattle.databinding.ActivityBattleBinding
import com.mytheclipse.quizbattle.util.SoundManager
import com.mytheclipse.quizbattle.viewmodel.BattleState
import com.mytheclipse.quizbattle.viewmodel.BattleViewModel
import kotlinx.coroutines.launch

/**
 * Battle game screen with quiz questions, animations, and combat mechanics
 * Features: Timer, health bars, character animations, sound effects
 */
class BattleActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityBattleBinding
    private val viewModel: BattleViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    private lateinit var soundManager: SoundManager
    private var knightAnimation: AnimationDrawable? = null
    private var goblinAnimation: AnimationDrawable? = null
    
    // Animation state tracking
    private var lastKnightAnimRes: Int = ANIM_NONE
    private var lastGoblinAnimRes: Int = ANIM_NONE
    private var lastKnightTranslation: Float = 0f
    private var lastGoblinTranslation: Float = 0f
    
    private var advanceScheduled: Boolean = false
    private var isNavigatingToResult: Boolean = false
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        soundManager = SoundManager(this)
        
        setupCharacterAnimations()
        setupAnswerButtons()
        observeState()
    }
    
    override fun onResume() {
        super.onResume()
        logDebug("onResume - playing music")
        soundManager.playBattleMusic()
    }
    
    override fun onPause() {
        super.onPause()
        soundManager.pauseMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupCharacterAnimations() {
        playKnightAnimation(R.drawable.anim_knight_idle, returnToIdle = false)
        playGoblinAnimation(R.drawable.anim_goblin_idle, returnToIdle = false)
    }
    
    private fun setupAnswerButtons() {
        getAnswerButtons().forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!viewModel.state.value.isAnswered) {
                    stopTimer()
                    viewModel.answerQuestion(index)
                }
            }
        }
    }
    
    private fun getAnswerButtons(): List<MaterialButton> = listOf(
        binding.answerButton1,
        binding.answerButton2,
        binding.answerButton3,
        binding.answerButton4
    )
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: BattleState) {
        updateLoadingState(state.isLoading)
        handleError(state.error)
        
        if (state.isGameOver && !isNavigatingToResult) {
            handleGameOver(state)
            return
        }
        
        handleAutoAdvance(state)
        
        if (state.questions.isNotEmpty()) {
            updateQuestion(state)
            updateHealthBars(state)
            updateCharacterAnimations(state)
            
            if (!state.isAnswered) {
                startTimer()
            }
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingProgressBar.isVisible = isLoading
    }
    
    private fun handleError(error: String?) {
        error?.let { showToast(it) }
    }
    
    private fun handleGameOver(state: BattleState) {
        isNavigatingToResult = true
        val isVictory = determineVictory(state)
        
        handler.postDelayed({
            if (!isFinishing) {
                navigateToResult(isVictory)
            }
        }, DEATH_ANIMATION_DELAY)
    }
    
    private fun determineVictory(state: BattleState): Boolean {
        return when {
            state.opponentHealth <= 0 && state.playerHealth > 0 -> true
            state.playerHealth <= 0 && state.opponentHealth > 0 -> false
            else -> state.playerHealth > state.opponentHealth
        }
    }
    
    private fun handleAutoAdvance(state: BattleState) {
        if (state.isAnswered && !advanceScheduled) {
            advanceScheduled = true
            handler.postDelayed({
                val currentState = viewModel.state.value
                if (currentState.isAnswered && !currentState.isGameOver) {
                    viewModel.nextQuestion()
                }
            }, AUTO_ADVANCE_DELAY)
        } else if (!state.isAnswered) {
            advanceScheduled = false
        }
    }
    
    // endregion
    
    // region Question UI
    
    private fun updateQuestion(state: BattleState) {
        val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex) ?: return
        
        binding.questionProgressTextView.text = getString(
            R.string.question_of,
            state.currentQuestionIndex + 1,
            state.questions.size
        )
        binding.questionTextView.text = currentQuestion.text
        
        updateAnswerButtons(state, currentQuestion)
    }
    
    private fun updateAnswerButtons(
        state: BattleState,
        question: com.mytheclipse.quizbattle.data.local.entity.Question
    ) {
        getAnswerButtons().forEachIndexed { index, button ->
            button.apply {
                text = question.answers.getOrNull(index) ?: ""
                isEnabled = !state.isAnswered
                
                resetButtonStyle(this)
                
                if (state.isAnswered) {
                    applyAnswerFeedback(this, index, state.selectedAnswerIndex, question.correctAnswerIndex)
                }
            }
        }
    }
    
    private fun resetButtonStyle(button: MaterialButton) {
        button.apply {
            setBackgroundColor(Color.TRANSPARENT)
            strokeColor = ContextCompat.getColorStateList(context, R.color.primary_blue)
            setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            alpha = 1f
        }
    }
    
    private fun applyAnswerFeedback(
        button: MaterialButton,
        buttonIndex: Int,
        selectedIndex: Int?,
        correctIndex: Int
    ) {
        when {
            buttonIndex == correctIndex -> {
                button.setBackgroundColor(ContextCompat.getColor(button.context, R.color.primary_blue))
                button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
            }
            buttonIndex == selectedIndex && buttonIndex != correctIndex -> {
                button.setBackgroundColor(ContextCompat.getColor(button.context, R.color.primary_red))
                button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
            }
        }
    }
    
    // endregion
    
    // region Health Bars
    
    private fun updateHealthBars(state: BattleState) {
        with(binding) {
            playerHealthBar.progress = state.playerHealth
            playerHealthTextView.text = getString(R.string.hp_format, state.playerHealth)
            
            opponentHealthBar.progress = state.opponentHealth
            opponentHealthTextView.text = getString(R.string.opponent_hp_format, state.opponentHealth)
        }
    }
    
    // endregion
    
    // region Character Animations
    
    private fun updateCharacterAnimations(state: BattleState) {
        updateKnightAnimation(state)
        updateGoblinAnimation(state)
    }
    
    private fun updateKnightAnimation(state: BattleState) {
        val translationX = calculateKnightTranslation(state)
        animateCharacterPosition(binding.knightImageView, translationX, lastKnightTranslation) {
            lastKnightTranslation = translationX
        }
        
        val animRes = getKnightAnimationResource(state)
        if (animRes != lastKnightAnimRes) {
            playKnightAnimation(animRes, animRes != R.drawable.anim_knight_idle && animRes != R.drawable.anim_knight_dead)
            lastKnightAnimRes = animRes
            
            playKnightSounds(state, animRes)
            
            if (state.playerTookDamage) {
                vibrateDevice(VIBRATION_DURATION)
                flashCharacter(binding.knightImageView)
            }
        }
    }
    
    private fun updateGoblinAnimation(state: BattleState) {
        val translationX = calculateGoblinTranslation(state)
        animateCharacterPosition(binding.goblinImageView, translationX, lastGoblinTranslation) {
            lastGoblinTranslation = translationX
        }
        
        val animRes = getGoblinAnimationResource(state)
        if (animRes != lastGoblinAnimRes) {
            playGoblinAnimation(animRes, animRes != R.drawable.anim_goblin_idle && animRes != R.drawable.anim_goblin_dead)
            lastGoblinAnimRes = animRes
            
            playGoblinSounds(state, animRes)
            
            if (state.opponentTookDamage) {
                flashCharacter(binding.goblinImageView)
            }
        }
    }
    
    private fun calculateKnightTranslation(state: BattleState): Float {
        return when {
            state.playerAttacking -> ATTACK_TRANSLATION
            state.playerTookDamage -> -RECOIL_TRANSLATION
            else -> 0f
        }
    }
    
    private fun calculateGoblinTranslation(state: BattleState): Float {
        return when {
            state.playerTookDamage -> -ATTACK_TRANSLATION
            state.opponentTookDamage -> RECOIL_TRANSLATION
            else -> 0f
        }
    }
    
    private fun getKnightAnimationResource(state: BattleState): Int {
        return when {
            state.playerHealth <= 0 -> R.drawable.anim_knight_dead
            state.playerTookDamage -> R.drawable.anim_knight_hurt
            state.playerAttacking -> R.drawable.anim_knight_attack
            else -> R.drawable.anim_knight_idle
        }
    }
    
    private fun getGoblinAnimationResource(state: BattleState): Int {
        return when {
            state.opponentHealth <= 0 -> R.drawable.anim_goblin_dead
            state.opponentTookDamage -> R.drawable.anim_goblin_hurt
            state.playerTookDamage -> R.drawable.anim_goblin_attack
            else -> R.drawable.anim_goblin_idle
        }
    }
    
    private inline fun animateCharacterPosition(
        imageView: ImageView,
        targetTranslation: Float,
        lastTranslation: Float,
        onUpdate: () -> Unit
    ) {
        if (targetTranslation != lastTranslation) {
            imageView.animate()
                .translationX(targetTranslation)
                .setDuration(ANIMATION_DURATION)
                .start()
            onUpdate()
        }
    }
    
    private fun flashCharacter(imageView: ImageView) {
        imageView.animate()
            .alpha(FLASH_ALPHA)
            .setDuration(FLASH_DURATION)
            .withEndAction {
                imageView.animate()
                    .alpha(1f)
                    .setDuration(FLASH_DURATION)
                    .start()
            }
            .start()
    }
    
    private fun playKnightAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.knightImageView.setBackgroundResource(animationRes)
        knightAnimation = binding.knightImageView.background as? AnimationDrawable
        knightAnimation?.start()
        
        if (returnToIdle) {
            val duration = getAnimationDuration(animationRes, isKnight = true)
            handler.postDelayed({
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
            val duration = getAnimationDuration(animationRes, isKnight = false)
            handler.postDelayed({
                if (lastGoblinAnimRes == animationRes) {
                    binding.goblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
                    goblinAnimation = binding.goblinImageView.background as? AnimationDrawable
                    goblinAnimation?.start()
                    lastGoblinAnimRes = R.drawable.anim_goblin_idle
                }
            }, duration)
        }
    }
    
    private fun getAnimationDuration(animationRes: Int, isKnight: Boolean): Long {
        return when (animationRes) {
            R.drawable.anim_knight_attack -> KNIGHT_ATTACK_DURATION
            R.drawable.anim_knight_hurt -> KNIGHT_HURT_DURATION
            R.drawable.anim_goblin_attack -> GOBLIN_ATTACK_DURATION
            R.drawable.anim_goblin_hurt -> GOBLIN_HURT_DURATION
            else -> DEFAULT_ANIMATION_DURATION
        }
    }
    
    // endregion
    
    // region Sound Effects
    
    private fun playKnightSounds(state: BattleState, animRes: Int) {
        when {
            state.playerHealth <= 0 -> soundManager.playDieKnight()
            state.playerTookDamage -> soundManager.playHurtKnight()
            state.playerAttacking -> soundManager.playAttackKnight()
        }
    }
    
    private fun playGoblinSounds(state: BattleState, animRes: Int) {
        when {
            state.opponentHealth <= 0 -> soundManager.playDieMonster()
            state.opponentTookDamage -> soundManager.playHurtMonster()
            state.playerTookDamage -> soundManager.playAttackMonster()
        }
    }
    
    // endregion
    
    // region Timer
    
    private fun startTimer() {
        stopTimer()
        var timeLeft = QUESTION_TIME_SECONDS
        
        timerRunnable = object : Runnable {
            override fun run() {
                binding.timerTextView.text = getString(R.string.timer_format, timeLeft)
                timeLeft--
                
                if (timeLeft >= 0 && !viewModel.state.value.isAnswered) {
                    handler.postDelayed(this, TIMER_TICK_MS)
                } else if (timeLeft < 0 && !viewModel.state.value.isAnswered) {
                    viewModel.timeUp()
                }
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
    
    // endregion
    
    // region Navigation
    
    private fun navigateToResult(isVictory: Boolean) {
        val finalState = viewModel.state.value
        navigateTo<BattleResultActivity> {
            putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
            putExtra(EXTRA_EARNED_POINTS, finalState.earnedPoints)
            putExtra(EXTRA_EARNED_COINS, finalState.earnedCoins)
            putExtra(EXTRA_EARNED_EXP, finalState.earnedExp)
            putExtra(EXTRA_OPPONENT_SCORE, finalState.opponentHealth)
        }
        finish()
    }
    
    // endregion
    
    // region Utilities
    
    private fun vibrateDevice(durationMs: Long) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            val vibrator = getSystemService(Vibrator::class.java)
            vibrator?.vibrate(
                VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
    
    private fun cleanup() {
        stopTimer()
        handler.removeCallbacksAndMessages(null)
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
    
    // endregion
    
    companion object {
        private const val ANIM_NONE = -1
        
        // Timing constants
        private const val DEATH_ANIMATION_DELAY = 1500L
        private const val AUTO_ADVANCE_DELAY = 1000L
        private const val QUESTION_TIME_SECONDS = 10
        private const val TIMER_TICK_MS = 1000L
        private const val ANIMATION_DURATION = 300L
        private const val VIBRATION_DURATION = 200L
        
        // Animation durations
        private const val KNIGHT_ATTACK_DURATION = 500L
        private const val KNIGHT_HURT_DURATION = 200L
        private const val GOBLIN_ATTACK_DURATION = 500L
        private const val GOBLIN_HURT_DURATION = 250L
        private const val DEFAULT_ANIMATION_DURATION = 500L
        
        // Movement translations
        private const val ATTACK_TRANSLATION = 80f
        private const val RECOIL_TRANSLATION = 20f
        
        // Flash effect
        private const val FLASH_ALPHA = 0.3f
        private const val FLASH_DURATION = 150L
        
        // Intent extras
        private const val EXTRA_EARNED_POINTS = "EARNED_POINTS"
        private const val EXTRA_EARNED_COINS = "EARNED_COINS"
        private const val EXTRA_EARNED_EXP = "EARNED_EXP"
        private const val EXTRA_OPPONENT_SCORE = "OPPONENT_SCORE"
    }
}
