package com.mytheclipse.quizbattle

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.mytheclipse.quizbattle.databinding.ActivityOnlineBattleBinding
import com.mytheclipse.quizbattle.data.repository.DataModels.Question
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.utils.MatchFoundDialogHelper
import com.mytheclipse.quizbattle.utils.MusicManager
import com.mytheclipse.quizbattle.utils.MusicTrack
import com.mytheclipse.quizbattle.utils.SoundEffect
import com.mytheclipse.quizbattle.utils.SoundManager
import com.mytheclipse.quizbattle.viewmodel.OnlineGameState
import com.mytheclipse.quizbattle.viewmodel.OnlineGameViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

/**
 * Activity for real-time online battle matches.
 * 
 * Manages WebSocket-based multiplayer gameplay including:
 * - Question display and answer submission
 * - Character animations based on answer correctness
 * - Timer management and auto-submit on timeout
 * - Match state synchronization with opponent
 */
class OnlineBattleActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityOnlineBattleBinding
    private val viewModel: OnlineGameViewModel by viewModels()
    
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    private var playerKnightAnimation: AnimationDrawable? = null
    private var opponentGoblinAnimation: AnimationDrawable? = null
    
    private var lastPlayerAnimRes: Int = -1
    private var lastOpponentAnimRes: Int = -1
    
    private var matchFoundDialogShown = false
    private var gameReady = false
    private var isSubmitting = false
    private var questionStartTime: Long = 0
    
    private lateinit var soundManager: SoundManager
    private lateinit var musicManager: MusicManager
    
    private val answerButtons: List<MaterialButton> by lazy {
        listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
    }
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        initializeManagers()
        startBattleMusic()
        initializeMatch()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cleanupResources()
    }
    
    // endregion

    // region Initialization
    
    private fun initializeManagers() {
        soundManager = SoundManager.getInstance(this)
        musicManager = MusicManager.getInstance(this)
    }
    
    private fun startBattleMusic() {
        musicManager.playMusic(MusicTrack.BATTLE)
    }
    
    private fun initializeMatch() {
        lifecycleScope.launch {
            val matchData = extractMatchData()
            val playerName = getPlayerName()
            
            if (!matchFoundDialogShown && matchData.matchId.isNotEmpty()) {
                matchFoundDialogShown = true
                showMatchFoundDialog(playerName, matchData)
            } else if (matchData.matchId.isNotEmpty()) {
                startGame(matchData.matchId)
            }
        }
    }
    
    private fun extractMatchData(): MatchData = MatchData(
        matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: "",
        opponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: DEFAULT_OPPONENT_NAME,
        opponentLevel = intent.getIntExtra(EXTRA_OPPONENT_LEVEL, DEFAULT_LEVEL),
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: DEFAULT_CATEGORY,
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: DEFAULT_DIFFICULTY
    )
    
    private suspend fun getPlayerName(): String {
        val tokenRepository = TokenRepository(application)
        return tokenRepository.getUserName() ?: DEFAULT_PLAYER_NAME
    }
    
    // endregion

    // region Match Found Dialog
    
    private fun showMatchFoundDialog(playerName: String, matchData: MatchData) {
        MatchFoundDialogHelper.showMatchFoundDialog(
            context = this,
            playerInfo = MatchFoundDialogHelper.PlayerInfo(
                name = playerName,
                level = DEFAULT_LEVEL,
                avatarRes = R.drawable.player_avatar
            ),
            opponentInfo = MatchFoundDialogHelper.PlayerInfo(
                name = matchData.opponentName,
                level = matchData.opponentLevel,
                avatarRes = R.drawable.bot_avatar
            ),
            matchInfo = MatchFoundDialogHelper.MatchInfo(
                matchId = matchData.matchId,
                category = matchData.category,
                difficulty = matchData.difficulty
            ),
            countdownSeconds = COUNTDOWN_SECONDS,
            onCountdownComplete = { startGame(matchData.matchId) }
        )
    }
    
    // endregion

    // region Game Setup
    
    private fun startGame(matchId: String) {
        gameReady = true
        viewModel.setMatchId(matchId)
        viewModel.connectToMatch(matchId)
        setupCharacterAnimations()
        setupAnswerButtons()
        observeGameState()
    }
    
    private fun setupCharacterAnimations() {
        try {
            setupPlayerKnightAnimation()
            setupOpponentGoblinAnimation()
        } catch (e: Exception) {
            logError(TAG, "Failed to setup character animations", e)
        }
    }
    
    private fun setupPlayerKnightAnimation() {
        binding.playerKnightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
        playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
        playerKnightAnimation?.start()
        lastPlayerAnimRes = R.drawable.anim_knight_idle
    }
    
    private fun setupOpponentGoblinAnimation() {
        binding.opponentGoblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
        opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
        opponentGoblinAnimation?.start()
        lastOpponentAnimRes = R.drawable.anim_goblin_idle
    }
    
    private fun setupAnswerButtons() {
        answerButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!isSubmitting) {
                    handleAnswerClick(index)
                }
            }
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeGameState() {
        collectState(viewModel.state) { state ->
            handleGameState(state)
        }
    }
    
    private fun handleGameState(state: OnlineGameState) {
        updateMatchInfo(state)
        updateHealthBars(state)
        updateQuestionProgress(state)
        updateQuestion(state)
        updateOpponentWaitingIndicator(state)
        updateAnswerFeedback(state)
        updateCharacterAnimations(state)
        handleGameFinished(state)
        handleTimer(state)
        handleError(state)
    }
    
    private fun updateMatchInfo(state: OnlineGameState) {
        binding.matchIdTextView.text = getString(R.string.match_id_format, state.matchId.takeLast(MATCH_ID_DISPLAY_LENGTH))
    }
    
    private fun updateHealthBars(state: OnlineGameState) {
        binding.playerHealthBar.progress = state.playerHealth
        binding.opponentHealthBar.progress = state.opponentHealth
    }
    
    private fun updateQuestionProgress(state: OnlineGameState) {
        binding.questionProgressTextView.text = getString(
            R.string.question_progress_format,
            state.currentQuestionIndex + 1,
            state.totalQuestions
        )
    }
    
    private fun updateQuestion(state: OnlineGameState) {
        state.currentQuestion?.let { question ->
            displayQuestion(question, state.isAnswered)
        }
    }
    
    private fun displayQuestion(question: Question, isAnswered: Boolean) {
        binding.questionTextView.text = question.questionText
        
        question.options.forEachIndexed { index, option ->
            answerButtons.getOrNull(index)?.apply {
                text = option
                isEnabled = !isAnswered
                resetButtonStyle()
            }
        }
    }
    
    private fun MaterialButton.resetButtonStyle() {
        setBackgroundColor(Color.TRANSPARENT)
        strokeColor = ContextCompat.getColorStateList(context, R.color.primary_blue)
        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
        alpha = 1f
    }
    
    private fun updateOpponentWaitingIndicator(state: OnlineGameState) {
        binding.opponentWaitingIndicator.visibility = 
            if (state.isAnswered && !state.opponentAnswered) View.VISIBLE else View.GONE
    }
    
    private fun updateAnswerFeedback(state: OnlineGameState) {
        if (state.isAnswered && state.correctAnswerIndex >= 0) {
            showAnswerFeedback(state.selectedAnswerIndex, state.correctAnswerIndex)
        }
    }
    
    // endregion

    // region Answer Handling
    
    private fun handleAnswerClick(answerIndex: Int) {
        val currentQuestion = viewModel.state.value.currentQuestion ?: return
        if (viewModel.state.value.isAnswered) return
        
        isSubmitting = true
        disableAnswerButtons()
        stopTimer()
        
        val timeSpentMs = calculateTimeSpent()
        
        viewModel.submitAnswer(
            questionId = currentQuestion.questionId,
            questionIndex = viewModel.state.value.currentQuestionIndex,
            answerIndex = answerIndex,
            answerTimeMs = timeSpentMs.toInt()
        )
        
        scheduleSubmitGuardReset()
    }
    
    private fun calculateTimeSpent(): Long {
        return if (questionStartTime > 0) {
            System.currentTimeMillis() - questionStartTime
        } else {
            (QUESTION_TIME_SECONDS - viewModel.state.value.timeLeft) * 1000L
        }
    }
    
    private fun disableAnswerButtons() {
        answerButtons.forEach {
            it.isEnabled = false
            it.alpha = DISABLED_BUTTON_ALPHA
        }
    }
    
    private fun enableAnswerButtons() {
        answerButtons.forEach {
            it.isEnabled = true
            it.alpha = 1.0f
        }
    }
    
    private fun scheduleSubmitGuardReset() {
        handler.postDelayed({
            if (isSubmitting) {
                isSubmitting = false
                if (!viewModel.state.value.isAnswered) {
                    enableAnswerButtons()
                }
            }
        }, SUBMIT_GUARD_TIMEOUT)
    }
    
    private fun showAnswerFeedback(selectedIndex: Int, correctIndex: Int) {
        playAnswerSound(selectedIndex, correctIndex)
        
        answerButtons.forEachIndexed { index, button ->
            button.isEnabled = false
            styleAnswerButton(button, index, selectedIndex, correctIndex)
        }
    }
    
    private fun playAnswerSound(selectedIndex: Int, correctIndex: Int) {
        if (selectedIndex >= 0) {
            val soundEffect = if (selectedIndex == correctIndex) {
                SoundEffect.CORRECT_ANSWER
            } else {
                SoundEffect.WRONG_ANSWER
            }
            soundManager.playSound(soundEffect)
        }
    }
    
    private fun styleAnswerButton(
        button: MaterialButton,
        buttonIndex: Int,
        selectedIndex: Int,
        correctIndex: Int
    ) {
        when {
            buttonIndex == correctIndex -> {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
            buttonIndex == selectedIndex && buttonIndex != correctIndex -> {
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_red))
                button.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }
    
    // endregion

    // region Character Animations
    
    private fun updateCharacterAnimations(state: OnlineGameState) {
        updatePlayerAnimation(state.lastAnswerCorrect)
        updateOpponentAnimation(state.lastAnswerCorrect)
    }
    
    private fun updatePlayerAnimation(lastAnswerCorrect: Boolean?) {
        val animRes = when (lastAnswerCorrect) {
            true -> R.drawable.anim_knight_attack
            false -> R.drawable.anim_knight_hurt
            null -> R.drawable.anim_knight_idle
        }
        
        if (animRes != lastPlayerAnimRes) {
            playPlayerAnimation(animRes, animRes != R.drawable.anim_knight_idle)
            lastPlayerAnimRes = animRes
            
            if (lastAnswerCorrect == false) {
                vibrateDevice(VIBRATE_DURATION_MS)
            }
        }
    }
    
    private fun updateOpponentAnimation(lastAnswerCorrect: Boolean?) {
        val animRes = when (lastAnswerCorrect) {
            true -> R.drawable.anim_goblin_hurt
            false -> R.drawable.anim_goblin_attack
            null -> R.drawable.anim_goblin_idle
        }
        
        if (animRes != lastOpponentAnimRes) {
            playOpponentAnimation(animRes, animRes != R.drawable.anim_goblin_idle)
            lastOpponentAnimRes = animRes
        }
    }
    
    private fun playPlayerAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.playerKnightImageView.setBackgroundResource(animationRes)
        playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
        playerKnightAnimation?.start()
        
        if (returnToIdle) {
            scheduleReturnToPlayerIdle(animationRes)
        }
    }
    
    private fun scheduleReturnToPlayerIdle(animationRes: Int) {
        val duration = getAnimationDuration(animationRes, isPlayer = true)
        
        handler.postDelayed({
            if (lastPlayerAnimRes == animationRes) {
                binding.playerKnightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
                playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
                playerKnightAnimation?.start()
                lastPlayerAnimRes = R.drawable.anim_knight_idle
            }
        }, duration)
    }
    
    private fun playOpponentAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.opponentGoblinImageView.setBackgroundResource(animationRes)
        opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
        opponentGoblinAnimation?.start()
        
        if (returnToIdle) {
            scheduleReturnToOpponentIdle(animationRes)
        }
    }
    
    private fun scheduleReturnToOpponentIdle(animationRes: Int) {
        val duration = getAnimationDuration(animationRes, isPlayer = false)
        
        handler.postDelayed({
            if (lastOpponentAnimRes == animationRes) {
                binding.opponentGoblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
                opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
                opponentGoblinAnimation?.start()
                lastOpponentAnimRes = R.drawable.anim_goblin_idle
            }
        }, duration)
    }
    
    private fun getAnimationDuration(animationRes: Int, isPlayer: Boolean): Long {
        return when {
            isPlayer && animationRes == R.drawable.anim_knight_attack -> ATTACK_ANIM_DURATION
            isPlayer && animationRes == R.drawable.anim_knight_hurt -> KNIGHT_HURT_ANIM_DURATION
            !isPlayer && animationRes == R.drawable.anim_goblin_attack -> ATTACK_ANIM_DURATION
            !isPlayer && animationRes == R.drawable.anim_goblin_hurt -> GOBLIN_HURT_ANIM_DURATION
            else -> DEFAULT_ANIM_DURATION
        }
    }
    
    // endregion

    // region Timer Management
    
    private fun handleTimer(state: OnlineGameState) {
        if (state.currentQuestion != null && !state.isAnswered) {
            if (questionStartTime == 0L) {
                questionStartTime = System.currentTimeMillis()
            }
            startTimer()
        } else if (state.isAnswered) {
            questionStartTime = 0
            isSubmitting = false
        }
    }
    
    private fun startTimer() {
        stopTimer()
        var timeLeft = viewModel.state.value.timeLeft
        
        timerRunnable = object : Runnable {
            override fun run() {
                binding.timerTextView.text = getString(R.string.timer_format, timeLeft)
                timeLeft--
                
                if (timeLeft >= 0 && !viewModel.state.value.isAnswered) {
                    handler.postDelayed(this, TIMER_INTERVAL)
                } else if (timeLeft < 0 && !viewModel.state.value.isAnswered) {
                    submitTimeoutAnswer()
                }
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun submitTimeoutAnswer() {
        val currentQuestion = viewModel.state.value.currentQuestion ?: return
        
        viewModel.submitAnswer(
            questionId = currentQuestion.questionId,
            questionIndex = viewModel.state.value.currentQuestionIndex,
            answerIndex = NO_ANSWER_INDEX,
            answerTimeMs = TIMEOUT_ANSWER_TIME_MS
        )
    }
    
    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }
    
    // endregion

    // region Game End
    
    private fun handleGameFinished(state: OnlineGameState) {
        if (state.gameFinished) {
            handler.postDelayed({
                if (!isFinishing) {
                    navigateToResult(state.isVictory)
                }
            }, RESULT_NAVIGATION_DELAY)
        }
    }
    
    private fun handleError(state: OnlineGameState) {
        state.error?.let { error ->
            showToast(error)
        }
    }
    
    // endregion

    // region Navigation
    
    private fun navigateToResult(isVictory: Boolean) {
        playResultSound(isVictory)
        
        navigateTo<BattleResultActivity> {
            putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
        }
        finish()
    }
    
    private fun playResultSound(isVictory: Boolean) {
        val soundEffect = if (isVictory) SoundEffect.VICTORY else SoundEffect.DEFEAT
        soundManager.playSound(soundEffect)
    }
    
    // endregion

    // region Utilities
    
    private fun vibrateDevice(durationMs: Long) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Vibrator::class.java)
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }
    
    private fun cleanupResources() {
        stopTimer()
        handler.removeCallbacksAndMessages(null)
        musicManager.stopMusic()
    }
    
    // endregion

    // region Data Classes
    
    private data class MatchData(
        val matchId: String,
        val opponentName: String,
        val opponentLevel: Int,
        val category: String,
        val difficulty: String
    )
    
    // endregion

    companion object {
        private const val TAG = "OnlineBattleActivity"
        
        const val EXTRA_MATCH_ID = "extra_match_id"
        const val EXTRA_OPPONENT_NAME = "extra_opponent_name"
        const val EXTRA_OPPONENT_LEVEL = "extra_opponent_level"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        
        private const val DEFAULT_OPPONENT_NAME = "Opponent"
        private const val DEFAULT_PLAYER_NAME = "You"
        private const val DEFAULT_LEVEL = 1
        private const val DEFAULT_CATEGORY = "General"
        private const val DEFAULT_DIFFICULTY = "Normal"
        
        private const val COUNTDOWN_SECONDS = 5
        private const val QUESTION_TIME_SECONDS = 30
        private const val MATCH_ID_DISPLAY_LENGTH = 8
        
        private const val NO_ANSWER_INDEX = -1
        private const val TIMEOUT_ANSWER_TIME_MS = 30000
        
        private const val TIMER_INTERVAL = 1000L
        private const val RESULT_NAVIGATION_DELAY = 1000L
        private const val SUBMIT_GUARD_TIMEOUT = 3000L
        
        private const val DISABLED_BUTTON_ALPHA = 0.5f
        
        private const val ATTACK_ANIM_DURATION = 500L
        private const val KNIGHT_HURT_ANIM_DURATION = 200L
        private const val GOBLIN_HURT_ANIM_DURATION = 250L
        private const val DEFAULT_ANIM_DURATION = 500L
        private const val VIBRATE_DURATION_MS = 200L
    }
}
