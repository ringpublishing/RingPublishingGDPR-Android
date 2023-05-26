package com.ringpublishing.gdpr.internal.cmp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ringpublishing.gdpr.BuildConfig;
import com.ringpublishing.gdpr.internal.log.Logger;

public class CmpWebViewClient extends WebViewClient
{
    private final CmpWebViewClientCallback callback;

    private final WebView webView;

    private final Logger log = Logger.get();

    public CmpWebViewClient(CmpWebViewClientCallback callback, WebView webView)
    {
        this.callback = callback;
        this.webView = webView;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
    {
        log.warn("Webview load url: " +  view.getUrl() +" is fail. WebView receive error: " + error.toString());
        super.onReceivedError(view, request, error);
        callback.onReceivedError(request, error);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon)
    {
        log.info( "Webview load started with url: " + url);

        callback.onPageStarted(url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        log.info("Webview load url page finish with url: " + url);

        super.onPageFinished(view, url);
        callback.onPageFinished(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
        if (Uri.parse(url).getHost().contains(BuildConfig.CMP_HOST))
        {
            //open url contents in webview
            return false;
        }
        else
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                webView.getContext().startActivity(intent);
                return true;
            }
            catch (ActivityNotFoundException e)
            {
                log.error("Activity not found exception when try open link: " + url + " Error: " + e.getLocalizedMessage());
                return false;
            }
        }
    }
}
