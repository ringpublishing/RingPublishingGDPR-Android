package com.ringpublishing.gdpr.internal.task;

import com.ringpublishing.gdpr.ConsentFormListener;
import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ApiSynchronizationTask
{
    @NonNull
    private final RequestsState requestsState;

    @NonNull
    private final TenantConfiguration tenantConfiguration;

    @NonNull
    private final Storage storage;

    @NonNull
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    private final Logger log = Logger.get();

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
            log.warn("Update apiMethodFinished, but still state is loading");
            return;
        }

        if (consentFormListener == null)
        {
            log.error("consentFormListener is null");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.EMPTY_CONSENT_FORM_LISTER);
            return;
        }

        if (requestsState.isFailure())
        {
            log.debug("requestsState.isFailure()  -> onConsentsUpToDate");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.REQUESTS_STATE_FAILURE);
            consentFormListener.onConsentsUpToDate();
        }
        else if (!tenantConfiguration.isGdprApplies())
        {
            log.debug( "tenantConfiguration is not set -> onConsentsUpToDate");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.MISSING_TENANT_CONFIGURATION);
            consentFormListener.onConsentsUpToDate();
        }
        else
        {
            if (storage.isConsentOutdated() || !storage.didAskUserForConsents())
            {
                log.debug("isConsentOutdated | not didAskUserForConsents  -> onReadyToShowForm");
                consentFormListener.onReadyToShowForm();
            }
            else
            {
                log.debug(requestsState.toString() + " other case  -> onConsentsUpToDate");
                consentFormListener.onConsentsUpToDate();
            }
        }
    }

}
