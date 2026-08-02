package com.raphdev.learner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val subject: String = "Général", // <-- NOUVEAU : La matière du cours
    val content: String = ""
)