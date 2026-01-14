package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.GameHistoryAdapter
import com.mytheclipse.quizbattle.databinding.ActivityGameHistoryBinding
import com.mytheclipse.quizbattle.viewmodel.GameHistoryState
import com.mytheclipse.quizbattle.viewmodel.GameHistoryViewModel

/**
 * Activity for displaying the user's game history.
 * 
 * Shows a chronological list of past battles with results,
 * scores, and opponent information.
 */
class GameHistoryActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityGameHistoryBinding
    private val viewModel: GameHistoryViewModel by viewModels()
    private lateinit var adapter: GameHistoryAdapter
    
    // endregion

    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupRecyclerView()
        setupClickListeners()
        observeState()
    }
    
    // endregion

    // region Setup
    
    private fun setupRecyclerView() {
        adapter = GameHistoryAdapter()
        binding.feedRecyclerView?.apply {
            layoutManager = LinearLayoutManager(this@GameHistoryActivity)
            adapter = this@GameHistoryActivity.adapter
        }
    }
    
    private fun setupClickListeners() {
        binding.backButton?.setOnClickListener {
            navigateBack()
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeState() {
        collectState(viewModel.state) { state ->
            handleState(state)
        }
    }
    
    private fun handleState(state: GameHistoryState) {
        updateLoadingState(state.isLoading)
        updateHistoryList(state)
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun updateHistoryList(state: GameHistoryState) {
        val isEmpty = state.gameHistoryList.isEmpty() && !state.isLoading
        
        binding.emptyStateLayout?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.feedRecyclerView?.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        if (!isEmpty) {
            adapter.submitList(state.gameHistoryList)
        }
    }
    
    // endregion
}
