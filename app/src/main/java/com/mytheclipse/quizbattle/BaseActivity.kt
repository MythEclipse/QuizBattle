package com.mytheclipse.quizbattle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.utils.LocaleHelper
import com.mytheclipse.quizbattle.utils.ResultDialogHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base Activity that provides common functionality for all activities:
 * - Edge-to-edge display with system bar padding
 * - Locale handling
 * - Authentication checking
 * - Click debouncing
 * - State collection utilities
 * - Navigation helpers
 * - Toast/Dialog helpers
 */
abstract class BaseActivity : AppCompatActivity() {
    
    // region Click Debouncing
    
    private var lastClickTime = 0L
    
    /**
     * Checks if a click action can be performed (debounce check)
     * @return true if enough time has passed since last click
     */
    protected fun canClick(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DEBOUNCE_MS) return false
        lastClickTime = currentTime
        return true
    }
    
    /**
     * Execute action only if click debounce passes
     */
    protected inline fun withDebounce(action: () -> Unit) {
        if (canClick()) action()
    }
    
    // endregion
    
    // region Lifecycle
    
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    // endregion
    
    // region System Bar Padding

    /**
     * Apply system bar insets as padding to a view
     * Call this in child activity after setContentView
     */
    protected fun applySystemBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = insets.top,
                bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }
    }
    
    /**
     * Apply only top system bar padding (for activities with bottom nav)
     */
    protected fun applyTopSystemBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top)
            WindowInsetsCompat.CONSUMED
        }
    }
    
    // endregion
    
    // region State Collection

    /**
     * Collect a StateFlow in a lifecycle-aware manner
     */
    protected fun <T> collectState(flow: Flow<T>, collector: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flow.collect { collector(it) }
            }
        }
    }
    
    /**
     * Collect multiple flows in parallel
     */
    protected fun collectStates(vararg collectors: Pair<Flow<*>, suspend (Any?) -> Unit>) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectors.forEach { (flow, collector) ->
                    launch {
                        @Suppress("UNCHECKED_CAST")
                        (flow as Flow<Any?>).collect { collector(it) }
                    }
                }
            }
        }
    }
    
    // endregion
    
    // region Authentication

    protected fun createLoginIntent(redirect: String, matchId: String? = null): Intent {
        return Intent(this, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_REDIRECT, redirect)
            if (!matchId.isNullOrBlank()) {
                putExtra(LoginActivity.EXTRA_MATCH_ID, matchId)
            }
        }
    }

    protected suspend fun requireLoginOrRedirect(redirect: String, matchId: String? = null): Boolean {
        val tokenRepo = TokenRepository(application)
        val token = withContext(Dispatchers.IO) { tokenRepo.getToken() }
        
        if (token == null) {
            withContext(Dispatchers.Main) {
                showToast(getString(R.string.login_required))
                startActivity(createLoginIntent(redirect, matchId))
                finish()
            }
            return false
        }
        return true
    }
    
    /**
     * Check login status synchronously (use only in coroutine context)
     */
    protected suspend fun isLoggedIn(): Boolean {
        val tokenRepo = TokenRepository(application)
        return withContext(Dispatchers.IO) { tokenRepo.getToken() != null }
    }
    
    // endregion
    
    // region Navigation
    
    /**
     * Navigate to another activity with optional flags
     */
    protected inline fun <reified T> navigateTo(
        clearTask: Boolean = false,
        intentBuilder: Intent.() -> Unit = {}
    ) {
        val intent = Intent(this, T::class.java)
        if (clearTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        intent.intentBuilder()
        startActivity(intent)
    }
    
    /**
     * Navigate back with optional result
     */
    protected fun navigateBack(resultCode: Int = RESULT_CANCELED, data: Intent? = null) {
        setResult(resultCode, data)
        finish()
    }
    
    /**
     * Navigate to main activity clearing back stack
     */
    protected fun navigateToMain() {
        navigateTo<MainActivity>(clearTask = true)
        finish()
    }
    
    // endregion
    
    // region Toast & Dialog Helpers
    
    protected fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
    
    protected fun showSuccessDialog(
        title: String,
        message: String,
        onDismiss: () -> Unit = {}
    ) {
        ResultDialogHelper.showSuccess(this, title, message, onDismiss)
    }
    
    protected fun showErrorDialog(title: String, message: String) {
        ResultDialogHelper.showError(this, title, message)
    }
    
    // endregion
    
    // region Logging
    
    protected fun logDebug(message: String) {
        Log.d(getTag(), message)
    }
    
    protected fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(getTag(), message, throwable)
        } else {
            Log.e(getTag(), message)
        }
    }
    
    protected open fun getTag(): String = this::class.java.simpleName
    
    // endregion
    
    companion object {
        private const val CLICK_DEBOUNCE_MS = 500L
    }
}
