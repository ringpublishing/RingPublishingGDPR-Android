package com.ringpublishing.gdpr.internal.cmp;

import android.annotation.SuppressLint;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.ringpublishing.gdpr.R;

import java.io.IOException;

import androidx.annotation.NonNull;

public class CmpWebView
{

    private static final String TAG = CmpWebView.class.getCanonicalName();

    private static final String JAVA_SCRIPT_INTERFACE_NAME = "Android";

    private final WebView webView;

    private final WebSettings webSettings;

    private final CmpWebViewClient cmpWebViewClient;

    private final CmpWebViewJavaScriptInterface cmpWebViewJavaScriptInterface;

    private String cmpHost;

    public CmpWebView(WebView webView, CmpWebViewClientCallback cmpWebViewClientCallback, CmpWebViewActionCallback cmpWebViewCallback, String userAgentHeader)
    {
        this.webView = webView;
        webSettings = webView.getSettings();

        cmpWebViewClient = new CmpWebViewClient(cmpWebViewClientCallback, webView);
        cmpWebViewJavaScriptInterface = new CmpWebViewJavaScriptInterface(cmpWebViewCallback);

        addUserAgent(userAgentHeader);
        enableJavaScript();
        setWebViewClient();
        setJavaScriptInterface();
    }

    private void addUserAgent(String userAgentHeader)
    {
        Log.i(TAG, "User agent: " + userAgentHeader);
        webSettings.setUserAgentString(userAgentHeader);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void enableJavaScript()
    {
        webSettings.setJavaScriptEnabled(true);
    }

    @SuppressLint("AddJavascriptInterface")
    private void setJavaScriptInterface()
    {
        webView.addJavascriptInterface(cmpWebViewJavaScriptInterface, JAVA_SCRIPT_INTERFACE_NAME);
    }

    private void setWebViewClient()
    {
        webView.setWebViewClient(cmpWebViewClient);
    }

    public void performAction(@NonNull String javaScript)
    {
        webView.post(() -> webView.loadUrl("javascript:" + javaScript));
    }

    public void setVisibility(int visibility)
    {
        webView.post(() -> webView.setVisibility(visibility));
    }

    public void attachJavaScriptInterface()
    {
        try
        {
            final String javaScript = JavaScriptResource.read(webView.getContext().getResources().openRawResource(R.raw.cmp_js_interface));
            performAction(javaScript);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Load JavaScript error", e);
            cmpWebViewJavaScriptInterface.onError("Fail attach javascript intefrace" + e.getLocalizedMessage());
        }
    }

    public String getCmpHost()
    {
        return cmpHost;
    }

    public void setCmpHost(String url)
    {
        cmpHost = url;
    }

    public void loadUrl(String url)
    {
        webView.post(() -> webView.loadUrl(url));
    }

}
