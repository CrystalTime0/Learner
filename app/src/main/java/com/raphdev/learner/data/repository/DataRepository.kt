package com.raphdev.learner.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.raphdev.learner.data.local.CourseDao
import com.raphdev.learner.data.local.ExamSubjectDao
import com.raphdev.learner.data.local.GlossaryDao
import com.raphdev.learner.data.local.QuizDao
import com.raphdev.learner.data.local.QuizResultDao
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.ExamSubject
import com.raphdev.learner.data.model.GlossaryTerm
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.model.QuizResult
import kotlinx.coroutines.tasks.await

class DataRepository(
    private val courseDao: CourseDao,
    private val quizDao: QuizDao,
    private val quizResultDao: QuizResultDao,
    private val examSubjectDao: ExamSubjectDao,
    private val glossaryDao: GlossaryDao,
    private val firestore: FirebaseFirestore
) {
    val coursesWithResults = courseDao.getCoursesWithResults()
    val examSubjects = examSubjectDao.getAllExamSubjects()

    fun getLocalQuizzes(courseId: String) = quizDao.getQuizzesByCourseId(courseId)

    suspend fun getGlossaryTerm(term: String) = glossaryDao.getTermByName(term)

    suspend fun saveQuizScore(courseId: String, score: Int, total: Int) {
        val result = QuizResult(courseId = courseId, score = score, total = total)
        quizResultDao.insertResult(result)
    }

    suspend fun seedInitialData() {
        if (courseDao.getCourseCount() == 0) {
            // 1. Cours avec mode Complet ET Fiche Synthétique + Termes du Glossaire
            val defaultCourses = listOf(
                Course(
                    id = "c1",
                    title = "L'Absolutisme sous Louis XIV",
                    subject = "Histoire",
                    content = """
                        <h1>L'affirmation de l'État monarchique</h1>
                        <p>Sous le règne de Louis XIV, l'<strong><span class="glossary-term" data-term="Absolutisme">Absolutisme</span></strong> atteint son apogée en France. Le roi concentre tous les pouvoirs (exécutif, législatif, judiciaire) au nom du droit divin.</p>
                        <h2>La centralisation du pouvoir</h2>
                        <p>Afin d'éviter de nouvelles révoltes comme la Fronde, Louis XIV soumet la noblesse à Versailles et s'entoure de ministres dévoués comme Colbert.</p>
                    """.trimIndent(),
                    summary = """
                        <h1>⚡ Fiche Résumé : L'Absolutisme</h1>
                        <ul>
                            <li><strong>Définition :</strong> Régime où le Roi détient tous les pouvoirs.</li>
                            <li><strong>Personnage clé :</strong> Louis XIV (Le Roi-Soleil).</li>
                            <li><strong>Lieu clé :</strong> Château de Versailles (domestication de la noblesse).</li>
                            <li><strong>Date clé :</strong> 1661 (Début du règne personnel de Louis XIV).</li>
                        </ul>
                    """.trimIndent()
                ),
                Course(
                    id = "c2",
                    title = "Génétique : Les Allèles",
                    subject = "SVT",
                    content = """
                        <h1>Comprendre la diversité génétique</h1>
                        <p>Chaque gène peut exister sous plusieurs versions différentes appelées <strong><span class="glossary-term" data-term="Allèle">Allèle</span></strong>.</p>
                        <p>Les individus diploïdes possèdent deux exemplaires de chaque gène, l'un d'origine paternelle, l'autre d'origine maternelle.</p>
                    """.trimIndent(),
                    summary = """
                        <h1>⚡ Fiche Résumé : Les Allèles</h1>
                        <ul>
                            <li><strong>Allèle :</strong> Version alternative d'un même gène.</li>
                            <li><strong>Homozygote :</strong> Deux allèles identiques.</li>
                            <li><strong>Hétérozygote :</strong> Deux allèles différents (Dominant / Récessif).</li>
                        </ul>
                    """.trimIndent()
                )
            )
            courseDao.insertAll(defaultCourses)

            // 2. Glossaire
            val defaultGlossary = listOf(
                GlossaryTerm(
                    id = "g1",
                    term = "Absolutisme",
                    definition = "Système politique dans lequel le souverain détient une autorité totale et illimitée, sans contrôle institutionnel.",
                    subject = "Histoire"
                ),
                GlossaryTerm(
                    id = "g2",
                    term = "Allèle",
                    definition = "Chacune des formes ou versions possibles d'un même gène, occupant une position précise sur un chromosome.",
                    subject = "SVT"
                )
            )
            glossaryDao.insertAll(defaultGlossary)

            // 3. Annales du Bac
            val defaultAnnales = listOf(
                ExamSubject(
                    id = "es1",
                    title = "Sujet Bac 2024 - Session Normale",
                    subject = "Histoire",
                    year = 2024,
                    statement = """
                        <h1>Sujet : L'évolution de la monarchie française</h1>
                        <p><strong>Consigne :</strong> À partir de vos connaissances, expliquez comment Louis XIV a utilisé le château de Versailles pour consolider son pouvoir absolu.</p>
                    """.trimIndent(),
                    correction = """
                        <h1>Corrigé Détaillé (Pas-à-Pas)</h1>
                        <h2>1. Introduction</h2>
                        <p>Rappeler le contexte de la Fronde et l'avènement du règne personnel en 1661.</p>
                        <h2>2. Versailles, instrument de contrôle</h2>
                        <p>Expliquer la vie de cour, les étiquettes et la transformation des grands nobles en courtisans dépendants des pensions royales.</p>
                        <h2>3. Conclusion</h2>
                        <p>Bilan sur l'apogée du modèle absolutiste français en Europe.</p>
                    """.trimIndent()
                )
            )
            examSubjectDao.insertAll(defaultAnnales)
        }
    }

    suspend fun syncWithFirebase(): Boolean {
        return try {
            val coursesSnapshot = firestore.collection("courses").get().await()
            val courses = coursesSnapshot.toObjects(Course::class.java)
            if (courses.isNotEmpty()) courseDao.insertAll(courses)

            val quizzesSnapshot = firestore.collection("quizzes").get().await()
            val quizzes = quizzesSnapshot.toObjects(Quiz::class.java)
            if (quizzes.isNotEmpty()) quizDao.insertAll(quizzes)

            val annalesSnapshot = firestore.collection("exam_subjects").get().await()
            val annales = annalesSnapshot.toObjects(ExamSubject::class.java)
            if (annales.isNotEmpty()) examSubjectDao.insertAll(annales)

            true
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Erreur synchro : ${e.message}", e)
            false
        }
    }
}