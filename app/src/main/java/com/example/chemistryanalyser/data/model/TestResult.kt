package com.example.chemistryanalyser.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int?,
    val patientName: String?,
    val testId: String,
    val testName: String,
    val absorbance: Double,
    val concentration: Double,
    val wavelength: Int,
    val minNormal: Double,
    val maxNormal: Double,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getStatus(): String {
        return when {
            concentration < minNormal -> "Low"
            concentration > maxNormal -> "High"
            else -> "Normal"
        }
    }
}
