package com.raphdev.learner.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raphdev.learner.data.model.CourseWithResult
import com.raphdev.learner.ui.viewmodel.SharedViewModel
import com.raphdev.learner.ui.viewmodel.ScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator(viewModel: SharedViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedGlossaryTerm by viewModel.selectedGlossaryTerm.collectAsState()

    // Gestion de la Pop-up Glossaire
    if (selectedGlossaryTerm != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.dismissGlossary() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = selectedGlossaryTerm!!.term,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = selectedGlossaryTerm!!.subject,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = selectedGlossaryTerm!!.definition,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.dismissGlossary() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("J'ai compris")
                }
            }
        }
    }

    when (currentScreen) {
        ScreenState.HOME -> HomeScreen(viewModel)
        ScreenState.COURSE -> CourseScreen(viewModel)
        ScreenState.QUIZ -> QuizScreen(viewModel)
        ScreenState.ANNALES -> AnnalesScreen(viewModel)
        ScreenState.EXAM_DETAIL -> ExamDetailScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: SharedViewModel) {
    val coursesGroupedBySubject by viewModel.coursesGroupedBySubject.collectAsState()
    val expandedSubjects by viewModel.expandedSubjects.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Révisions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 16.dp).size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.manualSync() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Synchroniser")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // BOUTON ACCÈS AUX ANNALES DU BAC
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clickable { viewModel.navigateToAnnales() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Annales & Sujets du Bac",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Entraînez-vous sur des sujets réels corrigés pas-à-pas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            coursesGroupedBySubject.forEach { (subject, coursesList) ->
                val isExpanded = expandedSubjects.contains(subject)

                item {
                    SubjectAccordionHeader(
                        subjectName = subject,
                        courseCount = coursesList.size,
                        isExpanded = isExpanded,
                        onToggle = { viewModel.toggleSubject(subject) }
                    )
                }

                if (isExpanded) {
                    items(coursesList) { item ->
                        CourseCardItem(item = item, viewModel = viewModel)
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun SubjectAccordionHeader(
    subjectName: String,
    courseCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Surface(
        color = if (isExpanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$subjectName ($courseCount)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Réduire" else "Développer",
                modifier = Modifier.rotate(arrowRotation),
                tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun CourseCardItem(item: CourseWithResult, viewModel: SharedViewModel) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.course.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (item.result != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Score : ${item.result.score}/${item.result.total}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.navigateToCourse(item.course) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lire")
                }
                OutlinedButton(
                    onClick = { viewModel.navigateToQuiz(item.course) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (item.result != null) "Refaire Quiz" else "Quiz")
                }
            }
        }
    }
}