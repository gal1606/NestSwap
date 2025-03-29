package com.example.nestswap.Model.Networking

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimResponse(
    val lat: String,
    val lon: String,
    val display_name: String
)

interface NominatimService {
    @GET("search")
    fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Call<List<NominatimResponse>>
}
