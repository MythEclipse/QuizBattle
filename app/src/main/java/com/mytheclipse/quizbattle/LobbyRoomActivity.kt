package com.mytheclipse.quizbattle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityLobbyRoomBinding

class LobbyRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLobbyRoomBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLobbyRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
