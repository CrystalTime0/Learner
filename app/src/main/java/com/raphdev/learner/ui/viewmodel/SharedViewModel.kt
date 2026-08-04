package com.raphdev.learner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.CourseWithResult
import com.raphdev.learner.data.model.ExamSubject
import com.raphdev.learner.data.model.GlossaryTerm
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ScreenState { HOME, COURSE, QUIZ, EXAM_DETAIL }

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModel(private val repository: DataRepository) : ViewModel() {

    val coursesGroupedBySubject: StateFlow<Map<String, List<CourseWithResult>>> =
        repository.coursesWithResults.map { list ->
            list.groupBy { it.course.subject }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val examSubjectsGroupedBySubject: StateFlow<Map<String, List<ExamSubject>>> =
        repository.examSubjects.map { list ->
            list.groupBy { it.subject }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _expandedSubjects = MutableStateFlow<Set<String>>(emptySet())
    val expandedSubjects: StateFlow<Set<String>> = _expandedSubjects.asStateFlow()

    private val _currentScreen = MutableStateFlow(ScreenState.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse = _selectedCourse.asStateFlow()

    private val _selectedExamSubject = MutableStateFlow<ExamSubject?>(null)
    val selectedExamSubject = _selectedExamSubject.asStateFlow()

    private val _selectedGlossaryTerm = MutableStateFlow<GlossaryTerm?>(null)
    val selectedGlossaryTerm = _selectedGlossaryTerm.asStateFlow()

    val currentQuizzes: StateFlow<List<Quiz>> = _selectedCourse
        .flatMapLatest { course ->
            if (course != null) repository.getLocalQuizzes(course.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialData()
        }
    }

    fun toggleSubject(subject: String) {
        val current = _expandedSubjects.value
        _expandedSubjects.value = if (current.contains(subject)) current - subject else current + subject
    }

    fun openGlossaryTerm(termName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val term = repository.getGlossaryTerm(termName)
            _selectedGlossaryTerm.value = term ?: GlossaryTerm(
                term = termName,
                definition = "Définition non disponible pour le moment."
            )
        }
    }

    fun dismissGlossary() {
        _selectedGlossaryTerm.value = null
    }

    fun navigateToCourse(course: Course) {
        _selectedCourse.value = course
        _currentScreen.value = ScreenState.COURSE
    }

    fun navigateToQuiz(course: Course) {
        _selectedCourse.value = course
        _currentScreen.value = ScreenState.QUIZ
    }

    fun navigateToExamDetail(subject: ExamSubject) {
        _selectedExamSubject.value = subject
        _currentScreen.value = ScreenState.EXAM_DETAIL
    }

    fun navigateHome() {
        _selectedCourse.value = null
        _selectedExamSubject.value = null
        _currentScreen.value = ScreenState.HOME
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
}