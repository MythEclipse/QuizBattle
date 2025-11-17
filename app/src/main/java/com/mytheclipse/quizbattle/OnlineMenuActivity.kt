package com.mytheclipse.quizbattle

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mytheclipse.quizbattle.databinding.ActivityOnlineMenuBinding

class OnlineMenuActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnlineMenuBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnlineMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.backButton?.setOnClickListener {
            finish()
        }
        
        binding.quickMatchButton?.setOnClickListener {
            startQuickMatch()
        }
        
        binding.createRoomButton?.setOnClickListener {
            showCreateRoomDialog()
        }
        
        binding.joinRoomButton?.setOnClickListener {
            showJoinRoomDialog()
        }
    }
    
    private fun startQuickMatch() {
        // Generate random match ID for demo
        val matchId = "QM${System.currentTimeMillis()}"
        
        Toast.makeText(this, "Searching for opponent...", Toast.LENGTH_SHORT).show()
        
        // Simulate matchmaking delay
        binding.quickMatchButton?.postDelayed({
            val intent = Intent(this, OnlineBattleActivity::class.java)
            intent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
            startActivity(intent)
        }, 1500)
    }
    
    private fun showCreateRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Room")
        builder.setMessage("Room ID: ROOM${System.currentTimeMillis()}")
        builder.setPositiveButton("Create") { _, _ ->
            val matchId = "ROOM${System.currentTimeMillis()}"
            Toast.makeText(this, "Room created! Share ID: $matchId", Toast.LENGTH_LONG).show()
            
            val intent = Intent(this, OnlineBattleActivity::class.java)
            intent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, matchId)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
    
    private fun showJoinRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Join Room")
        
        val input = android.widget.EditText(this)
        input.hint = "Enter Room ID"
        builder.setView(input)
        
        builder.setPositiveButton("Join") { _, _ ->
            val roomId = input.text.toString().trim()
            if (roomId.isNotEmpty()) {
                val intent = Intent(this, OnlineBattleActivity::class.java)
                intent.putExtra(OnlineBattleActivity.EXTRA_MATCH_ID, roomId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please enter a room ID", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
