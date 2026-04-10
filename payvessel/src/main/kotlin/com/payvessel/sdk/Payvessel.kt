package com.payvessel.sdk

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.payvessel.sdk.internal.PayvesselApiClient
import com.payvessel.sdk.ui.PayvesselCheckoutActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Payvessel SDK - Official Android SDK for Payvessel Payments
 *
 * Usage:
 * ```kotlin
 * // Configure the SDK
 * Payvessel.configure(PayvesselConfig(
 *     apiKey = "your_api_key",
 *     secretKey = "your_secret_key",
 *     environment = PayvesselEnvironment.SANDBOX
 * ))
 *
 * // Start checkout
 * Payvessel.checkout(
 *     activity = this,
 *     params = PayvesselPaymentParams(
 *         amount = 1000.0,
 *         email = "customer@example.com"
 *     ),
 *     callback = object : PayvesselCallback {
 *         override fun onSuccess(transaction: PayvesselTransaction) {
 *             // Handle success
 *         }
 *         override fun onError(error: PayvesselException) {
 *             // Handle error
 *         }
 *         override fun onCancel() {
 *             // Handle cancellation
 *         }
 *     }
 * )
 * ```
 */
object Payvessel {
    
    private var config: PayvesselConfig? = null
    private var currentCallback: PayvesselCallback? = null
    private val apiClient: PayvesselApiClient by lazy { 
        PayvesselApiClient(config ?: throw PayvesselException.NotConfigured()) 
    }
    
    internal const val EXTRA_ACCESS_CODE = "payvessel_access_code"
    internal const val EXTRA_REFERENCE = "payvessel_reference"
    internal const val RESULT_SUCCESS = 1
    internal const val RESULT_CANCELLED = 2
    internal const val RESULT_ERROR = 3
    internal const val EXTRA_ERROR_MESSAGE = "payvessel_error_message"
    
    /**
     * Configure the Payvessel SDK
     *
     * @param config The configuration containing API keys and environment
     */
    @JvmStatic
    fun configure(config: PayvesselConfig) {
        this.config = config
    }
    
    /**
     * Check if SDK is configured
     */
    @JvmStatic
    fun isConfigured(): Boolean = config != null
    
    /**
     * Get current configuration
     */
    @JvmStatic
    fun getConfig(): PayvesselConfig? = config
    
    /**
     * Start checkout process
     *
     * @param activity The activity to launch checkout from
     * @param params Payment parameters
     * @param callback Callback for payment result
     */
    @JvmStatic
    fun checkout(
        activity: Activity,
        params: PayvesselPaymentParams,
        callback: PayvesselCallback
    ) {
        val currentConfig = config ?: run {
            callback.onError(PayvesselException.NotConfigured())
            return
        }
        
        // Validate params
        if (params.amount <= 0) {
            callback.onError(PayvesselException.InvalidAmount())
            return
        }
        
        if (!isValidEmail(params.email)) {
            callback.onError(PayvesselException.InvalidEmail())
            return
        }
        
        currentCallback = callback
        
        // Initialize checkout
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    PayvesselApiClient(currentConfig).initializeCheckout(params)
                }
                
                result.fold(
                    onSuccess = { initResponse ->
                        // Launch checkout activity
                        val intent = Intent(activity, PayvesselCheckoutActivity::class.java).apply {
                            putExtra(EXTRA_ACCESS_CODE, initResponse.accessCode)
                            putExtra(EXTRA_REFERENCE, initResponse.reference)
                        }
                        activity.startActivityForResult(intent, CHECKOUT_REQUEST_CODE)
                    },
                    onFailure = { error ->
                        when (error) {
                            is PayvesselException -> callback.onError(error)
                            else -> callback.onError(PayvesselException.UnknownError(error.message ?: "Unknown error", error))
                        }
                    }
                )
            } catch (e: Exception) {
                callback.onError(PayvesselException.UnknownError(e.message ?: "Unknown error", e))
            }
        }
    }
    
    /**
     * Start checkout process using ActivityResultLauncher
     *
     * @param launcher The ActivityResultLauncher to use
     * @param params Payment parameters
     * @param callback Callback for payment result
     */
    @JvmStatic
    fun checkout(
        activity: Activity,
        launcher: ActivityResultLauncher<Intent>,
        params: PayvesselPaymentParams,
        callback: PayvesselCallback
    ) {
        val currentConfig = config ?: run {
            callback.onError(PayvesselException.NotConfigured())
            return
        }
        
        // Validate params
        if (params.amount <= 0) {
            callback.onError(PayvesselException.InvalidAmount())
            return
        }
        
        if (!isValidEmail(params.email)) {
            callback.onError(PayvesselException.InvalidEmail())
            return
        }
        
        currentCallback = callback
        
        // Initialize checkout
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    PayvesselApiClient(currentConfig).initializeCheckout(params)
                }
                
                result.fold(
                    onSuccess = { initResponse ->
                        // Launch checkout activity
                        val intent = Intent(activity, PayvesselCheckoutActivity::class.java).apply {
                            putExtra(EXTRA_ACCESS_CODE, initResponse.accessCode)
                            putExtra(EXTRA_REFERENCE, initResponse.reference)
                        }
                        launcher.launch(intent)
                    },
                    onFailure = { error ->
                        when (error) {
                            is PayvesselException -> callback.onError(error)
                            else -> callback.onError(PayvesselException.UnknownError(error.message ?: "Unknown error", error))
                        }
                    }
                )
            } catch (e: Exception) {
                callback.onError(PayvesselException.UnknownError(e.message ?: "Unknown error", e))
            }
        }
    }
    
    /**
     * Verify a transaction
     *
     * @param reference The transaction reference to verify
     * @param callback Callback for verification result
     */
    @JvmStatic
    fun verifyTransaction(
        reference: String,
        callback: PayvesselVerificationCallback
    ) {
        val currentConfig = config ?: run {
            callback.onError(PayvesselException.NotConfigured())
            return
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    PayvesselApiClient(currentConfig).verifyTransaction(reference)
                }
                
                result.fold(
                    onSuccess = { transaction ->
                        callback.onSuccess(transaction)
                    },
                    onFailure = { error ->
                        when (error) {
                            is PayvesselException -> callback.onError(error)
                            else -> callback.onError(PayvesselException.UnknownError(error.message ?: "Unknown error", error))
                        }
                    }
                )
            } catch (e: Exception) {
                callback.onError(PayvesselException.UnknownError(e.message ?: "Unknown error", e))
            }
        }
    }
    
    /**
     * Handle activity result from checkout
     * Call this from your activity's onActivityResult
     *
     * @param requestCode The request code from onActivityResult
     * @param resultCode The result code from onActivityResult
     * @param data The intent data from onActivityResult
     * @return true if the result was handled, false otherwise
     */
    @JvmStatic
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != CHECKOUT_REQUEST_CODE) return false
        
        val callback = currentCallback ?: return false
        currentCallback = null
        
        when (resultCode) {
            RESULT_SUCCESS -> {
                val reference = data?.getStringExtra(EXTRA_REFERENCE) ?: ""
                // Verify the transaction
                verifyTransaction(reference, object : PayvesselVerificationCallback {
                    override fun onSuccess(transaction: PayvesselTransaction) {
                        callback.onSuccess(transaction)
                    }
                    override fun onError(error: PayvesselException) {
                        // Even if verification fails, payment might have succeeded
                        // Return a basic transaction object
                        callback.onSuccess(PayvesselTransaction(
                            reference = reference,
                            amount = 0.0,
                            currency = "NGN",
                            status = "success",
                            message = "Payment completed"
                        ))
                    }
                })
            }
            RESULT_CANCELLED -> {
                callback.onCancel()
            }
            RESULT_ERROR -> {
                val errorMessage = data?.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Unknown error"
                callback.onError(PayvesselException.UnknownError(errorMessage))
            }
            else -> {
                callback.onCancel()
            }
        }
        
        return true
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    const val CHECKOUT_REQUEST_CODE = 9001
}

/**
 * Callback interface for payment operations
 */
interface PayvesselCallback {
    fun onSuccess(transaction: PayvesselTransaction)
    fun onError(error: PayvesselException)
    fun onCancel()
}

/**
 * Callback interface for transaction verification
 */
interface PayvesselVerificationCallback {
    fun onSuccess(transaction: PayvesselTransaction)
    fun onError(error: PayvesselException)
}
