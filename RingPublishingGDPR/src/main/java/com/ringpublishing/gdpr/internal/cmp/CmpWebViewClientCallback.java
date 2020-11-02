package com.ringpublishing.gdpr.internal.cmp;

import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;

public interface CmpWebViewClientCallback
{

    void onReceivedError(WebResourceRequest request, WebResourceError error);

    void onPageStarted(String url);

    void onPageFinished(String url);
}
