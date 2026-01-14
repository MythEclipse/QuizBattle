package com.mytheclipse.quizbattle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.mytheclipse.quizbattle.databinding.ActivitySplashBinding

/**
 * Splash screen shown at app launch
 * Displays branding and navigates to MainActivity after a delay
 */
class SplashActivity : BaseActivity() {
    
    // region Properties
    
    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private var navigationRunnable: Runnable? = null
    
    // endregion
    
    // region Lifecycle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)
        
        scheduleNavigation()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cancelScheduledNavigation()
    }
    
    // endregion
    
    // region Navigation
    
    private fun scheduleNavigation() {
        navigationRunnable = Runnable {
            navigateTo<MainActivity>(clearTask = true)
            finish()
        }
        handler.postDelayed(navigationRunnable!!, SPLASH_DELAY_MS)
    }
    
    private fun cancelScheduledNavigation() {
        navigationRunnable?.let { handler.removeCallbacks(it) }
        navigationRunnable = null
    }
    
    // endregion
    
    companion object {
        private const val SPLASH_DELAY_MS = 2000L
    }
}
