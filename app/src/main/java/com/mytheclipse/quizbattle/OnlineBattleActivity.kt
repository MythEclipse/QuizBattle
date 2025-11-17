package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityOnlineBattleBinding

class OnlineBattleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnlineBattleBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
