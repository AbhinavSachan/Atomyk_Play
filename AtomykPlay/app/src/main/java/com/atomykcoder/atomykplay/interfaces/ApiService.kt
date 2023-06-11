package com.atomykcoder.atomykplay.interfaces

import androidx.annotation.Keep
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @Keep
    @GET("search/all")
    suspend fun search(@Query("qry") query: String): ResponseBody

    @Keep
    @GET
    suspend fun getSong(@Url href: String): ResponseBody
}