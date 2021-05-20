package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.storage.Storage;

public class ApiSynchronizationTask
{
    private final String TAG = ApiSynchronizationTask.class.getSimpleName();

    private final RequestsState requestsState;

    private final TenantConfiguration tenantConfiguration;

    private final Storage storage;

    private final Runnable showFormFromApplication;

    public ApiSynchronizationTask(RequestsState requestsState, TenantConfiguration tenantConfiguration, Storage storage, Runnable showFormFromApplication)
    {
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.storage = storage;
        this.showFormFromApplication = showFormFromApplication;
    }

    public synchronized void run(ConsentFormListener consentFormListener)
    {
        if (requestsState.isLoading())
        {
            Log.w(TAG, "Update apiMethodFinished, but still state is loading");
            return;
        }

        if (requestsState.isFailure() || !tenantConfiguration.isGdprApplies())
        {
            if (consentFormListener != null)
            {
                consentFormListener.onConsentsUpToDate();
            }
            return;
        }

        if (consentFormListener == null)
        {
            if (storage.isConsentOutdated())
            {
                showFormFromApplication.run();
            }
        }
        else
        {
            if (storage.isConsentOutdated() || !storage.didAskUserForConsents())
            {
                consentFormListener.onReadyToShowForm();
            }
            else
            {
                consentFormListener.onConsentsUpToDate();
            }
        }
    }

}
