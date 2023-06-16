package com.ringpublishing.gdpr.internal.task;

import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.VerifyCallback;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.VerifyState;
import com.ringpublishing.gdpr.internal.storage.Storage;

import java.util.Map;

import androidx.annotation.NonNull;

public class ConsentVerifyTask
{
    private final Storage storage;

    private final Api api;

    private final RequestsState requestsState;

    @NonNull
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    private final Logger log = Logger.get();

    public ConsentVerifyTask(@NonNull Storage storage, @NonNull Api api, RequestsState requestsState, @NonNull RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        this.storage = storage;
        this.api = api;
        this.requestsState = requestsState;
        this.ringPublishingGDPRListener = ringPublishingGDPRListener;
    }

    public void run(Runnable finishCallback)
    {
        final Map<String, String> consents = storage.getRingConsents();

        if (consents == null || consents.isEmpty())
        {
            log.error("Fail verify consents. Consents are empty");
            requestsState.setVerifyState(VerifyState.FAILURE);
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.LOCAL_STORAGE_CONSENTS_EMPTY, "Missing consents: " + consents);
            finishCallback.run();
            return;
        }

        api.verify(consents, new VerifyCallback()
        {
            @Override
            public void onOutdated(@NonNull String rawStatus)
            {
                storage.setConsentOutdated(true);
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                requestsState.setVerifyState(VerifyState.OUTDATED);
                finishCallback.run();
            }

            @Override
            public void onActual(@NonNull String rawStatus)
            {
                storage.setConsentOutdated(false);
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                requestsState.setVerifyState(VerifyState.ACTUAL);
                finishCallback.run();
            }

            @Override
            public void onFailure(@NonNull String status)
            {
                storage.saveLastAPIConsentsCheckStatus(status);
                requestsState.setVerifyState(VerifyState.FAILURE);
                log.error("Verify consents failure. Status: " + status);
                ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_VERIFY_CONSENTS_STATE, "Verify consents failure: " + status);
                finishCallback.run();
            }
        });
    }

}
