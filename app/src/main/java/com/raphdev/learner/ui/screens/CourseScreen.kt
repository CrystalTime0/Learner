package com.raphdev.learner.ui.screens

import android.graphics.Color
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.raphdev.learner.ui.viewmodel.SharedViewModel

class WebAppInterface(private val onTermClicked: (String) -> Unit) {
    @JavascriptInterface
    fun openGlossary(term: String) {
        onTermClicked(term)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(viewModel: SharedViewModel) {
    val course by viewModel.selectedCourse.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Cours complet, 1: Fiche synthétique

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Cours complet") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Fiche synthétique") }
                )
            }

            val rawContent = if (selectedTabIndex == 0) {
                course?.content ?: "<p>Pas de contenu complet disponible.</p>"
            } else {
                course?.summary.takeIf { !it.isNullOrBlank() } ?: "<p>Pas de fiche synthétique disponible.</p>"
            }

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
                        p, ul, ol, li { margin-bottom: 12px; font-size: 15px; }
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
                        .glossary-term {
                            color: #6200EE;
                            background-color: #F0E6FF;
                            padding: 2px 6px;
                            border-radius: 4px;
                            border-bottom: 1.5px dashed #6200EE;
                            font-weight: bold;
                            cursor: pointer;
                        }
                    </style>
                </head>
                <body>
                    $rawContent

                    <script>
                        function bindGlossaryEvents() {
                            const terms = document.querySelectorAll(".glossary-term");
                            terms.forEach(function(el) {
                                el.onclick = function() {
                                    const term = this.getAttribute("data-term") || this.innerText;
                                    if (window.AndroidInterface) {
                                        window.AndroidInterface.openGlossary(term);
                                    }
                                };
                            });
                        }
                        if (document.readyState === "loading") {
                            document.addEventListener("DOMContentLoaded", bindGlossaryEvents);
                        } else {
                            bindGlossaryEvents();
                        }
                    </script>
                </body>
                </html>
            """.trimIndent()

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                        }
                        setBackgroundColor(Color.TRANSPARENT)

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    Log.d("WebViewJS", "${it.message()} -- à la ligne ${it.lineNumber()} de ${it.sourceId()}")
                                }
                                return true
                            }
                        }

                        addJavascriptInterface(WebAppInterface { term ->
                            viewModel.openGlossaryTerm(term)
                        }, "AndroidInterface")
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(
                        "https://localhost/",
                        fullHtml,
                        "text/html; charset=utf-8",
                        "UTF-8",
                        null
                    )
                }
            )
        }
    }
}