package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mytheclipse.quizbattle.adapter.QuestionAdapter
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.databinding.ActivityQuestionManagementBinding
import com.mytheclipse.quizbattle.viewmodel.QuestionManagementViewModel
import kotlinx.coroutines.launch

class QuestionManagementActivity : BaseActivity() {

    private lateinit var binding: ActivityQuestionManagementBinding
    private val viewModel: QuestionManagementViewModel by viewModels()
    private lateinit var questionAdapter: QuestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applySystemBarPadding(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(
            onEditClick = { question -> showEditDialog(question) },
            onDeleteClick = { question -> showDeleteConfirmation(question) }
        )
        binding.questionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@QuestionManagementActivity)
            adapter = questionAdapter
        }
    }

    private fun setupFab() {
        binding.addQuestionFab.setOnClickListener {
            showAddDialog()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // Show loading
                binding.progressBar.visibility = if (state.isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Update list
                questionAdapter.submitList(state.questions)

                // Show empty state
                binding.emptyStateLayout.visibility = if (state.questions.isEmpty() && !state.isLoading) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Show error
                state.error?.let { error ->
                    Toast.makeText(this@QuestionManagementActivity, error, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }

                // Show success
                state.successMessage?.let { message ->
                    Toast.makeText(this@QuestionManagementActivity, message, Toast.LENGTH_SHORT).show()
                    viewModel.clearMessage()
                }
            }
        }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_edit_question, null)

        setupDialogSpinners(dialogView)

        MaterialAlertDialogBuilder(this)
            .setTitle("Tambah Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val questionText = dialogView.findViewById<TextInputEditText>(R.id.questionEditText).text.toString()
                val answer1 = dialogView.findViewById<TextInputEditText>(R.id.answer1EditText).text.toString()
                val answer2 = dialogView.findViewById<TextInputEditText>(R.id.answer2EditText).text.toString()
                val answer3 = dialogView.findViewById<TextInputEditText>(R.id.answer3EditText).text.toString()
                val answer4 = dialogView.findViewById<TextInputEditText>(R.id.answer4EditText).text.toString()
                val correctAnswerSpinner = dialogView.findViewById<Spinner>(R.id.correctAnswerSpinner)
                val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
                val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)

                if (validateInputs(questionText, answer1, answer2, answer3, answer4)) {
                    viewModel.addQuestion(
                        questionText = questionText,
                        answer1 = answer1,
                        answer2 = answer2,
                        answer3 = answer3,
                        answer4 = answer4,
                        correctAnswerIndex = correctAnswerSpinner.selectedItemPosition,
                        category = categorySpinner.selectedItem.toString(),
                        difficulty = difficultySpinner.selectedItem.toString()
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditDialog(question: Question) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_edit_question, null)

        // Setup spinners
        setupDialogSpinners(dialogView)

        // Pre-fill form
        dialogView.findViewById<TextInputEditText>(R.id.questionEditText).setText(question.questionText)
        dialogView.findViewById<TextInputEditText>(R.id.answer1EditText).setText(question.answer1)
        dialogView.findViewById<TextInputEditText>(R.id.answer2EditText).setText(question.answer2)
        dialogView.findViewById<TextInputEditText>(R.id.answer3EditText).setText(question.answer3)
        dialogView.findViewById<TextInputEditText>(R.id.answer4EditText).setText(question.answer4)

        val correctAnswerSpinner = dialogView.findViewById<Spinner>(R.id.correctAnswerSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)

        correctAnswerSpinner.setSelection(question.correctAnswerIndex)
        
        val categoryIndex = QuestionManagementViewModel.CATEGORIES.indexOf(question.category)
        if (categoryIndex >= 0) categorySpinner.setSelection(categoryIndex)

        val difficultyIndex = QuestionManagementViewModel.DIFFICULTIES.indexOf(question.difficulty)
        if (difficultyIndex >= 0) difficultySpinner.setSelection(difficultyIndex)

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { dialog, _ ->
                val questionText = dialogView.findViewById<TextInputEditText>(R.id.questionEditText).text.toString()
                val answer1 = dialogView.findViewById<TextInputEditText>(R.id.answer1EditText).text.toString()
                val answer2 = dialogView.findViewById<TextInputEditText>(R.id.answer2EditText).text.toString()
                val answer3 = dialogView.findViewById<TextInputEditText>(R.id.answer3EditText).text.toString()
                val answer4 = dialogView.findViewById<TextInputEditText>(R.id.answer4EditText).text.toString()

                if (validateInputs(questionText, answer1, answer2, answer3, answer4)) {
                    val updatedQuestion = question.copy(
                        questionText = questionText,
                        answer1 = answer1,
                        answer2 = answer2,
                        answer3 = answer3,
                        answer4 = answer4,
                        correctAnswerIndex = correctAnswerSpinner.selectedItemPosition,
                        category = categorySpinner.selectedItem.toString(),
                        difficulty = difficultySpinner.selectedItem.toString()
                    )
                    viewModel.updateQuestion(updatedQuestion)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmation(question: Question) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Hapus Soal")
            .setMessage("Apakah Anda yakin ingin menghapus soal ini?\n\n\"${question.questionText}\"")
            .setPositiveButton("Hapus") { dialog, _ ->
                viewModel.deleteQuestion(question)
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupDialogSpinners(dialogView: android.view.View) {
        val correctAnswerSpinner = dialogView.findViewById<Spinner>(R.id.correctAnswerSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)

        correctAnswerSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            QuestionManagementViewModel.CORRECT_ANSWER_OPTIONS
        )

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            QuestionManagementViewModel.CATEGORIES
        )

        difficultySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            QuestionManagementViewModel.DIFFICULTIES
        )
    }

    private fun validateInputs(
        questionText: String,
        answer1: String,
        answer2: String,
        answer3: String,
        answer4: String
    ): Boolean {
        if (questionText.isBlank()) {
            Toast.makeText(this, "Pertanyaan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return false
        }
        if (answer1.isBlank() || answer2.isBlank() || answer3.isBlank() || answer4.isBlank()) {
            Toast.makeText(this, "Semua jawaban harus diisi", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
