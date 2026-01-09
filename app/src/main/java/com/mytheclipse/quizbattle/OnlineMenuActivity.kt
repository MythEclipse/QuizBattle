package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityOnlineMenuBinding
import com.mytheclipse.quizbattle.viewmodel.MatchmakingViewModel
import kotlinx.coroutines.launch

class OnlineMenuActivity : BaseActivity() {
    
    private lateinit var binding: ActivityOnlineMenuBinding
    private val matchmakingViewModel: MatchmakingViewModel by viewModels()
    
    private var isSearchingForMatch = false
    private var hasNavigated = false  // Guard against observer re-trigger
    private var searchStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        // Connect to WebSocket when activity opens
        matchmakingViewModel.connectWebSocket()
        
        // CRITICAL: Clear matchFound BEFORE setting up observer!
        // If we wait for onResume(), observer may trigger first!
        matchmakingViewModel.clearMatchFound()
        hasNavigated = false
        
        setupListeners()
        observeMatchmakingState()
    }
    
    override fun onResume() {
        super.onResume()
        // Double-clear for safety when returning from other activities
        matchmakingViewModel.clearMatchFound()
        hasNavigated = false
    }
    
    private fun setupListeners() {
        binding.backButton?.setOnClickListener {
            if (isSearchingForMatch) {
                // Cancel matchmaking if searching
                matchmakingViewModel.cancelMatchmaking()
                stopSearchTimer()
            }
            finish()
        }
        
        binding.quickMatchButton?.setOnClickListener {
            if (isSearchingForMatch) {
                // Cancel matchmaking
                matchmakingViewModel.cancelMatchmaking()
                stopSearchTimer()
            } else {
                // Start real matchmaking
                startRealMatchmaking()
            }
        }
        
        binding.createRoomButton?.setOnClickListener {
            showCreateRoomDialog()
        }
        
        binding.joinRoomButton?.setOnClickListener {
            showJoinRoomDialog()
        }
    }
    
    private fun observeMatchmakingState() {
        lifecycleScope.launch {
            matchmakingViewModel.state.collect { state ->
                isSearchingForMatch = state.isSearching
                
                // Update UI based on searching state
                updateSearchingUI(state.isSearching)
                
                // Handle match found
                state.matchFound?.let { matchData ->
                    // CRITICAL: Guard against observer re-trigger
                    if (hasNavigated) return@collect
                    hasNavigated = true
                    
                    stopSearchTimer()
                    navigateToOnlineBattle(
                        matchId = matchData.matchId,
                        opponentName = matchData.opponentName,
                        opponentLevel = matchData.opponentLevel,
                        category = matchData.category,
                        difficulty = matchData.difficulty
                    )
                }
                
                // Handle errors
                state.error?.let { error ->
                    Toast.makeText(this@OnlineMenuActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun startRealMatchmaking() {
        // Start real matchmaking via WebSocket
        matchmakingViewModel.findMatch(
            difficulty = "medium",
            category = "general"
        )
        
        // Start the search timer
        startSearchTimer()
        
        Toast.makeText(this, "Searching for opponent...", Toast.LENGTH_SHORT).show()
    }
    
    private fun startSearchTimer() {
        searchStartTime = System.currentTimeMillis()
        
        timerRunnable = object : Runnable {
            override fun run() {
                val elapsedSeconds = ((System.currentTimeMillis() - searchStartTime) / 1000).toInt()
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                
                val timeText = if (minutes > 0) {
                    String.format("Cancel Search (%d:%02d)", minutes, seconds)
                } else {
                    String.format("Cancel Search (%ds)", seconds)
                }
                
                binding.quickMatchButton?.text = timeText
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }
    
    private fun stopSearchTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }
    
    private fun updateSearchingUI(isSearching: Boolean) {
        binding.quickMatchButton?.apply {
            if (!isSearching) {
                text = "Quick Match"
            }
            // Timer updates the text when searching
        }
        
        // Disable other buttons while searching
        binding.createRoomButton?.isEnabled = !isSearching
        binding.joinRoomButton?.isEnabled = !isSearching
    }
    
    private fun navigateToOnlineBattle(
        matchId: String,
        opponentName: String,
        opponentLevel: Int,
        category: String,
        difficulty: String
    ) {
        val intent = Intent(this, OnlineBattleActivity::class.java).apply {
            putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
            putExtra(OnlineBattleActivity.EXTRA_OPPONENT_NAME, opponentName)
            putExtra(OnlineBattleActivity.EXTRA_OPPONENT_LEVEL, opponentLevel)
            putExtra(OnlineBattleActivity.EXTRA_CATEGORY, category)
            putExtra(OnlineBattleActivity.EXTRA_DIFFICULTY, difficulty)
        }
        
        startActivity(intent)
        
        // CRITICAL: Clear matchFound AFTER starting activity
        // If we clear before, new activity might observe old state!
        matchmakingViewModel.clearMatchFound()
        
        finish()
    }
    
    private fun showCreateRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Room")
        builder.setMessage("Private room creation is coming soon!\n\nFor now, use Quick Match to play with other players.")
        builder.setPositiveButton("OK", null)
        builder.show()
    }
    
    private fun showJoinRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Join Room")
        builder.setMessage("Private room feature is coming soon!\n\nFor now, use Quick Match to play with other players.")
        builder.setPositiveButton("OK", null)
        builder.show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopSearchTimer()
        handler.removeCallbacksAndMessages(null)
        // Cancel matchmaking if activity is destroyed
        if (isSearchingForMatch) {
            matchmakingViewModel.cancelMatchmaking()
        }
    }
}


