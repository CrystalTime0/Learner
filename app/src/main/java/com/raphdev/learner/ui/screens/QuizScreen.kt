package com.raphdev.learner.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raphdev.learner.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: SharedViewModel) {
    val course by viewModel.selectedCourse.collectAsState()
    val quizzes by viewModel.currentQuizzes.collectAsState()

    val courseId = course?.id ?: ""
    var currentQuizIndex by remember(courseId) { mutableIntStateOf(0) }
    var score by remember(courseId) { mutableIntStateOf(0) }
    var quizFinished by remember(courseId) { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz : ${course?.title ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (quizzes.isEmpty()) {
                Text("Aucun quiz disponible pour ce cours.", modifier = Modifier.padding(top = 32.dp))
            } else if (quizFinished) {
                Spacer(modifier = Modifier.height(32.dp))
                Text("🎉 Quiz Terminé !", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Score : $score / ${quizzes.size}",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.navigateHome() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Retour à l'accueil", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                val currentQuiz = quizzes.getOrNull(currentQuizIndex)
                if (currentQuiz != null) {
                    val progress by animateFloatAsState(
                        targetValue = (currentQuizIndex + 1).toFloat() / quizzes.size.toFloat(),
                        label = "QuizProgress"
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = currentQuiz.question,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    currentQuiz.options.forEachIndexed { index, option ->
                        OutlinedButton(
                            onClick = {
                                val newScore = if (index == currentQuiz.correctAnswerIndex) score + 1 else score
                                if (index == currentQuiz.correctAnswerIndex) score = newScore

                                if (currentQuizIndex < quizzes.size - 1) {
                                    currentQuizIndex++
                                } else {
                                    quizFinished = true
                                    viewModel.saveQuizScore(newScore, quizzes.size)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(56.dp)
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}