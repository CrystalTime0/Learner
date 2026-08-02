package com.raphdev.learner

import android.app.Application
import androidx.room.Room
import com.raphdev.learner.data.local.AppDatabase

class RevisionApp : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "revisions-db"
        )
            .fallbackToDestructiveMigration() // Recrée la BDD locale proprement suite à l'ajout de QuizResult
            .build()
    }
}