package com.example.chemistryanalyser.data.local

import androidx.room.*
import com.example.chemistryanalyser.data.model.TestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResult)

    @Query("SELECT * FROM test_results WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getResultsByPatient(patientId: Int): Flow<List<TestResult>>

    @Delete
    suspend fun deleteResult(result: TestResult)
}
