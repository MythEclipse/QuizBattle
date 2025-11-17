package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityMatchmakingBinding

class MatchmakingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchmakingBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchmakingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
