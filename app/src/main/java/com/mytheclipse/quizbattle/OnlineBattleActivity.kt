package com.mytheclipse.quizbattle

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
import com.mytheclipse.quizbattle.databinding.ActivityOnlineBattleBinding
import com.mytheclipse.quizbattle.viewmodel.OnlineGameViewModel
import kotlinx.coroutines.launch

class OnlineBattleActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnlineBattleBinding
    private val viewModel: OnlineGameViewModel by viewModels()
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    private var playerKnightAnimation: AnimationDrawable? = null
    private var opponentGoblinAnimation: AnimationDrawable? = null
    
    private var lastPlayerAnimRes: Int = -1
    private var lastOpponentAnimRes: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: ""
        if (matchId.isNotEmpty()) {
            viewModel.setMatchId(matchId)
            viewModel.connectToMatch(matchId)
        }
        
        setupCharacterAnimations()
        setupAnswerButtons()
        observeGameState()
    }
    
    private fun setupCharacterAnimations() {
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
                    val selectedAnswer = currentQuestion.options.getOrNull(index) ?: ""
                    val timeSpent = 30 - viewModel.state.value.timeLeft
                    viewModel.submitAnswer(currentQuestion.questionId, selectedAnswer, timeSpent)
                    
                    // Show answer feedback
                    val correctIndex = currentQuestion.options.indexOf(currentQuestion.correctAnswer)
                    handleAnswerSelection(index, correctIndex)
                }
            }
        }
    }
    
    private fun observeGameState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Update match ID
                binding.matchIdTextView.text = "Match #${state.matchId.takeLast(8)}"
                
                // Update scores
                binding.playerScoreTextView.text = state.playerScore.toString()
                binding.opponentScoreTextView.text = state.opponentScore.toString()
                
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(android.os.VibratorManager::class.java)
            vibratorManager?.defaultVibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    durationMs,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            val vibrator = getSystemService(android.os.Vibrator::class.java)
            vibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    durationMs,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
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
                    // Time's up - auto submit wrong answer
                    val currentQuestion = viewModel.state.value.currentQuestion
                    if (currentQuestion != null) {
                        viewModel.submitAnswer(currentQuestion.questionId, "", 30)
                        val correctIndex = currentQuestion.options.indexOf(currentQuestion.correctAnswer)
                        handleAnswerSelection(-1, correctIndex)
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
    }
    
    companion object {
        const val EXTRA_MATCH_ID = "extra_match_id"
    }
}
