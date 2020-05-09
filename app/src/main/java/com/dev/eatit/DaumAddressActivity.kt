package com.dev.eatit

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView

class DaumAddressActivity : AppCompatActivity() {

    lateinit var webView : WebView
    lateinit var handler : Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daum_address)
        handler = Handler()
        init_webView()

    }

    private fun init_webView(){
        webView = findViewById(R.id.webView_address)

        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.addJavascriptInterface(AndroidBridge(), "AddressCallback")
        /*webView.settings.useWideViewPort = true
        webView.settings.loadWithOverviewMode = true*/

        webView.webChromeClient = WebChromeClient()
        /*webView.webChromeClient = object : WebChromeClient(){
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                // Dialog Create Code
                // Dialog Create Code
                val newWebView = WebView(this@AddressActivity)
                val webSettings = newWebView.settings
                webSettings.javaScriptEnabled = true

                val dialog = Dialog(this@AddressActivity)
                dialog.setContentView(newWebView)

                val params: ViewGroup.LayoutParams = dialog.getWindow()!!.getAttributes()
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                dialog.getWindow()?.setAttributes(params as WindowManager.LayoutParams)
                dialog.show()
                newWebView.setWebChromeClient(object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView) {
                        dialog.dismiss()
                    }
                })

                // WebView Popup에서 내용이 안보이고 빈 화면만 보여 아래 코드 추가
                // WebView Popup에서 내용이 안보이고 빈 화면만 보여 아래 코드 추가
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        return false
                    }
                }

                (resultMsg!!.obj as WebViewTransport).webView = newWebView
                resultMsg.sendToTarget()
                return true
            }
        }*/

        webView.loadUrl("http://112.170.96.54:8009/address")
    }

    inner class AndroidBridge{
        @JavascriptInterface
        fun setAddress(arg1: String, arg2 : String, arg3 : String){
            handler.post(object : Runnable{
                override fun run() {
                    var intent = Intent(this@DaumAddressActivity, MainActivity::class.java)
                    intent.putExtra("address", String.format("(%s) %s %s", arg1, arg2, arg3))
                    setResult(Activity.RESULT_OK, intent)
                    init_webView()
                    finish()
                }
            })
        }
    }
}
