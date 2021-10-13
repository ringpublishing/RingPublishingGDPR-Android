package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ApiSynchronizationTask
{

    private final String TAG = ApiSynchronizationTask.class.getCanonicalName();

    @NonNull
    private final RequestsState requestsState;

    @NonNull
    private final TenantConfiguration tenantConfiguration;

    @NonNull
    private final Storage storage;

    public ApiSynchronizationTask(@NonNull RequestsState requestsState, @NonNull TenantConfiguration tenantConfiguration, @NonNull Storage storage)
    {
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.storage = storage;
    }

    public synchronized void run(@Nullable ConsentFormListener consentFormListener)
    {
        if (requestsState.isLoading())
        {
            Log.w(TAG, "Update apiMethodFinished, but still state is loading");
            return;
        }

        if (consentFormListener == null)
        {
            Log.e(TAG, "consentFormListener is null");
            return;
        }

        if (requestsState.isFailure() || !tenantConfiguration.isGdprApplies())
        {
            Log.d(TAG, "requestsState.isFailure() | tenantConfiguration is not set -> onConsentsUpToDate");
            consentFormListener.onConsentsUpToDate();
        }
        else if (storage.isConsentOutdated() || !storage.didAskUserForConsents())
        {
            Log.d(TAG, "isConsentOutdated | not didAskUserForConsents  -> onReadyToShowForm");
            consentFormListener.onReadyToShowForm();
        }
        else
        {
            Log.d(TAG, requestsState.toString() + " other case  -> onConsentsUpToDate" );
            consentFormListener.onConsentsUpToDate();
        }
    }

}
