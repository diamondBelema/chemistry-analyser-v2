package com.example.chemistryanalyser.data.repository

import com.example.chemistryanalyser.data.local.PatientDao
import com.example.chemistryanalyser.data.local.ResultDao
import com.example.chemistryanalyser.data.local.TestDao
import com.example.chemistryanalyser.data.model.Patient
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.data.model.TestResult
import com.example.chemistryanalyser.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class TestRepository(
    private val testDao: TestDao,
    private val patientDao: PatientDao,
    private val resultDao: ResultDao,
    private val apiService: ApiService
) {
    // Test Operations
    val allTests: Flow<List<Test>> = testDao.getAllTests()
    
    suspend fun insertTest(test: Test) = testDao.insertTest(test)
    suspend fun updateTest(test: Test) = testDao.updateTest(test)
    suspend fun deleteTest(test: Test) = testDao.deleteTest(test)
    suspend fun getTestById(id: String) = testDao.getTestById(id)

    // Patient Operations
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
    suspend fun insertPatient(patient: Patient) = patientDao.insertPatient(patient)

    // Result Operations
    val allResults: Flow<List<TestResult>> = resultDao.getAllResults()
    suspend fun saveResult(result: TestResult) = resultDao.insertResult(result)

    // Remote Operations
    suspend fun getStatus() = apiService.getStatus()
    suspend fun setBlank() = apiService.setBlank()
    suspend fun calculate(wavelength: Int) = apiService.calculate(wavelength)
}
