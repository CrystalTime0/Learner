package com.raphdev.learner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.Quiz
import com.raphdev.learner.data.model.QuizResult

// Version passée à 3 suite à l'ajout de la colonne 'subject'
@Database(entities = [Course::class, Quiz::class, QuizResult::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
}