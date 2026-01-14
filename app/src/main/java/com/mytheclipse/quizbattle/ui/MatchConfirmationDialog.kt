package com.mytheclipse.quizbattle.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import coil.load
import coil.transform.CircleCropTransformation
import com.mytheclipse.quizbattle.R
import com.mytheclipse.quizbattle.databinding.DialogMatchConfirmationBinding

/**
 * Dialog for confirming matchmaking
 * Shows opponent info with animated entrance and countdown timer for confirmation
 * 
 * Features:
 * - Animated avatar and content entrance
 * - Countdown progress with visual feedback
 * - Accept/Decline actions with loading states
 * - Status updates for match confirmation flow
 */
class MatchConfirmationDialog : DialogFragment() {

    private var _binding: DialogMatchConfirmationBinding? = null
    private val binding get() = _binding!!

    private var countDownTimer: CountDownTimer? = null
    private var onAccept: ((String) -> Unit)? = null
    private var onDecline: ((String) -> Unit)? = null

    // Match data
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
        parseArguments()
    }

    private fun parseArguments() {
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
        playEntranceAnimation()
        startCountdown()
    }

    private fun setupViews() = binding.apply {
        // Opponent info
        tvOpponentName.text = opponentName
        tvOpponentStats.text = getString(R.string.opponent_stats_format, opponentLevel, opponentPoints)

        // Match settings
        tvDifficulty.text = difficulty.replaceFirstChar { it.uppercase() }
        tvCategory.text = category
        tvQuestionCount.text = getString(R.string.question_count_format, totalQuestions)

        // Progress bar setup
        progressCountdown.max = (timeoutSeconds * 1000).toInt()
        progressCountdown.progress = progressCountdown.max

        // Load opponent avatar
        loadOpponentAvatar()
    }

    private fun loadOpponentAvatar() {
        val avatarUrl = opponentAvatarUrl
        if (!avatarUrl.isNullOrEmpty()) {
            binding.ivOpponentAvatar.load(avatarUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                transformations(CircleCropTransformation())
            }
        } else {
            binding.ivOpponentAvatar.setImageResource(R.drawable.ic_profile)
        }
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener { handleAccept() }
        binding.btnDecline.setOnClickListener { handleDecline() }
    }

    private fun handleAccept() {
        countDownTimer?.cancel()
        setButtonsEnabled(false)
        showWaitingStatus(getString(R.string.waiting_opponent_confirmation))
        onAccept?.invoke(matchId)
    }

    private fun handleDecline() {
        countDownTimer?.cancel()
        onDecline?.invoke(matchId)
        dismiss()
    }

    private fun setButtonsEnabled(enabled: Boolean) = binding.apply {
        btnAccept.isEnabled = enabled
        btnDecline.isEnabled = enabled
    }

    private fun showWaitingStatus(message: String) = binding.apply {
        tvWaitingStatus.isVisible = true
        tvWaitingStatus.text = message
    }

    private fun playEntranceAnimation() {
        val views = listOf(
            binding.tvTitle,
            binding.ivOpponentAvatar,
            binding.tvVsLabel,
            binding.tvOpponentName,
            binding.tvOpponentStats,
            binding.cardGameSettings,
            binding.layoutButtons
        )

        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f

            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 80).toLong())
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }

        // Special bounce animation for avatar
        binding.ivOpponentAvatar.postDelayed({
            playAvatarPulseAnimation()
        }, 500)
    }

    private fun playAvatarPulseAnimation() {
        val scaleX = ObjectAnimator.ofFloat(binding.ivOpponentAvatar, View.SCALE_X, 1f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivOpponentAvatar, View.SCALE_Y, 1f, 1.1f, 1f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun startCountdown() {
        val totalMillis = timeoutSeconds * 1000

        countDownTimer = object : CountDownTimer(totalMillis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                if (_binding == null) return

                val secondsRemaining = (millisUntilFinished / 1000).toInt()
                binding.tvCountdown.text = getString(R.string.time_remaining_format, secondsRemaining)
                binding.progressCountdown.progress = millisUntilFinished.toInt()

                // Warning color when low time
                if (secondsRemaining <= 10) {
                    binding.tvCountdown.setTextColor(
                        requireContext().getColor(R.color.error)
                    )
                }
            }

            override fun onFinish() {
                if (_binding == null) return
                binding.tvCountdown.text = getString(R.string.time_expired)
                onDecline?.invoke(matchId)
                dismiss()
            }
        }.start()
    }

    // ===== Public API =====

    fun setOnAcceptListener(listener: (String) -> Unit) {
        onAccept = listener
    }

    fun setOnDeclineListener(listener: (String) -> Unit) {
        onDecline = listener
    }

    fun updateWaitingStatus(status: String, confirmedCount: Int, totalPlayers: Int) {
        if (_binding == null) return

        val statusMessage = when (status) {
            STATUS_WAITING -> getString(R.string.waiting_confirmation_count, confirmedCount, totalPlayers)
            STATUS_BOTH_CONFIRMED -> getString(R.string.both_players_ready)
            STATUS_REJECTED -> getString(R.string.opponent_rejected)
            else -> status
        }

        showWaitingStatus(statusMessage)

        if (status == STATUS_BOTH_CONFIRMED || status == STATUS_REJECTED) {
            countDownTimer?.cancel()
        }
    }

    fun onMatchStarting() {
        countDownTimer?.cancel()
        binding.apply {
            tvWaitingStatus.isVisible = true
            tvWaitingStatus.text = getString(R.string.match_starting)
            layoutButtons.isVisible = false
        }
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        countDownTimer = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "MatchConfirmationDialog"

        // Status constants
        private const val STATUS_WAITING = "waiting"
        private const val STATUS_BOTH_CONFIRMED = "both_confirmed"
        private const val STATUS_REJECTED = "rejected"

        // Argument keys
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
