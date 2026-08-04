package com.raphdev.learner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glossary_terms")
data class GlossaryTerm(
    @PrimaryKey val id: String = "",
    val term: String = "",
    val definition: String = "",
    val subject: String = "Général"
)