package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityRankedBinding

class RankedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRankedBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankedBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
