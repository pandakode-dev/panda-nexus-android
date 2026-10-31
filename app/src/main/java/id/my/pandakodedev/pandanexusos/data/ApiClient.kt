package com.pandakode.nexus.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NexusApiService {
    @GET("api/app_api.php?action=tv_feed")
    suspend fun getTvFeed(@Query("branch_id") branchId: Int): TvFeedResponse
}

object ApiClient {
    private const val BASE_URL = "https://nexus.pandakode-dev.my.id/"

    fun create(): NexusApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(NexusApiService::class.java)
    }
}