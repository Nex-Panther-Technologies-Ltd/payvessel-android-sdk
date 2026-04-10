package com.payvessel.sdk

/**
 * Payvessel SDK Configuration
 *
 * @param apiKey Your Payvessel API key
 * @param secretKey Your Payvessel secret key
 * @param environment The environment to use (SANDBOX or PRODUCTION)
 */
data class PayvesselConfig(
    val apiKey: String,
    val secretKey: String,
    val environment: PayvesselEnvironment = PayvesselEnvironment.SANDBOX
)

/**
 * Payvessel Environment
 */
enum class PayvesselEnvironment(val baseUrl: String) {
    SANDBOX("https://sandbox.payvessel.com"),
    PRODUCTION("https://api.payvessel.com");
    
    companion object {
        const val CHECKOUT_URL = "https://checkout.payvessel.com"
    }
}
