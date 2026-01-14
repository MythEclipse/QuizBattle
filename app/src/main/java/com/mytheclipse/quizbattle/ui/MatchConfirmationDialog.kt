package com.mytheclipse.quizbattle.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import coil.load
import coil.transform.CircleCropTransformation
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.databinding.DialogMatchConfirmationBinding

/**
 * Dialog for confirming matchmaking
 * Shows opponent info and countdown timer for confirmation
 */
class MatchConfirmationDialog : DialogFragment() {
    
    private var _binding: DialogMatchConfirmationBinding? = null
    private val binding get() = _binding!!
    
    private var countDownTimer: CountDownTimer? = null
    private var onAccept: ((String) -> Unit)? = null
    private var onDecline: ((String) -> Unit)? = null
    
    private var matchId: String = ""
    private var opponentName: String = ""
    private var opponentLevel: Int = 0
    private var opponentPoints: Int = 0
    private var opponentAvatarUrl: String? = null
    private var difficulty: String = "medium"
    private var category: String = "General"
    private var totalQuestions: Int = 10
    private var timeoutSeconds: Long = 30
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_QuizBattle_Dialog)
        isCancelable = false
        
        arguments?.let { args ->
            matchId = args.getString(ARG_MATCH_ID, "")
            opponentName = args.getString(ARG_OPPONENT_NAME, "Opponent")
            opponentLevel = args.getInt(ARG_OPPONENT_LEVEL, 1)
            opponentPoints = args.getInt(ARG_OPPONENT_POINTS, 0)
            opponentAvatarUrl = args.getString(ARG_OPPONENT_AVATAR)
            difficulty = args.getString(ARG_DIFFICULTY, "medium")
            category = args.getString(ARG_CATEGORY, "General")
            totalQuestions = args.getInt(ARG_TOTAL_QUESTIONS, 10)
            timeoutSeconds = args.getLong(ARG_TIMEOUT_SECONDS, 30)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMatchConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupClickListeners()
        startCountdown()
    }
    
    private fun setupViews() {
        binding.apply {
            tvOpponentName.text = opponentName
            tvOpponentStats.text = "Level $opponentLevel • $opponentPoints pts"
            tvDifficulty.text = difficulty.replaceFirstChar { it.uppercase() }
            tvCategory.text = category
            tvQuestionCount.text = "$totalQuestions soal"
            
            progressCountdown.max = (timeoutSeconds * 1000).toInt()
            progressCountdown.progress = progressCountdown.max
            
            if (!opponentAvatarUrl.isNullOrEmpty()) {
                ivOpponentAvatar.load(opponentAvatarUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_profile)
                    error(R.drawable.ic_profile)
                    transformations(CircleCropTransformation())
                }
            } else {
                ivOpponentAvatar.setImageResource(R.drawable.ic_profile)
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            countDownTimer?.cancel()
            binding.btnAccept.isEnabled = false
            binding.btnDecline.isEnabled = false
            binding.tvWaitingStatus.visibility = View.VISIBLE
            binding.tvWaitingStatus.text = "Menunggu konfirmasi lawan..."
            onAccept?.invoke(matchId)
        }
        
        binding.btnDecline.setOnClickListener {
            countDownTimer?.cancel()
            onDecline?.invoke(matchId)
            dismiss()
        }
    }
    
    private fun startCountdown() {
        val totalMillis = timeoutSeconds * 1000
        
        countDownTimer = object : CountDownTimer(totalMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                binding.tvCountdown.text = "Waktu tersisa: $secondsRemaining detik"
                binding.progressCountdown.progress = millisUntilFinished.toInt()
            }
            
            override fun onFinish() {
                binding.tvCountdown.text = "Waktu habis!"
                onDecline?.invoke(matchId)
                dismiss()
            }
        }.start()
    }
    
    fun setOnAcceptListener(listener: (String) -> Unit) {
        onAccept = listener
    }
    
    fun setOnDeclineListener(listener: (String) -> Unit) {
        onDecline = listener
    }
    
    fun updateWaitingStatus(status: String, confirmedCount: Int, totalPlayers: Int) {
        if (_binding == null) return
        
        binding.tvWaitingStatus.visibility = View.VISIBLE
        binding.tvWaitingStatus.text = when (status) {
            "waiting" -> "Menunggu konfirmasi lawan... ($confirmedCount/$totalPlayers)"
            "both_confirmed" -> "Kedua pemain siap! Memulai pertandingan..."
            "rejected" -> "Lawan menolak pertandingan"
            else -> status
        }
        
        if (status == "both_confirmed" || status == "rejected") {
            countDownTimer?.cancel()
        }
    }
    
    fun onMatchStarting() {
        countDownTimer?.cancel()
        binding.tvWaitingStatus.visibility = View.VISIBLE
        binding.tvWaitingStatus.text = "Pertandingan dimulai..."
        binding.layoutButtons.visibility = View.GONE
    }
    
    override fun onDestroyView() {
        countDownTimer?.cancel()
        _binding = null
        super.onDestroyView()
    }
    
    companion object {
        const val TAG = "MatchConfirmationDialog"
        
        private const val ARG_MATCH_ID = "match_id"
        private const val ARG_OPPONENT_NAME = "opponent_name"
        private const val ARG_OPPONENT_LEVEL = "opponent_level"
        private const val ARG_OPPONENT_POINTS = "opponent_points"
        private const val ARG_OPPONENT_AVATAR = "opponent_avatar"
        private const val ARG_DIFFICULTY = "difficulty"
        private const val ARG_CATEGORY = "category"
        private const val ARG_TOTAL_QUESTIONS = "total_questions"
        private const val ARG_TIMEOUT_SECONDS = "timeout_seconds"
        
        fun newInstance(
            matchId: String,
            opponentName: String,
            opponentLevel: Int,
            opponentPoints: Int,
            opponentAvatarUrl: String?,
            difficulty: String,
            category: String,
            totalQuestions: Int,
            timeoutSeconds: Long = 30
        ): MatchConfirmationDialog {
            return MatchConfirmationDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_MATCH_ID, matchId)
                    putString(ARG_OPPONENT_NAME, opponentName)
                    putInt(ARG_OPPONENT_LEVEL, opponentLevel)
                    putInt(ARG_OPPONENT_POINTS, opponentPoints)
                    putString(ARG_OPPONENT_AVATAR, opponentAvatarUrl)
                    putString(ARG_DIFFICULTY, difficulty)
                    putString(ARG_CATEGORY, category)
                    putInt(ARG_TOTAL_QUESTIONS, totalQuestions)
                    putLong(ARG_TIMEOUT_SECONDS, timeoutSeconds)
                }
            }
        }
    }
}
