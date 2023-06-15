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
            log.warn("In api synchronization task update apiMethodFinished, but still state is loading, so ignored");
            return;
        }

        if (consentFormListener == null)
        {
            log.info("In Api synchronization task consentFormListener is null. Listener is currently not assigned to GDPR. Nothing to inform");
            return;
        }

        if (requestsState.isFailure())
        {
            log.debug("In Api synchronization task in requests state check is Failure. Ignore check and deliver onConsentsUpToDate callback");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.REQUESTS_STATE_FAILURE, "ApiSynchronizationTask requestsState.isFailure()");
            consentFormListener.onConsentsUpToDate();
        }
        else if (!tenantConfiguration.isGdprApplies())
        {
            log.debug( "In Api synchronization task tenantConfiguration is not set. Ignore check and deliver onConsentsUpToDate callback");
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.MISSING_TENANT_CONFIGURATION, "ApiSynchronizationTask !tenantConfiguration.isGdprApplies()");
            consentFormListener.onConsentsUpToDate();
        }
        else
        {
            if (storage.isConsentOutdated() || !storage.didAskUserForConsents())
            {
                log.debug("In Api synchronization task consent is outdated or not ask User for for consents before. So call ");
                consentFormListener.onReadyToShowForm();
            }
            else
            {
                log.debug("In Api synchronization task consents are up to date with requestsState: " + requestsState.toString());
                consentFormListener.onConsentsUpToDate();
            }
        }
    }

}
