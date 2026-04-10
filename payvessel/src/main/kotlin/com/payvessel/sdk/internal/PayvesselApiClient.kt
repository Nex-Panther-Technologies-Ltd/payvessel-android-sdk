package com.payvessel.sdk.internal

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.payvessel.sdk.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Internal API client for Payvessel
 */
internal class PayvesselApiClient(private val config: PayvesselConfig) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    /**
     * Initialize checkout session
     */
    fun initializeCheckout(params: PayvesselPaymentParams): Result<CheckoutInitResponse> {
        return try {
            val url = "${config.environment.baseUrl}/pms/checkout/initialize/"
            val body = gson.toJson(params.toMap()).toRequestBody(jsonMediaType)
            
            val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", config.apiKey)
                .addHeader("api-secret", config.secretKey)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful) {
                val errorMessage = try {
                    val errorResponse = gson.fromJson(responseBody, Map::class.java)
                    errorResponse["message"]?.toString() ?: "Request failed with code ${response.code}"
                } catch (e: Exception) {
                    "Request failed with code ${response.code}"
                }
                return Result.failure(PayvesselException.ApiError(errorMessage))
            }
            
            val type = object : TypeToken<PayvesselApiResponse<CheckoutInitResponse>>() {}.type
            val apiResponse: PayvesselApiResponse<CheckoutInitResponse> = gson.fromJson(responseBody, type)
            
            if (apiResponse.status && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(PayvesselException.ApiError(apiResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(PayvesselException.NetworkError(e.message ?: "Network error", e))
        }
    }
    
    /**
     * Verify transaction
     */
    fun verifyTransaction(reference: String): Result<PayvesselTransaction> {
        return try {
            val url = "${config.environment.baseUrl}/pms/transaction/verify/$reference/"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .addHeader("api-key", config.apiKey)
                .addHeader("api-secret", config.secretKey)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (!response.isSuccessful) {
                val errorMessage = try {
                    val errorResponse = gson.fromJson(responseBody, Map::class.java)
                    errorResponse["message"]?.toString() ?: "Request failed with code ${response.code}"
                } catch (e: Exception) {
                    "Request failed with code ${response.code}"
                }
                return Result.failure(PayvesselException.ApiError(errorMessage))
            }
            
            val type = object : TypeToken<PayvesselApiResponse<PayvesselTransaction>>() {}.type
            val apiResponse: PayvesselApiResponse<PayvesselTransaction> = gson.fromJson(responseBody, type)
            
            if (apiResponse.status && apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(PayvesselException.ApiError(apiResponse.message))
            }
        } catch (e: Exception) {
            Result.failure(PayvesselException.NetworkError(e.message ?: "Network error", e))
        }
    }
}
