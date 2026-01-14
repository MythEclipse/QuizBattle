package com.mytheclipse.quizbattle.core.ui

/**
 * Represents one-time UI events that should not be re-delivered on configuration change
 * Use for navigation, snackbars, toasts, etc.
 */
sealed class UiEvent {
    
    /**
     * Navigate to a destination
     */
    data class Navigate(
        val route: String,
        val args: Map<String, Any> = emptyMap()
    ) : UiEvent()
    
    /**
     * Navigate back
     */
    data object NavigateBack : UiEvent()
    
    /**
     * Show a snackbar message
     */
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val action: (() -> Unit)? = null
    ) : UiEvent()
    
    /**
     * Show a toast message
     */
    data class ShowToast(val message: String) : UiEvent()
    
    /**
     * Show an error dialog
     */
    data class ShowError(
        val title: String,
        val message: String
    ) : UiEvent()
    
    /**
     * Show a success dialog
     */
    data class ShowSuccess(
        val title: String,
        val message: String
    ) : UiEvent()
    
    /**
     * Show a loading dialog
     */
    data class ShowLoading(val message: String = "Loading...") : UiEvent()
    
    /**
     * Hide loading dialog
     */
    data object HideLoading : UiEvent()
}
