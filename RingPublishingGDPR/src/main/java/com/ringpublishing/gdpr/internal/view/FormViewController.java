package com.ringpublishing.gdpr.internal.view;

import android.util.Log;

import com.ringpublishing.gdpr.RingPublishingGDPRUIConfig;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.cmp.CmpAction;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.model.State;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.task.TimeoutTask;
import com.ringpublishing.gdpr.internal.task.TimeoutTask.TimeoutCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FormViewController implements TimeoutCallback
{

    private final String TAG = FormViewController.class.getCanonicalName();

    private final State state = new State();

    private final List<String> actionsQueue = new ArrayList<>();

    private final List<ActionType> waitingToCloseActions = new ArrayList<>();

    private final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig;

    private final Api api;

    private TimeoutTask cmpLoadingTimeout;

    private FormViewImpl formViewImpl;

    private int timeoutInSeconds = 10;

    private TenantConfiguration tenantConfiguration;

    public FormViewController(@NonNull final Api api, @NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig)
    {
        this.api = api;
        this.ringPublishingGDPRUIConfig = ringPublishingGDPRUIConfig;
    }

    public void setTimeoutInSeconds(int timeoutSeconds)
    {
        this.timeoutInSeconds = timeoutSeconds;
    }

    void onDetach()
    {
        cmpLoadingTimeout.cancel();
    }


    void addAction(String action)
    {
        actionsQueue.add(action);
    }


    void executeWaitingActions()
    {
        Log.i(TAG, "FormViewImpl. call executeWaitingActions() action: " + actionsQueue.size());
        if (actionsQueue.isEmpty())
        {
            return;
        }

        for (String action : actionsQueue)
        {
            formViewImpl.performAction(action);
            Log.i(TAG, "FormViewImpl.executeWaitingActions() action: " + action);
        }

        actionsQueue.clear();
    }

    void loadCmpSite()
    {
        startLoadingTimeout();
        if (tenantConfiguration != null)
        {
            formViewImpl.loadCmpUrl(tenantConfiguration.getHost());
        }
        else
        {
            formViewImpl.onFailure("Loading cmp site fail. TenantConfiguration is null");
        }
    }

    private void startLoadingTimeout()
    {
        cmpLoadingTimeout = new TimeoutTask(this, timeoutInSeconds);
        cmpLoadingTimeout.start();
    }

    void loading()
    {
        Log.i(TAG, "setStatusLoading()");
        state.loading();
    }

    void showContent()
    {
        state.content();
        cmpLoadingTimeout.cancel();
    }

    @Override
    public void onTimeout()
    {
        Log.w(TAG, "Loading cmp site timeout");
        formViewImpl.onFailure("Loading cmp site timeout");
    }

    void setFormViewImpl(FormViewImpl formViewImpl)
    {
        this.formViewImpl = formViewImpl;
    }

    String getUserAgentHeader()
    {
        return api.getNetwork().createUserAgentHeader();
    }

    RingPublishingGDPRUIConfig getRingPublishingGDPRUIConfig()
    {
        return ringPublishingGDPRUIConfig;
    }

    public void callFormSubmittedActions()
    {
        waitingToCloseActions.add(ActionType.GET_TC_DATA);
        waitingToCloseActions.add(ActionType.GET_COMPLETE_CONSENT_DATA);

        for (ActionType waitingToCloseAction : waitingToCloseActions)
        {
            formViewImpl.performAction(CmpAction.get(waitingToCloseAction));
        }

        startLoadingTimeout();
    }

    public boolean waitingActionFinish(ActionType action)
    {
        waitingToCloseActions.remove(action);
        if (waitingToCloseActions.isEmpty())
        {
            cmpLoadingTimeout.cancel();
            return true;
        }
        return false;
    }

    public void setTenantConfiguration(TenantConfiguration tenantConfiguration)
    {
        this.tenantConfiguration = tenantConfiguration;
    }
}
