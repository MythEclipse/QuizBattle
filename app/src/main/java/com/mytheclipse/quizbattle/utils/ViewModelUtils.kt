package com.mytheclipse.quizbattle.utils

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Helper function to create AndroidViewModel instances in Compose
 */
@Composable
inline fun <reified T : ViewModel> androidViewModel(): T {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    return viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return modelClass.getConstructor(Application::class.java)
                    .newInstance(application) as T
            }
        }
    )
}

