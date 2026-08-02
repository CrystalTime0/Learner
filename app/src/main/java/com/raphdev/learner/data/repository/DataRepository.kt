package com.raphdev.learner.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.raphdev.learner.data.local.CourseDao
import com.raphdev.learner.data.local.QuizDao
import com.raphdev.learner.data.local.QuizResultDao
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.model.QuizResult
import kotlinx.coroutines.tasks.await

class DataRepository(
    private val courseDao: CourseDao,
    private val quizDao: QuizDao,
    private val quizResultDao: QuizResultDao,
    private val firestore: FirebaseFirestore
) {
    // Récupère les cours combinés avec les résultats des quiz
    val coursesWithResults = courseDao.getCoursesWithResults()

    fun getLocalQuizzes(courseId: String) = quizDao.getQuizzesByCourseId(courseId)

    suspend fun saveQuizScore(courseId: String, score: Int, total: Int) {
        val result = QuizResult(courseId = courseId, score = score, total = total)
        quizResultDao.insertResult(result)
    }

    suspend fun seedInitialData() {
        if (courseDao.getCourseCount() == 0) {
            val defaultCourses = listOf(
                Course(
                    id = "c1",
                    title = "Introduction à Kotlin",
                    subject = "Programmation Kotlin", // <-- Matière 1
                    content = "<p>Kotlin est un langage moderne, concis et sûr, 100% interopérable avec Java.</p>"
                ),
                Course(
                    id = "c2",
                    title = "Jetpack Compose Basics",
                    subject = "Android & UI", // <-- Matière 2
                    content = "<h1>Les bases de Compose</h1><p>Compose simplifie la création d'interfaces.</p>"
                ),
                Course(
                    id = "c3",
                    title = "Les Coroutines",
                    subject = "Programmation Kotlin", // <-- Matière 1
                    content = "<p>Les coroutines permettent de gérer l'asynchrone de manière fluide.</p>"
                )
            )
            // ... (le reste de vos quiz par défaut reste inchangé)
            courseDao.insertAll(defaultCourses)
        }
    }

    suspend fun syncWithFirebase(): Boolean {
        return try {
            Log.d("FirebaseSync", "Début de la synchronisation...")

            val coursesSnapshot = firestore.collection("courses").get().await()
            Log.d("FirebaseSync", "Documents trouvés : ${coursesSnapshot.size()}")

            val courses = coursesSnapshot.toObjects(Course::class.java)

            if (courses.isNotEmpty()) {
                Log.d("FirebaseSync", "Données récupérées : ${courses.size} cours")
                courseDao.insertAll(courses)
                Log.d("FirebaseSync", "Insertion en base locale réussie")
            } else {
                Log.d("FirebaseSync", "Aucun cours trouvé dans la collection 'courses'")
            }

            // Fais la même chose pour les quiz
            val quizzesSnapshot = firestore.collection("quizzes").get().await()
            val quizzes = quizzesSnapshot.toObjects(Quiz::class.java)
            if (quizzes.isNotEmpty()) {
                quizDao.insertAll(quizzes)
            }

            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "ERREUR CRITIQUE DE SYNCHRO : ${e.message}", e)
            false
        }
    }
}