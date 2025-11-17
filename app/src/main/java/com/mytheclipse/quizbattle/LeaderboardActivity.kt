package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityLeaderboardBinding

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
