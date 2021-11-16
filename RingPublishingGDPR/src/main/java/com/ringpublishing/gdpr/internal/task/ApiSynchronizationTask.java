package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
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
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    public ApiSynchronizationTask(@NonNull RequestsState requestsState, @NonNull TenantConfiguration tenantConfiguration, @NonNull Storage storage, @NonNull RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.storage = storage;
        this.ringPublishingGDPRListener = ringPublishingGDPRListener;
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
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.EMPTY_CONSENT_FORM_LISTER);
            return;
        }

        if (requestsState.isFailure())
        {
            Log.d(TAG, "requestsState.isFailure()  -> onConsentsUpToDate");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.REQUESTS_STATE_FAILURE);
            consentFormListener.onConsentsUpToDate();
        }
        else if (!tenantConfiguration.isGdprApplies())
        {
            Log.d(TAG, "tenantConfiguration is not set -> onConsentsUpToDate");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.MISSING_TENANT_CONFIGURATION);
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
