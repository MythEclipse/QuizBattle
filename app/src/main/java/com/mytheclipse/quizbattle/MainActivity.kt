package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mytheclipse.quizbattle.data.local.entity.User
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.databinding.ActivityMainBinding
import com.mytheclipse.quizbattle.util.AuthUtils
import com.mytheclipse.quizbattle.utils.applySystemBarInsets
import com.mytheclipse.quizbattle.utils.enableEdgeToEdge
import com.mytheclipse.quizbattle.viewmodel.MainState
import com.mytheclipse.quizbattle.viewmodel.MainViewModel
import kotlinx.coroutines.launch

/**
 * Main menu screen with navigation to all app features
 * Shows different options based on login status
 */
class MainActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var tokenRepository: TokenRepository
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsets()
        
        tokenRepository = TokenRepository(application)
        
        setupClickListeners()
        observeState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshData()
    }
    
    // endregion
    
    // region Setup
    
    private fun setupClickListeners() {
        with(binding) {
            battleButton.setOnClickListener { withDebounce { navigateToBattle() } }
            loginButton.setOnClickListener { withDebounce { navigateTo<LoginActivity>() } }
            onlineButton.setOnClickListener { withDebounce { navigateToOnline() } }
            feedButton.setOnClickListener { withDebounce { navigateToFeed() } }
            manageQuestionsButton.setOnClickListener { withDebounce { navigateTo<QuestionManagementActivity>() } }
            profileButton.setOnClickListener { withDebounce { navigateToProfile() } }
        }
    }
    
    // endregion
    
    // region Navigation
    
    private fun navigateToBattle() {
        navigateTo<BattleActivity>()
    }
    
    private fun navigateToOnline() {
        navigateWithLoginCheck(LoginActivity.REDIRECT_ONLINE_MENU) {
            navigateTo<OnlineMenuActivity>()
        }
    }
    
    private fun navigateToFeed() {
        navigateWithLoginCheck(LoginActivity.REDIRECT_FEED) {
            navigateTo<GameHistoryActivity>()
        }
    }
    
    private fun navigateToProfile() {
        navigateWithLoginCheck(LoginActivity.REDIRECT_PROFILE) {
            navigateTo<ProfileActivity>()
        }
    }
    
    private fun navigateWithLoginCheck(redirect: String, onLoggedIn: () -> Unit) {
        lifecycleScope.launch {
            val token = tokenRepository.getToken()
            if (token == null) {
                startActivity(AuthUtils.createLoginIntent(this@MainActivity, redirect))
            } else {
                onLoggedIn()
            }
        }
    }
    
    // endregion
    
    // region State Observation
    
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }
    
    private fun handleState(state: MainState) {
        updateLoadingState(state.isLoading)
        updateUserInfo(state.currentUser)
        updateLeaderboardVisibility(state.isLoggedIn)
        updateNavigationButtons(state.isLoggedIn)
        
        if (state.isLoggedIn) {
            updateLeaderboard(state.topUsers)
        }
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }
    
    private fun updateUserInfo(user: User?) {
        with(binding) {
            if (user != null) {
                userNameTextView.text = getString(R.string.hello_user, user.username)
                userPointsTextView.text = getString(R.string.points_format, user.points)
            } else {
                userNameTextView.text = getString(R.string.mode_guest)
                userPointsTextView.text = getString(R.string.login_to_save_progress)
            }
        }
    }
    
    private fun updateLeaderboardVisibility(isLoggedIn: Boolean) {
        with(binding) {
            leaderboardTitleTextView.isVisible = isLoggedIn
            leaderboardLayout.isVisible = isLoggedIn
        }
    }
    
    private fun updateNavigationButtons(isLoggedIn: Boolean) {
        with(binding) {
            loginButton.isVisible = !isLoggedIn
            onlineButton.isVisible = isLoggedIn
            feedButton.isVisible = isLoggedIn
            profileButton.isVisible = isLoggedIn
            bottomButtonsLayout.isVisible = isLoggedIn
        }
    }
    
    private fun updateLeaderboard(topUsers: List<Any>) {
        updatePodiumItem(
            view = binding.firstPlaceItem.root,
            rank = RANK_FIRST,
            user = topUsers.getOrNull(0),
            avatarSize = AVATAR_SIZE_FIRST
        )
        
        updatePodiumItem(
            view = binding.secondPlaceItem.root,
            rank = RANK_SECOND,
            user = topUsers.getOrNull(1),
            avatarSize = AVATAR_SIZE_OTHERS
        )
        
        updatePodiumItem(
            view = binding.thirdPlaceItem.root,
            rank = RANK_THIRD,
            user = topUsers.getOrNull(2),
            avatarSize = AVATAR_SIZE_OTHERS
        )
    }
    
    // endregion
    
    // region Leaderboard Helpers
    
    private fun updatePodiumItem(view: View, rank: Int, user: Any?, avatarSize: Int) {
        val rankBadge = view.findViewById<TextView>(R.id.rankBadge)
        val playerName = view.findViewById<TextView>(R.id.playerNameTextView)
        val points = view.findViewById<TextView>(R.id.pointsTextView)
        val avatar = view.findViewById<ImageView>(R.id.avatarImageView)
        
        rankBadge.text = rank.toString()
        
        val (name, pointsValue) = extractUserData(user)
        playerName.text = name
        points.text = getString(R.string.points_short_format, pointsValue)
        
        updateAvatarSize(avatar, avatarSize)
    }
    
    private fun extractUserData(user: Any?): Pair<String, Int> {
        return when (user) {
            is User -> user.username to user.points
            else -> PLACEHOLDER_NAME to DEFAULT_POINTS
        }
    }
    
    private fun updateAvatarSize(avatar: ImageView, sizeDp: Int) {
        val layoutParams = avatar.layoutParams
        layoutParams.width = sizeDp.dpToPx()
        layoutParams.height = sizeDp.dpToPx()
        avatar.layoutParams = layoutParams
    }
    
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
    
    // endregion
    
    companion object {
        private const val RANK_FIRST = 1
        private const val RANK_SECOND = 2
        private const val RANK_THIRD = 3
        
        private const val AVATAR_SIZE_FIRST = 84
        private const val AVATAR_SIZE_OTHERS = 74
        
        private const val PLACEHOLDER_NAME = "-"
        private const val DEFAULT_POINTS = 0
    }
}
