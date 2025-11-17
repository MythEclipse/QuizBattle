package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityLobbyListBinding

class LobbyListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLobbyListBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyListBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
