package com.mytheclipse.quizbattle.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mytheclipse.quizbattle.data.local.QuizBattleDatabase
import com.mytheclipse.quizbattle.data.local.entity.Question
import com.mytheclipse.quizbattle.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionManagementState(
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class QuestionManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuizBattleDatabase.getDatabase(application)
    private val questionRepository = QuestionRepository(database.questionDao())

    private val _state = MutableStateFlow(QuestionManagementState())
    val state: StateFlow<QuestionManagementState> = _state.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                questionRepository.getAllActiveQuestions().collect { questions ->
                    _state.value = _state.value.copy(
                        questions = questions,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat soal"
                )
            }
        }
    }

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
        viewModelScope.launch {
            try {
                val question = Question(
                    questionText = questionText,
                    answer1 = answer1,
                    answer2 = answer2,
                    answer3 = answer3,
                    answer4 = answer4,
                    correctAnswerIndex = correctAnswerIndex,
                    category = category,
                    difficulty = difficulty
                )
                questionRepository.insertQuestion(question)
                _state.value = _state.value.copy(successMessage = "Soal berhasil ditambahkan")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Gagal menambahkan soal")
            }
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            try {
                questionRepository.updateQuestion(question)
                _state.value = _state.value.copy(successMessage = "Soal berhasil diperbarui")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Gagal memperbarui soal")
            }
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            try {
                questionRepository.deleteQuestion(question)
                _state.value = _state.value.copy(successMessage = "Soal berhasil dihapus")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Gagal menghapus soal")
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(successMessage = null, error = null)
    }

    companion object {
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

        val DIFFICULTIES = listOf("Easy", "Medium", "Hard")

        val CORRECT_ANSWER_OPTIONS = listOf("A", "B", "C", "D")
    }
}
