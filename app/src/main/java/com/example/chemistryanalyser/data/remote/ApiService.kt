package com.example.chemistryanalyser.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("status")
    suspend fun getStatus(): StatusResponse

    @GET("setBlank")
    suspend fun setBlank(): ActionResponse

    @GET("calculate")
    suspend fun calculate(
        @Query("wavelength") wavelength: Int
    ): CalculateResponse
}

@JsonClass(generateAdapter = true)
data class StatusResponse(
    val status: String,
    val connected: Boolean
)

@JsonClass(generateAdapter = true)
data class ActionResponse(
    val success: Boolean,
    val message: String
)

@JsonClass(generateAdapter = true)
data class CalculateResponse(
    val absorbance: Double,
    val concentration: Double? = null
)
