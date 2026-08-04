package com.raphdev.learner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_subjects")
data class ExamSubject(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val subject: String = "Général",
    val year: Int = 2024,
    val statement: String = "",
    val correction: String = ""
)