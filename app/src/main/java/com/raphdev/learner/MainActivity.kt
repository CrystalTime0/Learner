package com.raphdev.learner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.raphdev.learner.data.repository.DataRepository
import com.raphdev.learner.ui.screens.AppNavigator
import com.raphdev.learner.ui.viewmodel.SharedViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as RevisionApp
        val firestore = FirebaseFirestore.getInstance()

        val repository = DataRepository(
            courseDao = app.database.courseDao(),
            quizDao = app.database.quizDao(),
            quizResultDao = app.database.quizResultDao(),
            firestore = firestore
        )

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SharedViewModel(repository) as T
            }
        }

        val viewModel = ViewModelProvider(this, factory)[SharedViewModel::class.java]

        setContent {
            MaterialTheme {
                AppNavigator(viewModel = viewModel)
            }
        }
    }
}