package com.raphdev.learner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.CourseWithResult
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.repository.DataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenState { HOME, COURSE, QUIZ }

class SharedViewModel(private val repository: DataRepository) : ViewModel() {

    // Ancienne liste plate complète
    val coursesWithResults = repository.coursesWithResults.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dictionnaire regroupant les cours par matière (Subject -> Liste de cours)
    val coursesGroupedBySubject: StateFlow<Map<String, List<CourseWithResult>>> =
        repository.coursesWithResults.map { list ->
            list.groupBy { it.course.subject }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // NOUVEAU : Sauvegarde des matières OUVERTES.
    // Par défaut, cet Set est vide -> Tous les menus sont FERMÉS par défaut.
    private val _expandedSubjects = MutableStateFlow<Set<String>>(emptySet())
    val expandedSubjects: StateFlow<Set<String>> = _expandedSubjects.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenState.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse = _selectedCourse.asStateFlow()

    private val _currentQuizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val currentQuizzes: StateFlow<List<Quiz>> = _currentQuizzes.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    // NOUVEAU : Ouvre ou ferme le menu déroulant d'une matière
    fun toggleSubject(subject: String) {
        val currentSet = _expandedSubjects.value
        if (currentSet.contains(subject)) {
            _expandedSubjects.value = currentSet - subject // Fermer
        } else {
            _expandedSubjects.value = currentSet + subject // Ouvrir
        }
    }

    fun manualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncWithFirebase()
            _isSyncing.value = false
        }
    }

    fun saveQuizScore(score: Int, total: Int) {
        val courseId = _selectedCourse.value?.id ?: return
        viewModelScope.launch {
            repository.saveQuizScore(courseId, score, total)
        }
    }

    fun navigateToCourse(course: Course) {
        _selectedCourse.value = course
        _currentScreen.value = ScreenState.COURSE
    }

    fun navigateToQuiz(course: Course) {
        _selectedCourse.value = course
        viewModelScope.launch {
            repository.getLocalQuizzes(course.id).collect { quizzes ->
                _currentQuizzes.value = quizzes
            }
        }
        _currentScreen.value = ScreenState.QUIZ
    }

    fun navigateHome() {
        _selectedCourse.value = null
        _currentScreen.value = ScreenState.HOME
    }
}