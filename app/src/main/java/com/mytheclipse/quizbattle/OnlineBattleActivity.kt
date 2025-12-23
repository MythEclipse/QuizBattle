package com.mytheclipse.quizbattle

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.mytheclipse.quizbattle.databinding.ActivityOnlineBattleBinding
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.utils.MatchFoundDialogHelper
import com.mytheclipse.quizbattle.utils.MusicManager
import com.mytheclipse.quizbattle.utils.MusicTrack
import com.mytheclipse.quizbattle.utils.SoundEffect
import com.mytheclipse.quizbattle.utils.SoundManager
import com.mytheclipse.quizbattle.viewmodel.OnlineGameViewModel
import kotlinx.coroutines.launch

class OnlineBattleActivity : BaseActivity() {
    
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
    
    private lateinit var soundManager: SoundManager
    private lateinit var musicManager: MusicManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        soundManager = SoundManager.getInstance(this)
        musicManager = MusicManager.getInstance(this)
        
        // Start battle background music
        musicManager.playMusic(MusicTrack.BATTLE)
        
        lifecycleScope.launch {
            val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: ""
            
            // Get opponent info from intent extras
            val opponentName = intent.getStringExtra(EXTRA_OPPONENT_NAME) ?: "Opponent"
            val opponentLevel = intent.getIntExtra(EXTRA_OPPONENT_LEVEL, 1)
            val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "General"
            val difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "Normal"
            
            // Get player info
            val tokenRepository = TokenRepository(application)
            val playerName = tokenRepository.getUserName() ?: "You"
            
            // Show Match Found dialog before starting
            if (!matchFoundDialogShown && matchId.isNotEmpty()) {
                matchFoundDialogShown = true
                showMatchFoundDialog(
                    matchId = matchId,
                    playerName = playerName,
                    opponentName = opponentName,
                    opponentLevel = opponentLevel,
                    category = category,
                    difficulty = difficulty
                )
            } else if (matchId.isNotEmpty()) {
                startGame(matchId)
            }
        }
    }
    
    private fun showMatchFoundDialog(
        matchId: String,
        playerName: String,
        opponentName: String,
        opponentLevel: Int,
        category: String,
        difficulty: String
    ) {
        MatchFoundDialogHelper.showMatchFoundDialog(
            context = this,
            playerInfo = MatchFoundDialogHelper.PlayerInfo(
                name = playerName,
                level = 1,
                avatarRes = R.drawable.player_avatar
            ),
            opponentInfo = MatchFoundDialogHelper.PlayerInfo(
                name = opponentName,
                level = opponentLevel,
                avatarRes = R.drawable.bot_avatar
            ),
            matchInfo = MatchFoundDialogHelper.MatchInfo(
                matchId = matchId,
                category = category,
                difficulty = difficulty
            ),
            countdownSeconds = 5,
            onCountdownComplete = {
                startGame(matchId)
            }
        )
    }
    
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
            // Start player knight idle animation
            binding.playerKnightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
            playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
            playerKnightAnimation?.start()
            lastPlayerAnimRes = R.drawable.anim_knight_idle
            
            // Start opponent goblin idle animation
            binding.opponentGoblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
            opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
            opponentGoblinAnimation?.start()
            lastOpponentAnimRes = R.drawable.anim_goblin_idle
        } catch (e: Exception) {
            android.util.Log.e("OnlineBattle", "Failed to setup character animations", e)
        }
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
                val currentQuestion = viewModel.state.value.currentQuestion
                if (currentQuestion != null && !viewModel.state.value.isAnswered) {
                    stopTimer()
                    val timeSpentMs = (30 - viewModel.state.value.timeLeft) * 1000
                    viewModel.submitAnswer(
                        questionId = currentQuestion.questionId,
                        questionIndex = viewModel.state.value.currentQuestionIndex,
                        answerIndex = index,
                        answerTimeMs = timeSpentMs
                    )
                    
                    // Disable buttons while waiting for answer result
                    answerButtons.forEach { it.isEnabled = false }
                }
            }
        }
    }
    
    private fun observeGameState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Update match ID
                binding.matchIdTextView.text = "Match #${state.matchId.takeLast(8)}"
                
                // Update Health Bars
                binding.playerHealthBar.progress = state.playerHealth
                binding.opponentHealthBar.progress = state.opponentHealth
                
                // Update question progress
                binding.questionProgressTextView.text = 
                    "Question ${state.currentQuestionIndex + 1}/${state.totalQuestions}"
                
                // Update question
                state.currentQuestion?.let { question ->
                    updateQuestion(question, state)
                }
                
                // Show opponent waiting indicator
                if (state.isAnswered && !state.opponentAnswered) {
                    binding.opponentWaitingIndicator.visibility = View.VISIBLE
                } else {
                    binding.opponentWaitingIndicator.visibility = View.GONE
                }
                
                // Show answer feedback when answered
                if (state.isAnswered && state.correctAnswerIndex >= 0) {
                    handleAnswerSelection(state.selectedAnswerIndex, state.correctAnswerIndex)
                }
                
                // Update animations based on game state
                updateCharacterAnimations(state)
                
                // Handle game finish
                if (state.gameFinished) {
                    handler.postDelayed({
                        if (!isFinishing) {
                            navigateToResult(state.isVictory, state.playerScore, state.opponentScore)
                        }
                    }, 1000)
                }
                
                // Start timer if question loaded and not answered
                if (state.currentQuestion != null && !state.isAnswered) {
                    startTimer()
                }
                
                // Error handling
                state.error?.let { error ->
                    Toast.makeText(this@OnlineBattleActivity, error, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun updateQuestion(question: com.mytheclipse.quizbattle.data.repository.DataModels.Question, state: com.mytheclipse.quizbattle.viewmodel.OnlineGameState) {
        binding.questionTextView.text = question.questionText
        
        val answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
        
        question.options.forEachIndexed { index, option ->
            answerButtons.getOrNull(index)?.apply {
                text = option
                isEnabled = !state.isAnswered
                
                // Reset visual states
                setBackgroundColor(Color.TRANSPARENT)
                strokeColor = ContextCompat.getColorStateList(context, R.color.primary_blue)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                alpha = 1f
            }
        }
    }
    
    private fun handleAnswerSelection(selectedIndex: Int, correctIndex: Int) {
        val answerButtons = listOf(
            binding.answerButton1,
            binding.answerButton2,
            binding.answerButton3,
            binding.answerButton4
        )
        
        // Play sound effect based on answer correctness
        if (selectedIndex >= 0) {
            if (selectedIndex == correctIndex) {
                soundManager.playSound(SoundEffect.CORRECT_ANSWER)
            } else {
                soundManager.playSound(SoundEffect.WRONG_ANSWER)
            }
        }
        
        answerButtons.forEachIndexed { index, button ->
            button.isEnabled = false
            
            when {
                index == correctIndex -> {
                    // Show correct answer in blue
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_blue))
                    button.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
                index == selectedIndex && index != correctIndex -> {
                    // Show wrong answer in red
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_red))
                    button.setTextColor(ContextCompat.getColor(this, R.color.white))
                }
            }
        }
    }
    
    private fun updateCharacterAnimations(state: com.mytheclipse.quizbattle.viewmodel.OnlineGameState) {
        // Player animations
        val playerAnimRes = when {
            state.lastAnswerCorrect == true -> R.drawable.anim_knight_attack
            state.lastAnswerCorrect == false -> R.drawable.anim_knight_hurt
            else -> R.drawable.anim_knight_idle
        }
        
        if (playerAnimRes != lastPlayerAnimRes) {
            playPlayerAnimation(playerAnimRes, playerAnimRes != R.drawable.anim_knight_idle)
            lastPlayerAnimRes = playerAnimRes
            
            // Vibrate on wrong answer
            if (state.lastAnswerCorrect == false) {
                vibrateDevice(200)
            }
        }
        
        // Opponent animations (mirror player's result)
        val opponentAnimRes = when {
            state.lastAnswerCorrect == true -> R.drawable.anim_goblin_hurt
            state.lastAnswerCorrect == false -> R.drawable.anim_goblin_attack
            else -> R.drawable.anim_goblin_idle
        }
        
        if (opponentAnimRes != lastOpponentAnimRes) {
            playOpponentAnimation(opponentAnimRes, opponentAnimRes != R.drawable.anim_goblin_idle)
            lastOpponentAnimRes = opponentAnimRes
        }
    }
    
    private fun playPlayerAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.playerKnightImageView.setBackgroundResource(animationRes)
        playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
        playerKnightAnimation?.start()
        
        if (returnToIdle) {
            val duration = when (animationRes) {
                R.drawable.anim_knight_attack -> 500L
                R.drawable.anim_knight_hurt -> 200L
                else -> 500L
            }
            
            handler.postDelayed({
                if (lastPlayerAnimRes == animationRes) {
                    binding.playerKnightImageView.setBackgroundResource(R.drawable.anim_knight_idle)
                    playerKnightAnimation = binding.playerKnightImageView.background as? AnimationDrawable
                    playerKnightAnimation?.start()
                    lastPlayerAnimRes = R.drawable.anim_knight_idle
                }
            }, duration)
        }
    }
    
    private fun playOpponentAnimation(animationRes: Int, returnToIdle: Boolean) {
        binding.opponentGoblinImageView.setBackgroundResource(animationRes)
        opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
        opponentGoblinAnimation?.start()
        
        if (returnToIdle) {
            val duration = when (animationRes) {
                R.drawable.anim_goblin_attack -> 500L
                R.drawable.anim_goblin_hurt -> 250L
                else -> 500L
            }
            
            handler.postDelayed({
                if (lastOpponentAnimRes == animationRes) {
                    binding.opponentGoblinImageView.setBackgroundResource(R.drawable.anim_goblin_idle)
                    opponentGoblinAnimation = binding.opponentGoblinImageView.background as? AnimationDrawable
                    opponentGoblinAnimation?.start()
                    lastOpponentAnimRes = R.drawable.anim_goblin_idle
                }
            }, duration)
        }
    }
    
    private fun vibrateDevice(durationMs: Long) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(android.os.VibratorManager::class.java)
                vibratorManager?.defaultVibrator?.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        durationMs,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(android.os.Vibrator::class.java)
                vibrator?.vibrate(
                    android.os.VibrationEffect.createOneShot(
                        durationMs,
                        android.os.VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
        } catch (e: Exception) {
            // Check for specific exceptions if needed, but for vibration it's safe to just ignore failure
            Log.e("OnlineBattle", "Vibration failed", e)
        }
    }
    
    private fun startTimer() {
        stopTimer()
        var timeLeft = viewModel.state.value.timeLeft
        
        timerRunnable = object : Runnable {
            override fun run() {
                binding.timerTextView.text = "⏱ ${timeLeft}s"
                timeLeft--
                
                if (timeLeft >= 0 && !viewModel.state.value.isAnswered) {
                    handler.postDelayed(this, 1000)
                } else if (timeLeft < 0 && !viewModel.state.value.isAnswered) {
                    // Time's up - auto submit wrong answer (-1 index means no answer)
                    val currentQuestion = viewModel.state.value.currentQuestion
                    if (currentQuestion != null) {
                        viewModel.submitAnswer(
                            questionId = currentQuestion.questionId,
                            questionIndex = viewModel.state.value.currentQuestionIndex,
                            answerIndex = -1, // No answer selected
                            answerTimeMs = 30000 // 30 seconds
                        )
                    }
                }
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }
    
    private fun navigateToResult(isVictory: Boolean, playerScore: Int, opponentScore: Int) {
        // Play victory or defeat sound
        if (isVictory) {
            soundManager.playSound(SoundEffect.VICTORY)
        } else {
            soundManager.playSound(SoundEffect.DEFEAT)
        }
        
        val intent = Intent(this, BattleResultActivity::class.java).apply {
            putExtra(BattleResultActivity.EXTRA_IS_VICTORY, isVictory)
            putExtra("PLAYER_SCORE", playerScore)
            putExtra("OPPONENT_SCORE", opponentScore)
            putExtra("IS_ONLINE", true)
        }
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        handler.removeCallbacksAndMessages(null)
        musicManager.stopMusic()
    }
    
    companion object {
        const val EXTRA_MATCH_ID = "extra_match_id"
        const val EXTRA_OPPONENT_NAME = "extra_opponent_name"
        const val EXTRA_OPPONENT_LEVEL = "extra_opponent_level"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
    }
}
