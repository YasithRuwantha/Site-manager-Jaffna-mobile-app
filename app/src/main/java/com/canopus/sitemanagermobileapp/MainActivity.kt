package com.canopus.SiteManagerMobileApp

import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)

        setupWebView()

        // Load your website - replace with your actual hosted URL
        webView.loadUrl("https://jae.canopuz.com/index.php/login") // Replace with your site URL
    }

    private fun setupWebView() {
        // Enable JavaScript
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true

        // Allow zoom controls
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false

        // Add JavaScript interface for logout
        webView.addJavascriptInterface(WebAppInterface(), "Android")

        // Set WebView client to handle page navigation
        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Check if logout URL is being loaded
                if (url?.contains("logout") == true) {
                    clearWebViewData()
                }
                // Load URLs within the WebView instead of external browser
                url?.let { view?.loadUrl(it) }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // You can add loading indicator here if needed
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject logout functionality into the web page
                injectLogoutScript()
            }
        }
    }

    // JavaScript Interface for web app to call native functions
    inner class WebAppInterface {
        @JavascriptInterface
        fun logout() {
            runOnUiThread {
                clearWebViewData()
                webView.loadUrl("https://jae.canopuz.com/index.php/login")
                Toast.makeText(this@MainActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearWebViewData() {
        // Clear all WebView data
        webView.clearCache(true)
        webView.clearHistory()
        webView.clearFormData()

        // Clear cookies and storage
        android.webkit.CookieManager.getInstance().removeAllCookies(null)
        android.webkit.WebStorage.getInstance().deleteAllData()
    }

    private fun injectLogoutScript() {
        val javascript = """
            javascript:(function() {
                // Override any existing logout functions
                window.originalLogout = window.logout;
                
                // Create new logout function that calls Android
                window.logout = function() {
                    if (typeof Android !== 'undefined' && Android.logout) {
                        Android.logout();
                    } else {
                        window.location.href = 'index.php/login';
                    }
                };
                
                
                 Find and update logout buttons
                setTimeout(function() {
                    var logoutElements = document.querySelectorAll('a[href*="logout"], button[onclick*="logout"], .logout, #logout, [class*="logout"], [id*="logout"]');
                    
                    for (var i = 0; i < logoutElements.length; i++) {
                        var element = logoutElements[i];
                        element.onclick = function(e) {
                            e.preventDefault();
                            if (typeof Android !== 'undefined' && Android.logout) {
                                Android.logout();
                            } else {
                                window.location.href = 'index.php/login';
                            }
                            return false;
                        };
                    }
                }, 1000);
            })();
        """

        webView.evaluateJavascript(javascript, null)
    }

    // Handle back button press
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // If WebView has history, go back in WebView
            webView.goBack()
        } else {
            // If no WebView history, show exit confirmation
            showExitConfirmation()
        }
    }

    private fun showExitConfirmation() {
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        // Alternative: You can uncomment this to exit directly
        // super.onBackPressed()

        // Or use this for immediate exit
        finish()
    }
}