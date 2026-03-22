package com.example.chemistryanalyser.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chemistryanalyser.data.model.Patient
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.data.model.TestResult

@Database(entities = [Test::class, Patient::class, TestResult::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun testDao(): TestDao
    abstract fun patientDao(): PatientDao
    abstract fun resultDao(): ResultDao
}
