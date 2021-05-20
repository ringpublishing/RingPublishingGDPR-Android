package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.VerifyCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.VerifyState;
import com.ringpublishing.gdpr.internal.storage.Storage;

import java.util.Map;

import androidx.annotation.NonNull;

public class ConsentVerifyTask
{
    private final String TAG = ConsentVerifyTask.class.getSimpleName();

    private final Storage storage;

    private final Api api;

    private RequestsState requestsState;

    public ConsentVerifyTask(@NonNull Storage storage, @NonNull Api api, RequestsState requestsState)
    {
        this.storage = storage;
        this.api = api;
        this.requestsState = requestsState;
    }

    public void run(Runnable finishCallback)
    {
        final Map<String, String> consents = storage.getRingConsents();

        if (consents == null || consents.isEmpty())
        {
            Log.w(TAG, "Fail verify consents. Consents are empty");
            requestsState.setVerifyState(VerifyState.FAILURE);
            finishCallback.run();
            return;
        }

        api.verify(consents, new VerifyCallback()
        {
            @Override
            public void onOutdated(String rawStatus)
            {
                storage.setConsentOutdated(true);
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                requestsState.setVerifyState(VerifyState.OUTDATED);
                finishCallback.run();
            }

            @Override
            public void onActual(String rawStatus)
            {
                storage.setConsentOutdated(false);
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                requestsState.setVerifyState(VerifyState.ACTUAL);
                finishCallback.run();
            }

            @Override
            public void onFailure(String status)
            {
                storage.saveLastAPIConsentsCheckStatus(status);
                requestsState.setVerifyState(VerifyState.FAILURE);
                finishCallback.run();
            }
        });
    }

}
