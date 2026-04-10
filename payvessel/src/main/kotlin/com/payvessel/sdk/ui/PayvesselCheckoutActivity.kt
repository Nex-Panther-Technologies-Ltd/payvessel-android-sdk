package com.payvessel.sdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.payvessel.sdk.Payvessel
import com.payvessel.sdk.PayvesselEnvironment
import com.payvessel.sdk.R

/**
 * Checkout Activity that displays the Payvessel checkout WebView
 */
class PayvesselCheckoutActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: Toolbar
    
    private var accessCode: String = ""
    private var reference: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payvessel_checkout)
        
        accessCode = intent.getStringExtra(Payvessel.EXTRA_ACCESS_CODE) ?: ""
        reference = intent.getStringExtra(Payvessel.EXTRA_REFERENCE) ?: ""
        
        if (accessCode.isEmpty()) {
            finishWithError("Invalid checkout session")
            return
        }
        
        setupViews()
        setupWebView()
        loadCheckout()
    }
    
    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Payvessel Checkout"
        }
        
        toolbar.setNavigationOnClickListener {
            finishWithCancelled()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
        }
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
            
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleUrl(url)
            }
            
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.let { handleUrl(it) } ?: false
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
            }
        }
    }
    
    private fun handleUrl(url: String): Boolean {
        val uri = Uri.parse(url)
        
        // Check for callback URLs
        when {
            url.contains("/callback/success") || 
            url.contains("payvessel://success") ||
            url.contains("status=success") -> {
                finishWithSuccess()
                return true
            }
            url.contains("/callback/cancel") || 
            url.contains("payvessel://cancel") ||
            url.contains("status=cancel") -> {
                finishWithCancelled()
                return true
            }
            url.contains("/callback/failure") || 
            url.contains("/callback/error") ||
            url.contains("payvessel://failure") ||
            url.contains("payvessel://error") ||
            url.contains("status=failed") -> {
                finishWithError("Payment failed")
                return true
            }
            url.startsWith("tel:") || 
            url.startsWith("mailto:") || 
            url.startsWith("sms:") -> {
                // Open in external app
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: Exception) {
                    // Ignore if no app can handle it
                }
                return true
            }
        }
        
        return false
    }
    
    private fun loadCheckout() {
        val checkoutUrl = "${PayvesselEnvironment.CHECKOUT_URL}/$accessCode"
        webView.loadUrl(checkoutUrl)
    }
    
    private fun finishWithSuccess() {
        val resultIntent = Intent().apply {
            putExtra(Payvessel.EXTRA_REFERENCE, reference)
        }
        setResult(Payvessel.RESULT_SUCCESS, resultIntent)
        finish()
    }
    
    private fun finishWithCancelled() {
        setResult(Payvessel.RESULT_CANCELLED)
        finish()
    }
    
    private fun finishWithError(message: String) {
        val resultIntent = Intent().apply {
            putExtra(Payvessel.EXTRA_ERROR_MESSAGE, message)
        }
        setResult(Payvessel.RESULT_ERROR, resultIntent)
        finish()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finishWithCancelled()
        }
    }
}
