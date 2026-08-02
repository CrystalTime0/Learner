package com.raphdev.learner.ui.screens

import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.raphdev.learner.ui.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(viewModel: SharedViewModel) {
    val course by viewModel.selectedCourse.collectAsState()

    // Intercepte le bouton/geste physique "Retour" du téléphone pour revenir à l'accueil
    BackHandler {
        viewModel.navigateHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.title ?: "Cours") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        val courseContent = course?.content ?: "<p>Contenu introuvable</p>"

        val fullHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                        color: #2D3748;
                        background-color: #FFFFFF;
                        padding: 16px;
                        margin: 0;
                        line-height: 1.6;
                    }
                    h1 { color: #6200EE; font-size: 20px; border-bottom: 2px solid #6200EE; padding-bottom: 6px; }
                    h2 { color: #3700B3; font-size: 16px; margin-top: 24px; }
                    p, ul, ol { margin-bottom: 12px; }
                    button {
                        background-color: #6200EE;
                        color: white;
                        border: none;
                        padding: 10px 16px;
                        border-radius: 8px;
                        font-weight: bold;
                        cursor: pointer;
                        margin: 8px 0;
                    }
                    button:active { background-color: #3700B3; }
                </style>
            </head>
            <body>
                $courseContent
            </body>
            </html>
        """.trimIndent()

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    setBackgroundColor(Color.TRANSPARENT)

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            consoleMessage?.let {
                                Log.d("WebViewJS", "${it.message()} -- de la ligne ${it.lineNumber()} of ${it.sourceId()}")
                            }
                            return true
                        }
                    }
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
            }
        )
    }
}