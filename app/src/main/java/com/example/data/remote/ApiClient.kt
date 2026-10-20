package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private var authToken: String? = null
    private var currentBaseUrl: String = "http://10.0.2.2:8000/api/v1/"
    private var cachedService: ExpenseFlowApiService? = null

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun setBaseUrl(url: String) {
        val sanitized = if (!url.endsWith("/")) "$url/" else url
        if (currentBaseUrl != sanitized) {
            currentBaseUrl = sanitized
            cachedService = null
        }
    }

    fun getBaseUrl(): String = currentBaseUrl

    fun getService(): ExpenseFlowApiService {
        cachedService?.let { return it }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
            authToken?.let {
                requestBuilder.header("Authorization", "Bearer $it")
            }
            requestBuilder.header("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val service = retrofit.create(ExpenseFlowApiService::class.java)
        cachedService = service
        return service
    }
}
