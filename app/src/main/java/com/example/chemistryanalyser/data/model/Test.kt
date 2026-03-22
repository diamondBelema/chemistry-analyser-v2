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
    val category: String,
    val minNormal: Double,
    val maxNormal: Double,
    val unit: String = "mg/dL"
) {
    companion object {
        fun getDefaultTests(): List<Test> {
            return listOf(
                // Lipid Profile
                Test("ldl", "LDL Cholesterol", 100.0, 0.5, 500, "Lipid Profile", 0.0, 130.0),
                Test("chol", "Total Cholesterol", 200.0, 0.6, 505, "Lipid Profile", 125.0, 200.0),
                Test("trig", "Triglyceride", 150.0, 0.4, 546, "Lipid Profile", 0.0, 150.0),
                
                // Liver Function
                Test("ast", "AST (SGOT)", 40.0, 0.3, 340, "Liver Function", 8.0, 48.0, "U/L"),
                Test("alt", "ALT (SGPT)", 40.0, 0.3, 340, "Liver Function", 7.0, 55.0, "U/L"),
                Test("alp", "ALP", 120.0, 0.4, 405, "Liver Function", 40.0, 129.0, "U/L"),
                Test("ggt", "GGT", 50.0, 0.2, 405, "Liver Function", 8.0, 61.0, "U/L"),
                
                // Others
                Test("protein", "Serum Protein", 7.0, 0.5, 546, "General", 6.0, 8.3, "g/dL"),
                Test("glucose", "Glucose", 100.0, 0.4, 505, "General", 70.0, 99.0)
            )
        }
    }
}
