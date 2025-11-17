package com.mytheclipse.quizbattle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.data.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseActivity : AppCompatActivity() {

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
