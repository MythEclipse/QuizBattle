package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.LeaderboardAdapter
import com.mytheclipse.quizbattle.databinding.ActivityLeaderboardBinding
import com.mytheclipse.quizbattle.viewmodel.OnlineLeaderboardState
import com.mytheclipse.quizbattle.viewmodel.OnlineLeaderboardViewModel

/**
 * Activity for displaying the global leaderboard.
 * 
 * Shows player rankings fetched from the server with current user's rank
 * and total player count in a card view at the top.
 */
class LeaderboardActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityLeaderboardBinding
    private val viewModel: OnlineLeaderboardViewModel by viewModels()
    private val adapter = LeaderboardAdapter()
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        observeState()
    }
    
    // endregion

    // region Setup
    
    private fun setupRecyclerView() {
        binding.leaderboardRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LeaderboardActivity)
            adapter = this@LeaderboardActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            navigateBack()
        }
        
        binding.toggleButton.setOnClickListener {
            withDebounce { viewModel.refresh() }
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeState() {
        collectState(viewModel.state) { state ->
            handleState(state)
        }
    }
    
    private fun handleState(state: OnlineLeaderboardState) {
        updateLoadingState(state.isLoading)
        updateUserRankCard(state)
        updateLeaderboardList(state)
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.toggleButton.text = getString(R.string.refresh)
    }
    
    private fun updateUserRankCard(state: OnlineLeaderboardState) {
        binding.yourRankTextView.text = formatUserRank(state.userRank)
        binding.totalPlayersTextView.text = state.totalPlayers.toString()
    }
    
    private fun formatUserRank(rank: Int): String {
        return if (rank > 0) "#$rank" else NO_RANK_PLACEHOLDER
    }
    
    private fun updateLeaderboardList(state: OnlineLeaderboardState) {
        val isEmpty = state.leaderboard.isEmpty() && !state.isLoading
        
        binding.leaderboardRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        if (!isEmpty) {
            adapter.submitList(state.leaderboard)
        }
    }
    
    // endregion

    companion object {
        private const val NO_RANK_PLACEHOLDER = "#-"
    }
}
