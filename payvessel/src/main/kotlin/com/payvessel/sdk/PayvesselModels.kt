package com.payvessel.sdk

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * Payment Parameters for initiating a checkout
 */
data class PayvesselPaymentParams(
    val amount: Double,
    val email: String,
    val currency: String = "NGN",
    val reference: String = generateReference(),
    val customerName: String? = null,
    val phone: String? = null,
    val description: String? = null,
    val channels: List<String>? = null,
    val metadata: Map<String, Any>? = null
) {
    companion object {
        private fun generateReference(): String {
            return "PV_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
        }
    }
    
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "amount" to amount,
            "email" to email,
            "currency" to currency,
            "reference" to reference,
            "name" to customerName,
            "phone" to phone,
            "description" to description,
            "channels" to channels,
            "metadata" to metadata
        ).filterValues { it != null }
    }
}

/**
 * Transaction response from Payvessel API
 */
data class PayvesselTransaction(
    val reference: String,
    val amount: Double,
    val currency: String,
    val status: String,
    val message: String? = null,
    @SerializedName("payment_method")
    val paymentMethod: String? = null,
    @SerializedName("customer_email")
    val customerEmail: String? = null,
    @SerializedName("paid_at")
    val paidAt: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
) {
    val isSuccessful: Boolean
        get() = status.equals("success", ignoreCase = true) || 
                status.equals("successful", ignoreCase = true)
}

/**
 * API Response wrapper
 */
data class PayvesselApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T?
)

/**
 * Checkout initialization response
 */
data class CheckoutInitResponse(
    @SerializedName("access_code")
    val accessCode: String,
    val reference: String,
    @SerializedName("checkout_url")
    val checkoutUrl: String?
)

/**
 * Result of a payment operation
 */
sealed class PayvesselResult {
    data class Success(val transaction: PayvesselTransaction) : PayvesselResult()
    data object Cancelled : PayvesselResult()
    data class Error(val exception: PayvesselException) : PayvesselResult()
}

/**
 * Payvessel SDK Exceptions
 */
sealed class PayvesselException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NotConfigured : PayvesselException("SDK not configured. Call Payvessel.configure() first.")
    class InvalidAmount : PayvesselException("Invalid amount. Amount must be greater than 0.")
    class InvalidEmail : PayvesselException("Invalid email address.")
    class NetworkError(message: String, cause: Throwable? = null) : PayvesselException("Network error: $message", cause)
    class ApiError(message: String) : PayvesselException("API error: $message")
    class UnknownError(message: String, cause: Throwable? = null) : PayvesselException(message, cause)
}
