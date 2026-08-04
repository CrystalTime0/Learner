package com.raphdev.learner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raphdev.learner.data.model.GlossaryTerm
import kotlinx.coroutines.flow.Flow

@Dao
interface GlossaryDao {
    @Query("SELECT * FROM glossary_terms WHERE term = :term LIMIT 1")
    suspend fun getTermByName(term: String): GlossaryTerm?

    @Query("SELECT * FROM glossary_terms")
    fun getAllTerms(): Flow<List<GlossaryTerm>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(terms: List<GlossaryTerm>)
}