package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.mytheclipse.quizbattle.util.AuthUtils
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityMainBinding
import com.mytheclipse.quizbattle.viewmodel.MainViewModel
import com.mytheclipse.quizbattle.utils.enableEdgeToEdge
import com.mytheclipse.quizbattle.utils.applySystemBarInsets
import kotlinx.coroutines.launch

class MainActivity : BaseActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Apply padding for status/nav bars
        binding.root.applySystemBarInsets()
        
        setupListeners()
        observeMainState()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.refreshData()
    }
    
    private fun setupListeners() {
        binding.battleButton.setOnClickListener {
            startActivity(Intent(this, BattleActivity::class.java))
        }
        
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        binding.onlineButton.setOnClickListener {
            // Require login before going to Online
            lifecycleScope.launch {
                val tokenRepo = TokenRepository(application)
                val token = tokenRepo.getToken()
                if (token == null) {
                    startActivity(AuthUtils.createLoginIntent(this@MainActivity, LoginActivity.REDIRECT_ONLINE_MENU))
                } else {
                    startActivity(Intent(this@MainActivity, OnlineMenuActivity::class.java))
                }
            }
        }
        
        binding.feedButton.setOnClickListener {
            lifecycleScope.launch {
                val tokenRepo = TokenRepository(application)
                val token = tokenRepo.getToken()
                if (token == null) {
                    startActivity(AuthUtils.createLoginIntent(this@MainActivity, LoginActivity.REDIRECT_FEED))
                } else {
                    startActivity(Intent(this@MainActivity, FeedActivity::class.java))
                }
            }
        }

        binding.manageQuestionsButton.setOnClickListener {
            startActivity(Intent(this, QuestionManagementActivity::class.java))
        }
        
        binding.profileButton.setOnClickListener {
            lifecycleScope.launch {
                val tokenRepo = TokenRepository(application)
                val token = tokenRepo.getToken()
                if (token == null) {
                    startActivity(AuthUtils.createLoginIntent(this@MainActivity, LoginActivity.REDIRECT_PROFILE))
                } else {
                    startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                }
            }
        }
    }
    
    private fun observeMainState() {
        lifecycleScope.launch {
            mainViewModel.state.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                
                // Update user info
                val currentUser = state.currentUser
                val isLoggedIn = currentUser != null
                
                if (isLoggedIn) {
                    binding.userNameTextView.text = "Halo, ${currentUser!!.username}!"
                    binding.userPointsTextView.text = "Points: ${currentUser.points}"
                } else {
                    binding.userNameTextView.text = "Mode: Guest"
                    binding.userPointsTextView.text = "Login untuk menyimpan progress"
                }
                
                // Hide leaderboard when offline (not logged in)
                binding.leaderboardTitleTextView.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                binding.leaderboardLayout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                
                // Update leaderboard only if logged in
                if (isLoggedIn) {
                    updateLeaderboard(state.topUsers)
                }

                // Hide login button when logged in, show online/feed/profile buttons
                // Show login button when guest, hide online/feed/profile buttons
                binding.loginButton.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
                binding.onlineButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                binding.feedButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                binding.profileButton.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
                
                // Also hide the bottom buttons layout if not logged in
                binding.bottomButtonsLayout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
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
        
        // Extract real user data if available
        if (user != null && user is com.mytheclipse.quizbattle.data.local.entity.User) {
            playerName.text = user.username
            points.text = "${user.points} pts"
        } else {
            playerName.text = "-"
            points.text = "0 pts"
        }
        
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
