package com.raphdev.learner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_subjects")
data class ExamSubject(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val subject: String = "Général",
    val year: Int = 2024,
    val statement: String = "", // Énoncé HTML du sujet
    val correction: String = ""  // Corrigé pas-à-pas HTML
)