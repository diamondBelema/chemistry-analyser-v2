package com.example.chemistryanalyser.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class Test(
    @PrimaryKey val id: String,
    val name: String,
    val standardConcentration: Double,
    val standardAbsorbance: Double,
    val optimalWavelength: Int,
    val category: String // e.g., "Lipid Profile", "Liver Function", etc.
) {
    companion object {
        fun getDefaultTests(): List<Test> {
            return listOf(
                // Lipid Profile
                Test("ldl", "LDL Cholesterol", 100.0, 0.5, 500, "Lipid Profile"),
                Test("chol", "Total Cholesterol", 200.0, 0.6, 505, "Lipid Profile"),
                Test("trig", "Triglyceride", 150.0, 0.4, 546, "Lipid Profile"),
                
                // Liver Function
                Test("ast", "AST (SGOT)", 40.0, 0.3, 340, "Liver Function"),
                Test("alt", "ALT (SGPT)", 40.0, 0.3, 340, "Liver Function"),
                Test("alp", "ALP", 120.0, 0.4, 405, "Liver Function"),
                Test("ggt", "GGT", 50.0, 0.2, 405, "Liver Function"),
                
                // Others
                Test("protein", "Serum Protein", 7.0, 0.5, 546, "General"),
                Test("glucose", "Glucose", 100.0, 0.4, 505, "General")
            )
        }
    }
}
