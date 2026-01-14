package com.mytheclipse.quizbattle.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.core.ui.UiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality
 * Provides error handling, loading states, and event emission
 */
abstract class BaseViewModel : ViewModel() {

    // ===== Loading State =====
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ===== Error Events =====
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // ===== Success Events =====
    private val _successEvent = MutableSharedFlow<String>()
    val successEvent: SharedFlow<String> = _successEvent.asSharedFlow()

    /**
     * Coroutine exception handler for safe error handling
     */
    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch {
            _isLoading.value = false
            _errorEvent.emit(throwable.message ?: "An unknown error occurred")
        }
    }

    /**
     * Execute a suspending block with loading state management
     */
    protected fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            try {
                block()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Execute a suspending block and wrap result in UiState
     */
    protected suspend fun <T> executeWithState(
        stateFlow: MutableStateFlow<UiState<T>>,
        block: suspend () -> T
    ) {
        stateFlow.value = UiState.Loading
        try {
            val result = block()
            stateFlow.value = UiState.Success(result)
        } catch (e: Exception) {
            stateFlow.value = UiState.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Emit an error message to observers
     */
    protected fun emitError(message: String) {
        viewModelScope.launch {
            _errorEvent.emit(message)
        }
    }

    /**
     * Emit a success message to observers
     */
    protected fun emitSuccess(message: String) {
        viewModelScope.launch {
            _successEvent.emit(message)
        }
    }

    /**
     * Set loading state
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}
