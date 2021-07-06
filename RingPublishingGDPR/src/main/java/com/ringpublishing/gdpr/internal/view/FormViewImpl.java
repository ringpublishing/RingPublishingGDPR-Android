package com.ringpublishing.gdpr.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.widget.ProgressBar;

import com.ringpublishing.gdpr.R;
import com.ringpublishing.gdpr.RingPublishingGDPRUIConfig;
import com.ringpublishing.gdpr.internal.cmp.CmpAction;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.cmp.CmpWebView;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewActionCallback;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewClientCallback;
import com.ringpublishing.gdpr.internal.network.NetworkInfo;
import com.ringpublishing.gdpr.internal.view.ErrorView.RetryCallback;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressLint("ViewConstructor")
public class FormViewImpl extends FormView implements RetryCallback, CmpWebViewClientCallback
{

    private static final String TAG = FormViewImpl.class.getCanonicalName();

    private final FormViewController formViewController;

    private final NetworkInfo networkInfo;

    private final CmpWebView cmpWebView;

    private final ProgressBar loadingView;

    private final ErrorView errorView;

    private final CmpWebViewActionCallback cmpWebViewCallback;

    public FormViewImpl(@NonNull final Context context, @NonNull final FormViewController formViewController, @NonNull final CmpWebViewActionCallback cmpWebViewCallback)
    {
        super(context);
        this.formViewController = formViewController;
        this.cmpWebViewCallback = cmpWebViewCallback;
        formViewController.setFormViewImpl(this);

        LayoutInflater.from(context).inflate(R.layout.ring_publishing_gdpr_contest_view, this);

        networkInfo = new NetworkInfo(context);
        cmpWebView = new CmpWebView(findViewById(R.id.webview), this, cmpWebViewCallback, formViewController.getUserAgentHeader());

        loadingView = findViewById(R.id.progressbar_loading);
        errorView = findViewById(R.id.error_view);
        errorView.setRetryCallback(this);

        setViewStyle(formViewController.getRingPublishingGDPRUIConfig());
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        showLoading();
        formViewController.loadCmpSite();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        formViewController.onDetach();
        super.onDetachedFromWindow();
    }

    public void setViewStyle(@NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig)
    {
        errorView.setTypeface(ringPublishingGDPRUIConfig.getTypeface());
        errorView.setThemeColor(ringPublishingGDPRUIConfig.getThemeColor());
        DrawableCompat.setTint(loadingView.getIndeterminateDrawable(), ringPublishingGDPRUIConfig.getThemeColor());
    }

    public void showError()
    {
        errorView.setVisibility(View.VISIBLE);
        loadingView.setVisibility(View.GONE);
        cmpWebView.setVisibility(View.GONE);
    }

    public void showLoading()
    {
        Log.i(TAG, "showLoading()");
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        cmpWebView.setVisibility(View.GONE);
    }

    public void showContent()
    {
        Log.i(TAG, "showContent()");
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.GONE);
        cmpWebView.setVisibility(View.VISIBLE);
    }

    public void performAction(String action)
    {
        cmpWebView.performAction(action);
    }

    @Override
    public void onRetryClicked()
    {
        showLoading();
        formViewController.loadCmpSite();
    }

    void loadCmpUrl(String url)
    {
        cmpWebView.setCmpHost(url);
        cmpWebView.loadUrl(url);
    }

    public void attachJavascript(String url)
    {
        if (url.startsWith(cmpWebView.getCmpHost()))
        {
            cmpWebView.attachJavaScriptInterface();
            Log.i(TAG, "attachJavascript()");
        }
        else
        {
            Log.e(TAG, "attachJavascript() not called wrong url" + url);
        }
    }

    @Override
    public void showConsentsWelcomeScreen()
    {
        performActionWithBuffer(CmpAction.get(ActionType.SHOW_WELCOME));
    }

    @Override
    public void showConsentsSettingsScreen()
    {
        performActionWithBuffer(CmpAction.get(ActionType.SHOW_SETTINGS));
    }

    @Override
    public void onFailure(String errorMessage)
    {
        cmpWebViewCallback.onActionError(errorMessage);
    }

    private void performActionWithBuffer(String action)
    {
        Log.i(TAG, "performActionWithBuffer() add action to queue" + action);
        formViewController.addAction(action);
    }

    public void cmpReady()
    {
        showContent();
        formViewController.showContent();
    }

    @Override
    public void onPageStarted(String url)
    {
        Log.i(TAG, "onPageStarted()");
        showLoading();
        formViewController.loading();
    }

    @Override
    public void onPageFinished(String url)
    {
        Log.i(TAG, "onPageFinished()");
        attachJavascript(url);
        formViewController.executeWaitingActions();
    }

    @Override
    public void onReceivedError(WebResourceRequest request, WebResourceError error)
    {
        //Requirement - show error only when network is off. When no resources, then continue.
        if (!networkInfo.isOnline())
        {
            showError();
        }

        Log.w(TAG, "Receive error loading resources" + error.toString());
    }

    public boolean isOnline()
    {
        return networkInfo.isOnline();
    }

    public void formSubmittedAction()
    {
        post(this::showLoading);

        formViewController.callFormSubmittedActions();
    }

    public boolean waitingActionFinish(ActionType actionType)
    {
        return formViewController.waitingActionFinish(actionType);
    }

    public void setTimeoutInSeconds(int timeoutInSeconds)
    {
        formViewController.setTimeoutInSeconds(timeoutInSeconds);
    }
}
