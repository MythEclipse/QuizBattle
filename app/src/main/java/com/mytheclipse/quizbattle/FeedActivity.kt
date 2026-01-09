package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.adapter.GameHistoryAdapter
import com.mytheclipse.quizbattle.databinding.ActivityFeedBinding
import com.mytheclipse.quizbattle.viewmodel.GameHistoryViewModel
import kotlinx.coroutines.launch

class FeedActivity : BaseActivity() {
    
    private lateinit var binding: ActivityFeedBinding
    private val gameHistoryViewModel: GameHistoryViewModel by viewModels()
    private lateinit var gameHistoryAdapter: GameHistoryAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        setupRecyclerView()
        setupListeners()
        observeGameHistory()
    }
    
    private fun setupRecyclerView() {
        gameHistoryAdapter = GameHistoryAdapter()
        binding.feedRecyclerView?.layoutManager = LinearLayoutManager(this)
        binding.feedRecyclerView?.adapter = gameHistoryAdapter
    }
    
    private fun setupListeners() {
        binding.backButton?.setOnClickListener {
            finish()
        }
    }
    
    private fun observeGameHistory() {
        lifecycleScope.launch {
            gameHistoryViewModel.state.collect { state ->
                binding.progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                if (state.gameHistoryList.isEmpty() && !state.isLoading) {
                    binding.emptyStateLayout?.visibility = View.VISIBLE
                    binding.feedRecyclerView?.visibility = View.GONE
                } else {
                    binding.emptyStateLayout?.visibility = View.GONE
                    binding.feedRecyclerView?.visibility = View.VISIBLE
                    gameHistoryAdapter.submitList(state.gameHistoryList)
                }
            }
        }
    }
}
