package com.mytheclipse.quizbattle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import com.mytheclipse.quizbattle.utils.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge for all activities
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

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

    protected fun createLoginIntent(redirect: String, matchId: String? = null): Intent {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(LoginActivity.EXTRA_REDIRECT, redirect)
        if (!matchId.isNullOrBlank()) intent.putExtra(LoginActivity.EXTRA_MATCH_ID, matchId)
        return intent
    }

    protected suspend fun requireLoginOrRedirect(redirect: String, matchId: String? = null): Boolean {
        val tokenRepo = TokenRepository(application)
        val token = withContext(Dispatchers.IO) { tokenRepo.getToken() }
        if (token == null) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(this@BaseActivity, "Silakan login terlebih dahulu", android.widget.Toast.LENGTH_SHORT).show()
                startActivity(createLoginIntent(redirect, matchId))
                finish()
            }
            return false
        }
        return true
    }
}
