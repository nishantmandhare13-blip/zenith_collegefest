package com.zenith26.fest.data.api

import com.google.gson.Gson
import com.zenith26.fest.data.model.ApiErrorResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Singleton client configuring OkHttp, Retrofit, and dynamic Base URL handling.
 */
object ApiClient {

    // Default to live cloud backend deployed on Vercel
    private const val DEFAULT_BASE_URL = "https://zenithcollegefest.vercel.app/"

    private var currentBaseUrl: String = DEFAULT_BASE_URL
    private val gson: Gson = Gson()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private var retrofit: Retrofit = buildRetrofit(currentBaseUrl)
    private var cachedApiService: ZenithApiService = retrofit.create(ZenithApiService::class.java)

    private fun buildRetrofit(url: String): Retrofit {
        val normalizedUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Get the current active API service instance.
     */
    fun getService(): ZenithApiService = cachedApiService

    /**
     * Get current configured base URL.
     */
    fun getBaseUrl(): String = currentBaseUrl

    /**
     * Dynamically update the base URL (e.g. For real device or custom IP).
     */
    @Synchronized
    fun updateBaseUrl(newUrl: String) {
        currentBaseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        retrofit = buildRetrofit(currentBaseUrl)
        cachedApiService = retrofit.create(ZenithApiService::class.java)
    }

    /**
     * Extracts exact FastAPI validation/conflict error message from HttpException.
     */
    fun parseErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                val errorBody = throwable.response()?.errorBody()?.string()
                val statusCode = throwable.code()
                if (!errorBody.isNullOrBlank()) {
                    try {
                        val parsed = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                        if (!parsed.detail.isNullOrBlank()) {
                            return parsed.detail
                        }
                    } catch (_: Exception) {
                        // Return raw error body if JSON parsing fails
                        return "$errorBody (HTTP $statusCode)"
                    }
                }
                when (statusCode) {
                    400 -> "Bad Request: Please verify submitted information."
                    404 -> "Requested item was not found."
                    409 -> "Conflict: Event is full or already registered."
                    500 -> "Server encountered an internal error."
                    else -> "HTTP error $statusCode: ${throwable.message()}"
                }
            }
            is IOException -> "Cannot reach server. Ensure FastAPI backend is running on $currentBaseUrl."
            else -> throwable.localizedMessage ?: "An unexpected error occurred."
        }
    }
}
