package com.ringpublishing.gdpr.internal.task;

import android.util.Log;

import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.model.TenantState;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormViewController;

public class FetchConfigurationTask
{

    private final String TAG = FetchConfigurationTask.class.getSimpleName();

    private final Api api;

    private final Storage storage;

    private final RequestsState requestsState;

    private final TenantConfiguration tenantConfiguration;

    private final FormViewController formViewController;

    public FetchConfigurationTask(Api api, Storage storage, RequestsState requestsState, TenantConfiguration tenantConfiguration, FormViewController formViewController)
    {
        this.api = api;
        this.storage = storage;
        this.requestsState = requestsState;
        this.tenantConfiguration = tenantConfiguration;
        this.formViewController = formViewController;
    }

    public void run(Runnable finishCallback)
    {
        api.configuration(new ConfigurationCallback()
        {
            @Override
            public void onSuccess(String url, boolean gdprApplies)
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
            }
        });
    }

    private void setTenantConfiguration(boolean success, String url, boolean gdprApplies)
    {
        storage.configureGDPRApplies(gdprApplies);
        requestsState.setTenantState(success ? TenantState.SUCCESS : TenantState.FAILURE);
        tenantConfiguration.setHost(url);
        tenantConfiguration.setGdprApplies(gdprApplies);
        formViewController.setTenantConfiguration(tenantConfiguration);
    }

}
