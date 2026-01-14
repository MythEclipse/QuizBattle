package com.mytheclipse.quizbattle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mytheclipse.quizbattle.adapter.QuestionAdapter
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.databinding.ActivityQuestionManagementBinding
import com.mytheclipse.quizbattle.viewmodel.QuestionManagementState
import com.mytheclipse.quizbattle.viewmodel.QuestionManagementViewModel

/**
 * Activity for managing quiz questions (CRUD operations).
 * 
 * Provides functionality to:
 * - View all questions in a list
 * - Add new questions with category and difficulty
 * - Edit existing questions
 * - Delete questions with confirmation
 */
class QuestionManagementActivity : BaseActivity() {

    // region Properties
    
    private lateinit var binding: ActivityQuestionManagementBinding
    private val viewModel: QuestionManagementViewModel by viewModels()
    private lateinit var questionAdapter: QuestionAdapter
    
    // endregion

    // region Lifecycle
    
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
    
    // endregion

    // region Setup
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navigateBack()
        }
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(
            onEditClick = { question -> showEditDialog(question) },
            onDeleteClick = { question -> showDeleteConfirmation(question) }
        )
        binding.questionsRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@QuestionManagementActivity)
            adapter = questionAdapter
        }
    }

    private fun setupFab() {
        binding.addQuestionFab.setOnClickListener {
            withDebounce { showAddDialog() }
        }
    }
    
    // endregion

    // region State Observation
    
    private fun observeState() {
        collectState(viewModel.state) { state ->
            handleState(state)
        }
    }
    
    private fun handleState(state: QuestionManagementState) {
        updateLoadingState(state.isLoading)
        updateQuestionsList(state)
        handleMessages(state)
    }
    
    private fun updateLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    
    private fun updateQuestionsList(state: QuestionManagementState) {
        val isEmpty = state.questions.isEmpty() && !state.isLoading
        
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        questionAdapter.submitList(state.questions)
    }
    
    private fun handleMessages(state: QuestionManagementState) {
        state.error?.let { error ->
            showToast(error)
            viewModel.clearMessage()
        }
        
        state.successMessage?.let { message ->
            showToast(message)
            viewModel.clearMessage()
        }
    }
    
    // endregion

    // region Dialogs
    
    private fun showAddDialog() {
        val dialogView = inflateQuestionDialogView()
        setupDialogSpinners(dialogView)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_question)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                handleAddQuestion(dialogView)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditDialog(question: Question) {
        val dialogView = inflateQuestionDialogView()
        setupDialogSpinners(dialogView)
        prefillEditForm(dialogView, question)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_question)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { dialog, _ ->
                handleUpdateQuestion(dialogView, question)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeleteConfirmation(question: Question) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_question)
            .setMessage(getString(R.string.delete_question_confirm, question.questionText))
            .setPositiveButton(R.string.delete) { dialog, _ ->
                viewModel.deleteQuestion(question)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    // endregion

    // region Dialog Helpers
    
    private fun inflateQuestionDialogView(): View {
        return LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_question, null)
    }
    
    private fun setupDialogSpinners(dialogView: View) {
        val correctAnswerSpinner = dialogView.findViewById<Spinner>(R.id.correctAnswerSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)

        correctAnswerSpinner.adapter = createSpinnerAdapter(QuestionManagementViewModel.CORRECT_ANSWER_OPTIONS)
        categorySpinner.adapter = createSpinnerAdapter(QuestionManagementViewModel.CATEGORIES)
        difficultySpinner.adapter = createSpinnerAdapter(QuestionManagementViewModel.DIFFICULTIES)
    }
    
    private fun createSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
    }
    
    private fun prefillEditForm(dialogView: View, question: Question) {
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
    }
    
    // endregion

    // region Question Actions
    
    private fun handleAddQuestion(dialogView: View) {
        val formData = extractFormData(dialogView)
        
        if (validateFormData(formData)) {
            viewModel.addQuestion(
                questionText = formData.questionText,
                answer1 = formData.answer1,
                answer2 = formData.answer2,
                answer3 = formData.answer3,
                answer4 = formData.answer4,
                correctAnswerIndex = formData.correctAnswerIndex,
                category = formData.category,
                difficulty = formData.difficulty
            )
        }
    }
    
    private fun handleUpdateQuestion(dialogView: View, originalQuestion: Question) {
        val formData = extractFormData(dialogView)
        
        if (validateFormData(formData)) {
            val updatedQuestion = originalQuestion.copy(
                questionText = formData.questionText,
                answer1 = formData.answer1,
                answer2 = formData.answer2,
                answer3 = formData.answer3,
                answer4 = formData.answer4,
                correctAnswerIndex = formData.correctAnswerIndex,
                category = formData.category,
                difficulty = formData.difficulty
            )
            viewModel.updateQuestion(updatedQuestion)
        }
    }
    
    private fun extractFormData(dialogView: View): QuestionFormData {
        val correctAnswerSpinner = dialogView.findViewById<Spinner>(R.id.correctAnswerSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)
        
        return QuestionFormData(
            questionText = dialogView.findViewById<TextInputEditText>(R.id.questionEditText).text.toString(),
            answer1 = dialogView.findViewById<TextInputEditText>(R.id.answer1EditText).text.toString(),
            answer2 = dialogView.findViewById<TextInputEditText>(R.id.answer2EditText).text.toString(),
            answer3 = dialogView.findViewById<TextInputEditText>(R.id.answer3EditText).text.toString(),
            answer4 = dialogView.findViewById<TextInputEditText>(R.id.answer4EditText).text.toString(),
            correctAnswerIndex = correctAnswerSpinner.selectedItemPosition,
            category = categorySpinner.selectedItem.toString(),
            difficulty = difficultySpinner.selectedItem.toString()
        )
    }
    
    // endregion

    // region Validation
    
    private fun validateFormData(formData: QuestionFormData): Boolean {
        if (formData.questionText.isBlank()) {
            showToast(getString(R.string.error_question_empty))
            return false
        }
        
        if (formData.answer1.isBlank() || formData.answer2.isBlank() || 
            formData.answer3.isBlank() || formData.answer4.isBlank()) {
            showToast(getString(R.string.error_answers_required))
            return false
        }
        
        return true
    }
    
    // endregion

    // region Data Classes
    
    private data class QuestionFormData(
        val questionText: String,
        val answer1: String,
        val answer2: String,
        val answer3: String,
        val answer4: String,
        val correctAnswerIndex: Int,
        val category: String,
        val difficulty: String
    )
    
    // endregion
}
