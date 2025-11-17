package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.databinding.ActivityMainBinding
import com.mytheclipse.quizbattle.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
        observeMainState()
        
        mainViewModel.refreshData()
    }
    
    private fun setupListeners() {
        binding.battleButton.setOnClickListener {
            startActivity(Intent(this, BattleActivity::class.java))
        }
        
        binding.onlineButton.setOnClickListener {
            startActivity(Intent(this, OnlineMenuActivity::class.java))
        }
        
        binding.feedButton.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
        }
        
        binding.profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
    
    private fun observeMainState() {
        lifecycleScope.launch {
            mainViewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Update user info
                val currentUser = state.currentUser
                if (currentUser != null) {
                    binding.userNameTextView.text = "Halo, ${currentUser.username}!"
                    binding.userPointsTextView.text = "Points: ${currentUser.points}"
                } else {
                    binding.userNameTextView.text = "Mode: Guest"
                    binding.userPointsTextView.text = "Login untuk menyimpan progress"
                }
                
                // Update leaderboard
                updateLeaderboard(state.topUsers)
            }
        }
    }
    
    private fun updateLeaderboard(topUsers: List<Any>) {
        // Update first place
        updatePodiumItem(
            binding.firstPlaceItem.root,
            1,
            topUsers.getOrNull(0),
            84
        )
        
        // Update second place
        updatePodiumItem(
            binding.secondPlaceItem.root,
            2,
            topUsers.getOrNull(1),
            74
        )
        
        // Update third place
        updatePodiumItem(
            binding.thirdPlaceItem.root,
            3,
            topUsers.getOrNull(2),
            74
        )
    }
    
    private fun updatePodiumItem(view: View, rank: Int, user: Any?, avatarSize: Int) {
        val rankBadge = view.findViewById<TextView>(R.id.rankBadge)
        val playerName = view.findViewById<TextView>(R.id.playerNameTextView)
        val points = view.findViewById<TextView>(R.id.pointsTextView)
        val avatar = view.findViewById<ImageView>(R.id.avatarImageView)
        
        rankBadge.text = rank.toString()
        
        // Since we don't have the User data class structure, use placeholder
        playerName.text = "Player $rank"
        points.text = "0 pts"
        
        // Adjust avatar size for first place
        val layoutParams = avatar.layoutParams
        layoutParams.width = avatarSize.dpToPx()
        layoutParams.height = avatarSize.dpToPx()
        avatar.layoutParams = layoutParams
    }
    
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
