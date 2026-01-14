package com.mytheclipse.quizbattle.core.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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
import androidx.viewbinding.ViewBinding
import com.mytheclipse.quizbattle.core.ui.UiState
import com.mytheclipse.quizbattle.utils.LocaleHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Base Activity with common functionality for all activities
 * Implements ViewBinding pattern and provides utility methods
 * 
 * @param VB ViewBinding type for this activity
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "Binding is only valid between onCreate and onDestroy"
        )

    /**
     * Provides the ViewBinding inflater for this activity
     */
    abstract val bindingInflater: (LayoutInflater) -> VB

    /**
     * Called after binding is inflated. Override to setup UI
     */
    abstract fun setupUI()

    /**
     * Called to observe ViewModel states. Override to collect flows
     */
    open fun observeState() {}

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflater(layoutInflater)
        setContentView(binding.root)
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setupUI()
        observeState()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // ===== Utility Methods =====

    /**
     * Apply system bar insets as padding to a view
     */
    protected fun applySystemBarPadding(view: View = binding.root) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = insets.top, bottom = insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Show a short toast message
     */
    protected fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Show a long toast message
     */
    protected fun showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Collect a Flow with lifecycle awareness
     * Only collects when activity is at least STARTED
     */
    protected fun <T> Flow<T>.collectWithLifecycle(
        state: Lifecycle.State = Lifecycle.State.STARTED,
        action: suspend (T) -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(state) {
                collectLatest(action)
            }
        }
    }

    /**
     * Handle common UiState for loading, error, and success
     */
    protected fun <T> handleUiState(
        state: UiState<T>,
        onLoading: () -> Unit = {},
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit = { showToast(it) }
    ) {
        when (state) {
            is UiState.Loading -> onLoading()
            is UiState.Success -> onSuccess(state.data)
            is UiState.Error -> onError(state.message)
            is UiState.Empty -> {}
        }
    }
}
