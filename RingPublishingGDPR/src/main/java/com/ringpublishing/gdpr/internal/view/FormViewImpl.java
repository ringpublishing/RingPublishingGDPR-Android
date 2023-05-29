package com.ringpublishing.gdpr.internal.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.widget.ProgressBar;

import com.ringpublishing.gdpr.R;
import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.RingPublishingGDPRUIConfig;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.callback.GDPRActivityCallback;
import com.ringpublishing.gdpr.internal.cmp.CmpAction;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.cmp.CmpWebView;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewAction;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewClientCallback;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.network.NetworkInfo;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.ErrorView.RetryCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressLint("ViewConstructor")
public class FormViewImpl extends FormView implements RetryCallback, CmpWebViewClientCallback
{

    @NonNull
    private final NetworkInfo networkInfo;

    @NonNull
    private final CmpWebView cmpWebView;

    @NonNull
    private final ProgressBar loadingView;

    @NonNull
    private final ErrorView errorView;

    @NonNull
    private final CmpWebViewAction cmpWebViewCallback;

    @NonNull
    private final FormViewController formViewController;

    @NonNull
    private final TenantConfiguration tenantConfiguration;

    @Nullable
    private GDPRActivityCallback gdprActivityCallback;

    @NonNull
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    private final Logger log = Logger.get();

    public FormViewImpl(@NonNull final Context context,
                        @NonNull final Api api,
                        @NonNull TenantConfiguration tenantConfiguration,
                        @NonNull Storage storage,
                        @NonNull RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        super(context);

        this.formViewController = new FormViewController(this);
        this.tenantConfiguration = tenantConfiguration;
        this.ringPublishingGDPRListener = ringPublishingGDPRListener;

        this.cmpWebViewCallback = new CmpWebViewAction(storage, this.ringPublishingGDPRListener, this);

        LayoutInflater.from(context).inflate(R.layout.ring_publishing_gdpr_contest_view, this);

        networkInfo = new NetworkInfo(context.getApplicationContext());
        cmpWebView = new CmpWebView(findViewById(R.id.webview), this, cmpWebViewCallback, api.getNetwork().createUserAgentHeader());

        loadingView = findViewById(R.id.progressbar_loading);
        errorView = findViewById(R.id.error_view);
        errorView.setRetryCallback(this);
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
        log.info( "Form view showLoading()");
        errorView.setVisibility(View.GONE);
        loadingView.setVisibility(View.VISIBLE);
        cmpWebView.setVisibility(View.GONE);
    }

    public void showContent()
    {
        log.info( "Form view showContent()");
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
        loadCmpSite();
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        loadCmpSite();
    }

    private void loadCmpSite()
    {
        showLoading();
        formViewController.loadCmpSite();
        if (tenantConfiguration.getHost() != null)
        {
            cmpWebView.loadUrl(tenantConfiguration.getHost());
        }
        else
        {
            onFailure("Loading cmp site fail. TenantConfiguration is null");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_MISSING_HOST, "loadCmpSite missing host " + tenantConfiguration.getHost());
        }
    }

    public void attachJavascript(String url)
    {
        if (tenantConfiguration.getHost() != null && url.startsWith(tenantConfiguration.getHost()))
        {
            log.info( "Form view attachJavascript()");
            cmpWebView.attachJavaScriptInterface();
        }
        else
        {
            log.error("Form view attachJavascript() not called because of wrong url: " + url);
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_MISSING_HOST, "Form view attachJavascript() not called because of wrong url: " + tenantConfiguration.getHost());
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
        log.info( "Form view performActionWithBuffer() add to queue action: " + action);
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
        log.info( "Form view onPageStarted()");
        showLoading();
    }

    @Override
    public void onPageFinished(String url)
    {
        log.info( "Form view onPageFinished()");
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
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL, "Receive error loading resources. Called onReceivedError() User is offline. Request:" + request.getMethod() + " error:" + error.getDescription());
        }
        else
        {
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL, "Receive error loading resources. Called onReceivedError() User is online. Request:" + request.getMethod() + " error:" + error.getDescription());
            log.warn("Receive error loading resources" + error.toString());
        }
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

    @Override
    public void setActivityCallback(@Nullable GDPRActivityCallback ringPublishingGDPRActivity)
    {
        this.gdprActivityCallback = ringPublishingGDPRActivity;
    }

    public void hideForm()
    {
        if (this.gdprActivityCallback != null)
        {
            this.gdprActivityCallback.hideForm();
        }
    }
}
