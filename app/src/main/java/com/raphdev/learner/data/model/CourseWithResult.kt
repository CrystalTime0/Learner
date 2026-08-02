package com.raphdev.learner.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CourseWithResult(
    @Embedded val course: Course,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val result: QuizResult?
)