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
    private var searchStartTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        lifecycleScope.launch {
            if (!requireLoginOrRedirect(LoginActivity.REDIRECT_ONLINE_MENU)) return@launch
            
            // Connect to WebSocket when activity opens
            matchmakingViewModel.connectWebSocket()
            
            setupListeners()
            observeMatchmakingState()
        }
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
        finish()
    }
    
    private fun showCreateRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Room")
        builder.setMessage("Room ID: ROOM${System.currentTimeMillis()}")
        builder.setPositiveButton("Create") { _, _ ->
            val matchId = "ROOM${System.currentTimeMillis()}"
            Toast.makeText(this, "Room created! Share ID: $matchId", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this, OnlineBattleActivity::class.java)
            intent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    private fun showJoinRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Join Room")
        
        val input = android.widget.EditText(this)
        input.hint = "Enter Room ID"
        builder.setView(input)
        
        builder.setPositiveButton("Join") { _, _ ->
            val roomId = input.text.toString().trim()
            if (roomId.isNotEmpty()) {
                val intent = Intent(this, OnlineBattleActivity::class.java)
                intent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, roomId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a room ID", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
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

