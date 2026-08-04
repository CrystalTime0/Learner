package com.raphdev.learner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.ExamSubject
import com.raphdev.learner.data.model.GlossaryTerm
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.model.QuizResult

@Database(
    entities = [Course::class, Quiz::class, QuizResult::class, ExamSubject::class, GlossaryTerm::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun examSubjectDao(): ExamSubjectDao
    abstract fun glossaryDao(): GlossaryDao
}