package com.raphdev.learner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.raphdev.learner.data.model.Course
import com.raphdev.learner.data.model.CourseWithResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Transaction
    @Query("SELECT * FROM courses")
    fun getCoursesWithResults(): Flow<List<CourseWithResult>>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
}