package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.LeaderboardAdapter
import com.mytheclipse.quizbattle.databinding.ActivityLeaderboardBinding
import com.mytheclipse.quizbattle.viewmodel.OnlineLeaderboardViewModel
import kotlinx.coroutines.launch

class LeaderboardActivity : BaseActivity() {
    
    private lateinit var binding: ActivityLeaderboardBinding
    private val viewModel: OnlineLeaderboardViewModel by viewModels()
    private val adapter = LeaderboardAdapter()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupViews()
        setupListeners()
        observeState()
    }
    
    private fun setupViews() {
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.toggleButton.setOnClickListener {
            viewModel.toggleFriendsOnly()
        }
    }
    
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Loading state
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Update toggle button text
                binding.toggleButton.text = if (state.showFriendsOnly) "Friends" else "Global"
                
                // User rank card
                binding.yourRankTextView.text = if (state.userRank > 0) "#${state.userRank}" else "#-"
                binding.totalPlayersTextView.text = state.totalPlayers.toString()
                
                // Leaderboard list
                if (state.leaderboard.isEmpty() && !state.isLoading) {
                    binding.leaderboardRecyclerView.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.leaderboardRecyclerView.visibility = View.VISIBLE
                    binding.emptyStateLayout.visibility = View.GONE
                    adapter.submitList(state.leaderboard)
                }
            }
        }
    }
}
