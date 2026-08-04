package com.raphdev.learner.ui.screens

import android.graphics.Color
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.raphdev.learner.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnalesScreen(viewModel: SharedViewModel) {
    val examSubjectsGrouped by viewModel.examSubjectsGroupedBySubject.collectAsState()
    val allExamSubjects = remember(examSubjectsGrouped) {
        examSubjectsGrouped.values.flatten()
    }

    BackHandler {
        viewModel.navigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Annales & Sujets du Bac", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allExamSubjects, key = { it.id }) { subject ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateToExamDetail(subject) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subject.subject,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Bac ${subject.year}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = subject.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamDetailScreen(viewModel: SharedViewModel) {
    val examSubject by viewModel.selectedExamSubject.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    BackHandler {
        viewModel.navigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(examSubject?.title ?: "Détail Annal") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("📝 Énoncé du sujet") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("✅ Corrigé pas-à-pas") }
                )
            }

            val htmlContent = if (selectedTab == 0) {
                examSubject?.statement ?: "<p>Aucun sujet disponible.</p>"
            } else {
                examSubject?.correction ?: "<p>Aucun corrigé disponible.</p>"
            }

            val fullHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: sans-serif; padding: 16px; color: #2D3748; line-height: 1.6; }
                        h1 { color: #6200EE; font-size: 18px; }
                        h2 { color: #3700B3; font-size: 16px; }
                    </style>
                </head>
                <body>$htmlContent</body>
                </html>
            """.trimIndent()

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        setBackgroundColor(Color.TRANSPARENT)
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL("https://localhost/", fullHtml, "text/html; charset=utf-8", "UTF-8", null)
                }
            )
        }
    }
}