package com.mytheclipse.quizbattle.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mytheclipse.quizbattle.ui.components.QuizAnswerButton
import com.mytheclipse.quizbattle.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Question(
    val text: String,
    val answers: List<String>,
    val correctAnswerIndex: Int
)

@Composable
fun BattleScreen(
    onNavigateToResult: (Boolean) -> Unit // true for victory, false for defeat
) {
    val coroutineScope = rememberCoroutineScope()
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswerIndex by remember { mutableIntStateOf(-1) }
    var isAnswered by remember { mutableStateOf(false) }
    var playerScore by remember { mutableIntStateOf(0) }
    var opponentScore by remember { mutableIntStateOf(0) }
    var timeProgress by remember { mutableFloatStateOf(1f) }
    
    // Sample questions
    val questions = remember {
        listOf(
            Question(
                text = "Benda langit yang dikenal sebagai \"Bintang Kejora\" atau \"Bintang Fajar\" adalah:",
                answers = listOf("Mars", "Venus", "Jupiter", "Saturnus"),
                correctAnswerIndex = 1
            ),
            Question(
                text = "Unsur kimia dengan lambang Fe dan nomor atom 26 dikenal sebagai",
                answers = listOf("Emas (Gold)", "Perak (Silver)", "Besi (Iron)", "Tembaga (Copper)"),
                correctAnswerIndex = 2
            ),
            Question(
                text = "Siapakah pelukis terkenal asal Belanda yang dikenal dengan karyanya The Starry Night?",
                answers = listOf("Pablo Picasso", "Claude Monet", "Vincent van Gogh", "Leonardo da Vinci"),
                correctAnswerIndex = 2
            )
        )
    }
    
    val currentQuestion = questions[currentQuestionIndex]
    
    // Timer effect
    LaunchedEffect(currentQuestionIndex, isAnswered) {
        if (!isAnswered) {
            while (timeProgress > 0) {
                delay(100)
                timeProgress -= 0.01f
            }
            // Time's up
            isAnswered = true
            delay(1500)
            // Move to next question or end game
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                selectedAnswerIndex = -1
                isAnswered = false
                timeProgress = 1f
            } else {
                onNavigateToResult(playerScore > opponentScore)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Players section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player 1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { timeProgress },
                    modifier = Modifier
                        .width(46.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryRed,
                    trackColor = ProgressTrack
                )
            }
            
            // VS or score
            Text(
                text = "VS",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            
            // Player 2
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier
                        .width(46.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryRed,
                    trackColor = ProgressTrack
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Question card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, BorderLight),
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentQuestion.text,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp
                    ),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Answer buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizAnswerButton(
                    text = currentQuestion.answers[0],
                    onClick = {
                        if (!isAnswered) {
                            selectedAnswerIndex = 0
                            isAnswered = true
                            if (0 == currentQuestion.correctAnswerIndex) {
                                playerScore++
                            }
                            // Wait then move to next question
                            coroutineScope.launch {
                                delay(1500)
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswerIndex = -1
                                    isAnswered = false
                                    timeProgress = 1f
                                } else {
                                    onNavigateToResult(playerScore > opponentScore)
                                }
                            }
                        }
                    },
                    isSelected = selectedAnswerIndex == 0,
                    isCorrect = if (isAnswered && selectedAnswerIndex == 0) 0 == currentQuestion.correctAnswerIndex else null,
                    enabled = !isAnswered,
                    modifier = Modifier.weight(1f)
                )
                
                QuizAnswerButton(
                    text = currentQuestion.answers[1],
                    onClick = {
                        if (!isAnswered) {
                            selectedAnswerIndex = 1
                            isAnswered = true
                            if (1 == currentQuestion.correctAnswerIndex) {
                                playerScore++
                            }
                            coroutineScope.launch {
                                delay(1500)
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswerIndex = -1
                                    isAnswered = false
                                    timeProgress = 1f
                                } else {
                                    onNavigateToResult(playerScore > opponentScore)
                                }
                            }
                        }
                    },
                    isSelected = selectedAnswerIndex == 1,
                    isCorrect = if (isAnswered && selectedAnswerIndex == 1) 1 == currentQuestion.correctAnswerIndex else null,
                    enabled = !isAnswered,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuizAnswerButton(
                    text = currentQuestion.answers[2],
                    onClick = {
                        if (!isAnswered) {
                            selectedAnswerIndex = 2
                            isAnswered = true
                            if (2 == currentQuestion.correctAnswerIndex) {
                                playerScore++
                            }
                            coroutineScope.launch {
                                delay(1500)
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswerIndex = -1
                                    isAnswered = false
                                    timeProgress = 1f
                                } else {
                                    onNavigateToResult(playerScore > opponentScore)
                                }
                            }
                        }
                    },
                    isSelected = selectedAnswerIndex == 2,
                    isCorrect = if (isAnswered && selectedAnswerIndex == 2) 2 == currentQuestion.correctAnswerIndex else null,
                    enabled = !isAnswered,
                    modifier = Modifier.weight(1f)
                )
                
                QuizAnswerButton(
                    text = currentQuestion.answers[3],
                    onClick = {
                        if (!isAnswered) {
                            selectedAnswerIndex = 3
                            isAnswered = true
                            if (3 == currentQuestion.correctAnswerIndex) {
                                playerScore++
                            }
                            coroutineScope.launch {
                                delay(1500)
                                if (currentQuestionIndex < questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswerIndex = -1
                                    isAnswered = false
                                    timeProgress = 1f
                                } else {
                                    onNavigateToResult(playerScore > opponentScore)
                                }
                            }
                        }
                    },
                    isSelected = selectedAnswerIndex == 3,
                    isCorrect = if (isAnswered && selectedAnswerIndex == 3) 3 == currentQuestion.correctAnswerIndex else null,
                    enabled = !isAnswered,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
