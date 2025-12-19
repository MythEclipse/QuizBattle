package com.mytheclipse.quizbattle.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.mytheclipse.quizbattle.R

/**
 * Helper class for showing "Match Found" dialog similar to Mobile Legends.
 * Shows player vs opponent info with countdown before starting the game.
 */
object MatchFoundDialogHelper {

    data class PlayerInfo(
        val name: String,
        val level: Int = 1,
        val avatarRes: Int? = null
    )

    data class MatchInfo(
        val matchId: String,
        val category: String = "General",
        val difficulty: String = "Normal"
    )

    /**
     * Shows a "Match Found" dialog with countdown animation.
     * 
     * @param context Context to show dialog in
     * @param playerInfo Player's display info
     * @param opponentInfo Opponent's display info
     * @param matchInfo Match category and difficulty
     * @param countdownSeconds Seconds to count down before game starts (default: 5)
     * @param onCountdownComplete Callback when countdown completes
     */
    fun showMatchFoundDialog(
        context: Context,
        playerInfo: PlayerInfo,
        opponentInfo: PlayerInfo,
        matchInfo: MatchInfo,
        countdownSeconds: Int = 5,
        onCountdownComplete: () -> Unit
    ): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        
        val view = LayoutInflater.from(context).inflate(R.layout.overlay_match_found, null)
        dialog.setContentView(view)
        
        // Make dialog fullscreen with dark overlay
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.3f)
        }
        
        // Set player info
        view.findViewById<TextView>(R.id.playerName).text = playerInfo.name
        view.findViewById<TextView>(R.id.playerLevel).text = "Level ${playerInfo.level}"
        playerInfo.avatarRes?.let { res ->
            view.findViewById<ImageView>(R.id.playerAvatar).setImageResource(res)
        }
        
        // Set opponent info
        view.findViewById<TextView>(R.id.opponentName).text = opponentInfo.name
        view.findViewById<TextView>(R.id.opponentLevel).text = "Level ${opponentInfo.level}"
        opponentInfo.avatarRes?.let { res ->
            view.findViewById<ImageView>(R.id.opponentAvatar).setImageResource(res)
        }
        
        // Set match info
        view.findViewById<TextView>(R.id.categoryText).text = "📚 ${matchInfo.category}"
        view.findViewById<TextView>(R.id.difficultyText).text = "⚡ ${matchInfo.difficulty}"
        
        // Setup countdown
        val countdownText = view.findViewById<TextView>(R.id.countdownText)
        val progressBar = view.findViewById<ProgressBar>(R.id.loadingBar)
        progressBar.max = countdownSeconds * 1000
        progressBar.progress = countdownSeconds * 1000
        
        // Animate countdown
        val timer = object : CountDownTimer((countdownSeconds * 1000).toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt() + 1
                countdownText.text = "Game starting in $secondsLeft..."
                progressBar.progress = millisUntilFinished.toInt()
            }
            
            override fun onFinish() {
                countdownText.text = "GO! 🎮"
                progressBar.progress = 0
                
                // Short delay after "GO!" then dismiss
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    dialog.dismiss()
                    onCountdownComplete()
                }, 500)
            }
        }
        
        dialog.setOnShowListener {
            timer.start()
        }
        
        dialog.setOnDismissListener {
            timer.cancel()
        }
        
        dialog.show()
        return dialog
    }
}
