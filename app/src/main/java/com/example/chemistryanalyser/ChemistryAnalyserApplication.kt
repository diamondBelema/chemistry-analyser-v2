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
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ChemistryAnalyserApplication : Application() {

    val database by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "chemistry_analyser_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository by lazy {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        // FIX: Set explicit timeouts so connect() fails fast with an error
        // instead of hanging the UI indefinitely when the ESP32 is unreachable.
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)   // fail quickly if ESP32 AP not found
            .readTimeout(10, TimeUnit.SECONDS)      // allow sensor read time
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.4.1/") // ESP32 SoftAP default IP
            .client(okHttpClient)
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
            // Only seed defaults if the table is empty — prevents duplicating tests on restart
            if (testDao.getCount() == 0) {
                Test.getDefaultTests().forEach { testDao.insertTest(it) }
            }
        }
    }
}