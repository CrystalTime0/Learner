package com.raphdev.learner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey val courseId: String,
    val score: Int,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis()
)