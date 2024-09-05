package com.example.hw4

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetEventInfo {
    @GET("events.json")
    fun getInfoOfEvents(@Query("results") amount: Int,
                        @Query("keyword") eventSearch: String,
                        @Query("city") citySearch: String,
                        @Query("apikey") apikey: String,
                        @Query("sort") sortBy: String): Call<EventInfo>
}