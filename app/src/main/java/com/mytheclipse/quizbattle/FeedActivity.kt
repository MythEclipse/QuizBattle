package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mytheclipse.quizbattle.databinding.ActivityFeedBinding

class FeedActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFeedBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupListeners()
        // TODO: Load game history when GameHistoryViewModel is ready
        binding.progressBar?.visibility = View.GONE
        binding.emptyStateLayout?.visibility = View.VISIBLE
    }
    
    private fun setupRecyclerView() {
        binding.feedRecyclerView?.layoutManager = LinearLayoutManager(this)
        // TODO: Create adapter for game history feed
    }
    
    private fun setupListeners() {
        binding.backButton?.setOnClickListener {
            finish()
        }
    }
}
