package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.databinding.ItemQuestionBinding

/**
 * RecyclerView adapter for displaying and managing quiz questions.
 * 
 * Provides edit and delete functionality for each question item,
 * with color-coded difficulty badges.
 *
 * @param onEditClick Callback invoked when edit button is clicked
 * @param onDeleteClick Callback invoked when delete button is clicked
 */
class QuestionAdapter(
    private val onEditClick: (Question) -> Unit,
    private val onDeleteClick: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

    // region Adapter Implementation
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    // endregion

    // region ViewHolder
    
    inner class QuestionViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {
            bindQuestionInfo(question)
            bindDifficultyBadge(question.difficulty)
            setupClickListeners(question)
        }
        
        private fun bindQuestionInfo(question: Question) {
            binding.questionTextView.text = question.questionText
            binding.categoryBadge.text = question.category
            binding.difficultyBadge.text = question.difficulty
        }
        
        private fun bindDifficultyBadge(difficulty: String) {
            val colorRes = getDifficultyColor(difficulty)
            binding.difficultyBadge.setTextColor(
                binding.root.context.getColor(colorRes)
            )
        }
        
        @ColorRes
        private fun getDifficultyColor(difficulty: String): Int {
            return when (difficulty.lowercase()) {
                DIFFICULTY_EASY -> android.R.color.holo_green_dark
                DIFFICULTY_MEDIUM -> android.R.color.holo_orange_dark
                DIFFICULTY_HARD -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
        }
        
        private fun setupClickListeners(question: Question) {
            binding.editButton.setOnClickListener { onEditClick(question) }
            binding.deleteButton.setOnClickListener { onDeleteClick(question) }
        }
    }
    
    // endregion

    // region DiffCallback
    
    private class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }
    
    // endregion

    companion object {
        private const val DIFFICULTY_EASY = "easy"
        private const val DIFFICULTY_MEDIUM = "medium"
        private const val DIFFICULTY_HARD = "hard"
    }
}
