package com.mytheclipse.quizbattle.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.databinding.ItemQuestionBinding

class QuestionAdapter(
    private val onEditClick: (Question) -> Unit,
    private val onDeleteClick: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.QuestionViewHolder>(QuestionDiffCallback()) {

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

    inner class QuestionViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {
            binding.questionTextView.text = question.questionText
            binding.categoryBadge.text = question.category
            binding.difficultyBadge.text = question.difficulty

            // Set difficulty badge color based on difficulty level
            val difficultyColorRes = when (question.difficulty.lowercase()) {
                "easy" -> android.R.color.holo_green_dark
                "medium" -> android.R.color.holo_orange_dark
                "hard" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            binding.difficultyBadge.setTextColor(
                binding.root.context.getColor(difficultyColorRes)
            )

            binding.editButton.setOnClickListener { onEditClick(question) }
            binding.deleteButton.setOnClickListener { onDeleteClick(question) }
        }
    }

    private class QuestionDiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }
}
