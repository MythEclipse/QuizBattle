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
import com.mytheclipse.quizbattle.ui.MatchConfirmationDialog
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
    
    private var confirmationDialog: MatchConfirmationDialog? = null
    
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
        matchmakingViewModel.clearConfirmRequest()
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
        
        // Add friend battle button
        binding.friendBattleButton?.setOnClickListener {
            val intent = Intent(this, FriendListActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun observeMatchmakingState() {
        lifecycleScope.launch {
            matchmakingViewModel.state.collect { state ->
                isSearchingForMatch = state.isSearching
                
                // Update UI based on searching state
                updateSearchingUI(state.isSearching)
                
                // Restore timer if searching and timer not running
                if (state.isSearching && timerRunnable == null) {
                    state.searchStartTime?.let { startTime ->
                        startSearchTimer(startTime)
                    }
                } else if (!state.isSearching) {
                    stopSearchTimer()
                }
                
                // Handle confirmation request (new flow with confirmation)
                state.confirmRequest?.let { confirmData ->
                    showMatchConfirmationDialog(confirmData)
                }
                
                // Handle confirm status updates
                state.confirmStatus?.let { statusData ->
                    confirmationDialog?.updateWaitingStatus(
                        statusData.status,
                        statusData.confirmedCount,
                        statusData.totalPlayers
                    )
                }
                
                // Handle match found (after confirmation or direct match)
                state.matchFound?.let { matchData ->
                    // CRITICAL: Guard against observer re-trigger
                    if (hasNavigated) return@collect
                    hasNavigated = true
                    
                    // Dismiss confirmation dialog if showing
                    confirmationDialog?.dismiss()
                    confirmationDialog = null
                    
                    stopSearchTimer()
                    navigateToOnlineBattle(
                        matchData.matchId,
                        matchData.opponentName,
                        matchData.opponentLevel,
                        matchData.category,
                        matchData.difficulty
                    )
                }
                
                // Handle errors
                state.error?.let { error ->
                    Toast.makeText(this@OnlineMenuActivity, error, Toast.LENGTH_SHORT).show()
                    matchmakingViewModel.clearError()
                }
            }
        }
    }
    
    private fun showMatchConfirmationDialog(confirmData: com.mytheclipse.quizbattle.viewmodel.ConfirmRequestData) {
        // Dismiss existing dialog if any
        confirmationDialog?.dismiss()
        
        confirmationDialog = MatchConfirmationDialog.newInstance(
            matchId = confirmData.matchId,
            opponentName = confirmData.opponentName,
            opponentLevel = confirmData.opponentLevel,
            opponentPoints = confirmData.opponentPoints,
            opponentAvatarUrl = confirmData.opponentAvatar,
            difficulty = confirmData.difficulty,
            category = confirmData.category,
            totalQuestions = confirmData.totalQuestions,
            timeoutSeconds = confirmData.expiresIn / 1000
        ).apply {
            setOnAcceptListener { matchId ->
                matchmakingViewModel.confirmMatch(matchId, true)
            }
            setOnDeclineListener { matchId ->
                matchmakingViewModel.confirmMatch(matchId, false)
                matchmakingViewModel.clearConfirmRequest()
            }
        }
        
        confirmationDialog?.show(supportFragmentManager, MatchConfirmationDialog.TAG)
    }
    
    private fun startRealMatchmaking() {
        // Start real matchmaking via WebSocket
        matchmakingViewModel.findMatch(
            difficulty = "medium",
            category = "general"
        )
        
        // Start the search timer (will use current time)
        startSearchTimer()
        
        Toast.makeText(this, "Searching for opponent...", Toast.LENGTH_SHORT).show()
    }
    
    private fun startSearchTimer(startTime: Long? = null) {
        // Use provided start time or current time
        searchStartTime = startTime ?: System.currentTimeMillis()
        
        // Stop any existing timer first
        stopSearchTimer()
        
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
        binding.friendBattleButton?.isEnabled = !isSearching
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
        // Cancel matchmaking ONLY if activity is finishing (user pressed back)
        // do not cancel on rotation
        if (isFinishing && isSearchingForMatch) {
            matchmakingViewModel.cancelMatchmaking()
        }
    }
}


