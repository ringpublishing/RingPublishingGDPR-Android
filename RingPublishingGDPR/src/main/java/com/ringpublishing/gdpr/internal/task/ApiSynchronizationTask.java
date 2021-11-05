package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.RingPublishingGDPROnErrorListener;
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

    @NonNull
    private final RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener;

    public ApiSynchronizationTask(@NonNull RequestsState requestsState, @NonNull TenantConfiguration tenantConfiguration, @NonNull Storage storage, @NonNull RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener)
    {
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.storage = storage;
        this.ringPublishingGDPROnErrorListener = ringPublishingGDPROnErrorListener;
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
            ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_1, "consentFormListener is null");
            return;
        }

        if (requestsState.isFailure())
        {
            Log.d(TAG, "requestsState.isFailure()  -> onConsentsUpToDate");
            ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_2, "requestsState isFailure");
            consentFormListener.onConsentsUpToDate();
        }
        else if (!tenantConfiguration.isGdprApplies())
        {
            Log.d(TAG, "tenantConfiguration is not set -> onConsentsUpToDate");
            ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_3, "tenantConfiguration is not set");
            consentFormListener.onConsentsUpToDate();
        }
        else
        {
            if (storage.isConsentOutdated() || !storage.didAskUserForConsents())
            {
                Log.d(TAG, "isConsentOutdated | not didAskUserForConsents  -> onReadyToShowForm");
                consentFormListener.onReadyToShowForm();
            }
            else
            {
                Log.d(TAG, requestsState.toString() + " other case  -> onConsentsUpToDate");
                consentFormListener.onConsentsUpToDate();
            }
        }
    }

}
