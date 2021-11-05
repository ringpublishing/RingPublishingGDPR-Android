package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.RingPublishingGDPROnErrorListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.model.TenantState;
import com.ringpublishing.gdpr.internal.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FetchConfigurationTask
{

    private final String TAG = FetchConfigurationTask.class.getCanonicalName();

    @NonNull
    private final Api api;

    @NonNull
    private final Storage storage;

    @NonNull
    private final RequestsState requestsState;

    @NonNull
    private final TenantConfiguration tenantConfiguration;

    @NonNull
    private final RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener;

    public FetchConfigurationTask(@NonNull Api api,
                                  @NonNull Storage storage,
                                  @NonNull RequestsState requestsState,
                                  @NonNull TenantConfiguration tenantConfiguration,
                                  @NonNull RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener)
    {
        this.api = api;
        this.storage = storage;
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.ringPublishingGDPROnErrorListener = ringPublishingGDPROnErrorListener;
    }

    public void run(Runnable finishCallback)
    {
        api.configuration(new ConfigurationCallback()
        {
            @Override
            public void onSuccess(@NonNull String url, boolean gdprApplies)
            {
                setTenantConfiguration(true, url, gdprApplies);
                finishCallback.run();
            }

            @Override
            public void onFailure()
            {
                setTenantConfiguration(false, null, false);
                Log.w(TAG, "Failure onConfigurationFailure");
                finishCallback.run();
                ringPublishingGDPROnErrorListener.onError(RingPublishingGDPROnErrorListener.ERROR_CODE_4, "Failure onConfigurationFailure");
            }
        });
    }

    private void setTenantConfiguration(boolean success, @Nullable String url, boolean gdprApplies)
    {
        storage.configureGDPRApplies(gdprApplies);
        requestsState.setTenantState(success ? TenantState.SUCCESS : TenantState.FAILURE);
        tenantConfiguration.setHost(url);
        tenantConfiguration.setGdprApplies(gdprApplies);
    }

}
