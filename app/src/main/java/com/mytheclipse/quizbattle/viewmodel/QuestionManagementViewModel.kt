package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.data.repository.QuestionRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for Question Management screen
 */
data class QuestionManagementState(
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
) {
    /** Check if there are any questions */
    val hasQuestions: Boolean get() = questions.isNotEmpty()
    
    /** Get total question count */
    val totalQuestions: Int get() = questions.size
    
    /** Get questions by difficulty */
    fun getQuestionsByDifficulty(difficulty: String): List<Question> =
        questions.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    
    /** Get questions by category */
    fun getQuestionsByCategory(category: String): List<Question> =
        questions.filter { it.category.equals(category, ignoreCase = true) }
}

/**
 * One-time events for Question Management screen
 */
sealed class QuestionManagementEvent {
    data class QuestionAdded(val questionId: Long) : QuestionManagementEvent()
    data class QuestionUpdated(val questionId: Long) : QuestionManagementEvent()
    data class QuestionDeleted(val questionId: Long) : QuestionManagementEvent()
    data class ShowSuccess(val message: String) : QuestionManagementEvent()
    data class ShowError(val message: String) : QuestionManagementEvent()
}

/**
 * ViewModel for Question Management functionality
 */
class QuestionManagementViewModel(application: Application) : AndroidViewModel(application) {

    // region Dependencies
    private val database = QuizBattleDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())
    // endregion

    // region State
    private val _state = MutableStateFlow(QuestionManagementState())
    val state: StateFlow<QuestionManagementState> = _state.asStateFlow()
    
    private val _events = MutableSharedFlow<QuestionManagementEvent>()
    val events: SharedFlow<QuestionManagementEvent> = _events.asSharedFlow()
    // endregion

    // region Exception Handler
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logError("Coroutine error", throwable)
        updateState { copy(isLoading = false, error = throwable.message) }
    }
    // endregion

    init {
        loadQuestions()
    }

    // region Public Actions

    fun addQuestion(
        questionText: String,
        answer1: String,
        answer2: String,
        answer3: String,
        answer4: String,
        correctAnswerIndex: Int,
        category: String,
        difficulty: String
    ) {
        launchSafely {
            val question = createQuestion(
                questionText, answer1, answer2, answer3, answer4,
                correctAnswerIndex, category, difficulty
            )
            questionRepository.insertQuestion(question)
            
            showSuccess(MSG_QUESTION_ADDED)
            emitEvent(QuestionManagementEvent.QuestionAdded(question.id))
        }
    }

    fun updateQuestion(question: Question) {
        launchSafely {
            questionRepository.updateQuestion(question)
            showSuccess(MSG_QUESTION_UPDATED)
            emitEvent(QuestionManagementEvent.QuestionUpdated(question.id))
        }
    }

    fun deleteQuestion(question: Question) {
        launchSafely {
            questionRepository.deleteQuestion(question)
            showSuccess(MSG_QUESTION_DELETED)
            emitEvent(QuestionManagementEvent.QuestionDeleted(question.id))
        }
    }

    fun clearMessage() {
        updateState { copy(successMessage = null, error = null) }
    }
    
    fun refresh() {
        loadQuestions()
    }

    // endregion

    // region Private Methods

    private fun loadQuestions() {
        launchSafely {
            setLoading(true)
            questionRepository.getAllActiveQuestions().collect { questions ->
                updateState {
                    copy(questions = questions, isLoading = false, error = null)
                }
            }
        }
    }

    private fun createQuestion(
        questionText: String,
        answer1: String,
        answer2: String,
        answer3: String,
        answer4: String,
        correctAnswerIndex: Int,
        category: String,
        difficulty: String
    ) = Question(
        questionText = questionText,
        answer1 = answer1,
        answer2 = answer2,
        answer3 = answer3,
        answer4 = answer4,
        correctAnswerIndex = correctAnswerIndex,
        category = category,
        difficulty = difficulty
    )

    private fun showSuccess(message: String) {
        updateState { copy(successMessage = message) }
        emitEvent(QuestionManagementEvent.ShowSuccess(message))
    }

    // endregion

    // region Utility Methods

    private inline fun updateState(update: QuestionManagementState.() -> QuestionManagementState) {
        _state.update { it.update() }
    }

    private fun setLoading(isLoading: Boolean) {
        updateState { copy(isLoading = isLoading, error = null) }
    }

    private fun launchSafely(block: suspend () -> Unit) {
        viewModelScope.launch(exceptionHandler) { block() }
    }

    private fun emitEvent(event: QuestionManagementEvent) {
        viewModelScope.launch { _events.emit(event) }
    }

    private fun logError(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }

    // endregion

    companion object {
        private const val TAG = "QuestionManagementVM"
        
        // Success messages
        private const val MSG_QUESTION_ADDED = "Soal berhasil ditambahkan"
        private const val MSG_QUESTION_UPDATED = "Soal berhasil diperbarui"
        private const val MSG_QUESTION_DELETED = "Soal berhasil dihapus"
        
        // Category options
        val CATEGORIES = listOf(
            "General",
            "Science",
            "History",
            "Geography",
            "Technology",
            "Art",
            "Sports",
            "Literature",
            "Mathematics",
            "Culture",
            "General Knowledge"
        )

        // Difficulty options
        val DIFFICULTIES = listOf("Easy", "Medium", "Hard")

        // Answer options
        val CORRECT_ANSWER_OPTIONS = listOf("A", "B", "C", "D")
    }
}
