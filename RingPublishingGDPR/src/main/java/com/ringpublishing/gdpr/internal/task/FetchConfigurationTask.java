package com.ringpublishing.gdpr.internal.task;

import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.model.TenantState;
import com.ringpublishing.gdpr.internal.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FetchConfigurationTask
{

    @NonNull
    private final Api api;

    @NonNull
    private final Storage storage;

    @NonNull
    private final RequestsState requestsState;

    @NonNull
    private final TenantConfiguration tenantConfiguration;

    @NonNull
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    private final Logger log = Logger.get();

    public FetchConfigurationTask(@NonNull Api api,
                                  @NonNull Storage storage,
                                  @NonNull RequestsState requestsState,
                                  @NonNull TenantConfiguration tenantConfiguration,
                                  @NonNull RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        this.api = api;
        this.storage = storage;
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.ringPublishingGDPRListener = ringPublishingGDPRListener;
    }

    public void run(Runnable finishCallback)
    {
        api.configuration(new ConfigurationCallback()
        {
            @Override
            public void onSuccess(@NonNull String url, boolean gdprApplies)
            {
                log.info( "Fetch api configuration task success");
                setTenantConfiguration(true, gdprApplies);
                tenantConfiguration.setHost(url);
                finishCallback.run();
            }

            @Override
            public void onFailure()
            {
                setTenantConfiguration(false, false);
                log.error( "Fetch api configuration task failure");
                finishCallback.run();
                ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_FETCH_TENANT_CONFIGURATION, "Fetch api configuration task failure");
            }
        });
    }

    private void setTenantConfiguration(boolean success, boolean gdprApplies)
    {
        storage.configureGDPRApplies(gdprApplies);
        requestsState.setTenantState(success ? TenantState.SUCCESS : TenantState.FAILURE);
        tenantConfiguration.setGdprApplies(gdprApplies);
    }

}
