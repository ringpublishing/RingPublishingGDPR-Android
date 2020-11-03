package com.ringpublishing.gdpr.internal.cmp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ringpublishing.gdpr.BuildConfig;

public class CmpWebViewClient extends WebViewClient
{

    private static final String TAG = CmpWebViewClient.class.getSimpleName();

    private CmpWebViewClientCallback callback;

    private WebView webView;

    public CmpWebViewClient(CmpWebViewClientCallback callback, WebView webView)
    {
        this.callback = callback;
        this.webView = webView;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error)
    {
        Log.w(TAG, "WebView loading error " + error.toString());

        super.onReceivedError(view, request, error);

        callback.onReceivedError(request, error);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon)
    {
        Log.i(TAG, "Load url " + url);

        callback.onPageStarted(url);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        Log.i("Load url finish %s", url);

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
                Log.e(TAG, "Activity not found to open link" + e.getLocalizedMessage());
                return false;
            }
        }
    }
}
