package com.raphdev.learner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raphdev.learner.data.model.ExamSubject
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSubjectDao {
    @Query("SELECT * FROM exam_subjects")
    fun getAllExamSubjects(): Flow<List<ExamSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<ExamSubject>)
}