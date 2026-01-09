package com.mytheclipse.quizbattle.data.remote.api

import com.mytheclipse.quizbattle.data.remote.model.GameHistoryResponse
import retrofit2.http.GET

interface GameHistoryApiService {
    @GET("api/history")
    suspend fun getGameHistory(): GameHistoryResponse
}
