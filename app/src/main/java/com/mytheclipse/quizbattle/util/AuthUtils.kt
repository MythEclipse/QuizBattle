package com.mytheclipse.quizbattle.util

import android.content.Context
import android.content.Intent
import com.mytheclipse.quizbattle.LoginActivity
import com.mytheclipse.quizbattle.data.repository.TokenRepository

object AuthUtils {
    suspend fun isLoggedIn(context: Context): Boolean {
        val tokenRepository = TokenRepository(context)
        return tokenRepository.getToken() != null
    }

    fun createLoginIntent(context: Context, redirect: String, matchId: String? = null): Intent {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtra(LoginActivity.EXTRA_REDIRECT, redirect)
        if (!matchId.isNullOrBlank()) intent.putExtra(LoginActivity.EXTRA_MATCH_ID, matchId)
        return intent
    }
}
