# Payvessel Android SDK

Official Payvessel Payment SDK for Android applications.

[![Maven Central](https://img.shields.io/maven-central/v/com.payvessel/payvessel-android.svg)](https://search.maven.org/artifact/com.payvessel/payvessel-android)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Features

- 💳 Accept Card, Bank Transfer, and USSD payments
- 🔒 Secure WebView-based checkout
- ✅ Transaction verification
- 🌍 Sandbox and Production environments
- 📱 Android API 21+ support

## Installation

### Gradle (Kotlin DSL)

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.payvessel:payvessel-android:1.0.0")
}
```

### Gradle (Groovy)

Add to your `build.gradle`:

```groovy
dependencies {
    implementation 'com.payvessel:payvessel-android:1.0.0'
}
```

### JitPack (Alternative)

Add JitPack repository to your root `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:

```groovy
dependencies {
    implementation 'com.github.Nex-Panther-Technologies-Ltd:payvessel-android-sdk:1.0.0'
}
```

## Quick Start

### 1. Configure the SDK

```kotlin
import com.payvessel.sdk.Payvessel
import com.payvessel.sdk.PayvesselConfig
import com.payvessel.sdk.PayvesselEnvironment

// In your Application class or main Activity
Payvessel.configure(PayvesselConfig(
    apiKey = "your_api_key",
    secretKey = "your_secret_key",
    environment = PayvesselEnvironment.SANDBOX // Use PRODUCTION for live
))
```

### 2. Initiate Payment

```kotlin
import com.payvessel.sdk.*

val params = PayvesselPaymentParams(
    amount = 5000.0, // Amount in Naira
    email = "customer@example.com",
    currency = "NGN",
    customerName = "John Doe",
    phone = "08012345678",
    description = "Payment for Order #123"
)

Payvessel.checkout(
    activity = this,
    params = params,
    callback = object : PayvesselCallback {
        override fun onSuccess(transaction: PayvesselTransaction) {
            Log.d("Payvessel", "Payment successful!")
            Log.d("Payvessel", "Reference: ${transaction.reference}")
            Log.d("Payvessel", "Amount: ${transaction.amount}")
        }
        
        override fun onError(error: PayvesselException) {
            Log.e("Payvessel", "Payment failed: ${error.message}")
        }
        
        override fun onCancel() {
            Log.d("Payvessel", "Payment cancelled by user")
        }
    }
)
```

### 3. Handle Activity Result

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    Payvessel.handleActivityResult(requestCode, resultCode, data)
}
```

### 4. Verify Transaction (Optional)

```kotlin
Payvessel.verifyTransaction(
    reference = "your_reference",
    callback = object : PayvesselVerificationCallback {
        override fun onSuccess(transaction: PayvesselTransaction) {
            if (transaction.isSuccessful) {
                Log.d("Payvessel", "Transaction verified successfully")
            }
        }
        
        override fun onError(error: PayvesselException) {
            Log.e("Payvessel", "Verification failed: ${error.message}")
        }
    }
)
```

## Using ActivityResultLauncher (Recommended)

For better results handling with the modern Activity Result API:

```kotlin
class PaymentActivity : AppCompatActivity() {
    
    private val checkoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Payvessel.handleActivityResult(
            Payvessel.CHECKOUT_REQUEST_CODE,
            result.resultCode,
            result.data
        )
    }
    
    private fun startPayment() {
        val params = PayvesselPaymentParams(
            amount = 1000.0,
            email = "test@example.com"
        )
        
        Payvessel.checkout(
            activity = this,
            launcher = checkoutLauncher,
            params = params,
            callback = object : PayvesselCallback {
                override fun onSuccess(transaction: PayvesselTransaction) {
                    showToast("Payment successful!")
                }
                override fun onError(error: PayvesselException) {
                    showToast("Error: ${error.message}")
                }
                override fun onCancel() {
                    showToast("Payment cancelled")
                }
            }
        )
    }
}
```

## Payment Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `amount` | Double | Yes | Amount in the currency's main unit (e.g., Naira) |
| `email` | String | Yes | Customer's email address |
| `currency` | String | No | Currency code (default: "NGN") |
| `reference` | String | No | Unique transaction reference (auto-generated if not provided) |
| `customerName` | String | No | Customer's full name |
| `phone` | String | No | Customer's phone number |
| `description` | String | No | Payment description |
| `channels` | List<String> | No | Payment channels to enable (e.g., listOf("card", "bank_transfer")) |
| `metadata` | Map<String, Any> | No | Additional metadata |

## Environment

| Environment | Description |
|-------------|-------------|
| `SANDBOX` | Test environment with test credentials |
| `PRODUCTION` | Live environment for real transactions |

## ProGuard Rules

If you're using ProGuard, add these rules:

```proguard
-keep class com.payvessel.sdk.** { *; }
-keepclassmembers class com.payvessel.sdk.** { *; }
```

## Permissions

The SDK requires the following permissions (automatically included):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Requirements

- Android API 21 (Lollipop) or higher
- Kotlin 1.9+
- AndroidX

## Java Support

The SDK can also be used from Java:

```java
PayvesselPaymentParams params = new PayvesselPaymentParams(
    1000.0,
    "customer@example.com",
    "NGN",
    null, // reference (auto-generated)
    "John Doe",
    "08012345678",
    "Payment description",
    null, // channels
    null  // metadata
);

Payvessel.checkout(this, params, new PayvesselCallback() {
    @Override
    public void onSuccess(@NonNull PayvesselTransaction transaction) {
        Log.d("Payvessel", "Success: " + transaction.getReference());
    }
    
    @Override
    public void onError(@NonNull PayvesselException error) {
        Log.e("Payvessel", "Error: " + error.getMessage());
    }
    
    @Override
    public void onCancel() {
        Log.d("Payvessel", "Cancelled");
    }
});
```

## Support

- **Documentation**: [docs.payvessel.com](https://docs.payvessel.com)
- **Email**: support@payvessel.com
- **Dashboard**: [dashboard.payvessel.com](https://dashboard.payvessel.com)

## License

MIT License - see [LICENSE](LICENSE) for details.
