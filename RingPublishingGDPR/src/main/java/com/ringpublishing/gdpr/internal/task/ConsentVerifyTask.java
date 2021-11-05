package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.RingPublishingGDPROnErrorListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.VerifyCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.VerifyState;
import com.ringpublishing.gdpr.internal.storage.Storage;

import java.util.Map;

import androidx.annotation.NonNull;

public class ConsentVerifyTask
{

    private final String TAG = ConsentVerifyTask.class.getCanonicalName();

    private final Storage storage;

    private final Api api;

    private RequestsState requestsState;

    @NonNull
    private final RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener;

    public ConsentVerifyTask(@NonNull Storage storage, @NonNull Api api, RequestsState requestsState, @NonNull RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener)
    {
        this.storage = storage;
        this.api = api;
        this.requestsState = requestsState;
        this.ringPublishingGDPROnErrorListener = ringPublishingGDPROnErrorListener;
    }

    public void run(Runnable finishCallback)
    {
        final Map<String, String> consents = storage.getRingConsents();

        if (consents == null || consents.isEmpty())
        {
            Log.w(TAG, "Fail verify consents. Consents are empty");
            requestsState.setVerifyState(VerifyState.FAILURE);
            ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_5, "Fail verify consents. Consents are empty");
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
                finishCallback.run();
                ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_6, status);
            }
        });
    }

}
