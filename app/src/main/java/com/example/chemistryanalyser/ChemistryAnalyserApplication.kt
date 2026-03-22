package com.example.chemistryanalyser

import android.app.Application
import androidx.room.Room
import com.example.chemistryanalyser.data.local.AppDatabase
import com.example.chemistryanalyser.data.model.Test
import com.example.chemistryanalyser.data.remote.ApiService
import com.example.chemistryanalyser.data.repository.TestRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ChemistryAnalyserApplication : Application() {
    val database by lazy { 
        Room.databaseBuilder(this, AppDatabase::class.java, "chemistry_analyser_db")
            .build() 
    }
    
    val repository by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.4.1/") // Default ESP32 AP IP
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val apiService = retrofit.create(ApiService::class.java)
        TestRepository(database.testDao(), database.patientDao(), database.resultDao(), apiService)
    }

    override fun onCreate() {
        super.onCreate()
        val scope = CoroutineScope(SupervisorJob())
        scope.launch {
            val testDao = database.testDao()
            // Check if tests already exist to avoid duplication
            // For simplicity in this example, we just insert.
            // In a real app, you'd check database count first.
            Test.getDefaultTests().forEach {
                testDao.insertTest(it)
            }
        }
    }
}
