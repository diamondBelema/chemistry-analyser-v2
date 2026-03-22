package com.example.chemistryanalyser.data.local

import androidx.room.*
import com.example.chemistryanalyser.data.model.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): Patient?
}
