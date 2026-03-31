package com.example.chemistryanalyser.data.local

import androidx.room.*
import com.example.chemistryanalyser.data.model.Test
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests")
    fun getAllTests(): Flow<List<Test>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: Test)

    @Update
    suspend fun updateTest(test: Test)

    @Delete
    suspend fun deleteTest(test: Test)

    @Query("SELECT * FROM tests WHERE id = :testId")
    suspend fun getTestById(testId: String): Test?

    @Query("SELECT COUNT(*) FROM tests")
    suspend fun getCount(): Int
}