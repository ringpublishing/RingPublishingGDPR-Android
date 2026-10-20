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

import java.util.ArrayList;
import java.util.List;

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

    @NonNull
    private final List<Runnable> finishListeners = new ArrayList<>();

    private boolean running;

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

    public boolean isRunning()
    {
        synchronized (finishListeners)
        {
            return running;
        }
    }

    /**
     * Registers a one-shot listener run on the callback thread after the fetch finishes.
     *
     * @return false when no fetch is running, then the listener is not stored
     */
    public boolean addFinishListener(@NonNull Runnable listener)
    {
        synchronized (finishListeners)
        {
            if (!running)
            {
                return false;
            }
            finishListeners.add(listener);
            return true;
        }
    }

    public void removeFinishListener(@NonNull Runnable listener)
    {
        synchronized (finishListeners)
        {
            finishListeners.remove(listener);
        }
    }

    public void run(@NonNull Runnable finishCallback)
    {
        fetch(finishCallback);
    }

    /**
     * Fetches the configuration again without running the finish callback of {@link #run(Runnable)}. Waits for a fetch already running.
     */
    public void refetch()
    {
        fetch(null);
    }

    private void fetch(@Nullable Runnable finishCallback)
    {
        synchronized (finishListeners)
        {
            if (running)
            {
                return;
            }
            running = true;
        }

        api.configuration(new ConfigurationCallback()
        {
            @Override
            public void onSuccess(@NonNull String url, boolean gdprApplies)
            {
                try
                {
                    log.info( "Fetch api configuration task success");
                    storage.configureGDPRApplies(gdprApplies);
                    setTenantConfiguration(true, gdprApplies);
                    tenantConfiguration.setHost(url);
                    runFinishCallback(finishCallback);
                }
                finally
                {
                    finishRunning();
                }
            }

            @Override
            public void onFailure()
            {
                try
                {
                    setTenantConfiguration(false, false);
                    log.error( "Fetch api configuration task failure");
                    runFinishCallback(finishCallback);
                    ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_FETCH_TENANT_CONFIGURATION, "Fetch api configuration task failure");
                }
                finally
                {
                    finishRunning();
                }
            }
        });
    }

    private void runFinishCallback(@Nullable Runnable finishCallback)
    {
        if (finishCallback != null)
        {
            finishCallback.run();
        }
    }

    private void finishRunning()
    {
        final List<Runnable> toRun;
        synchronized (finishListeners)
        {
            running = false;
            toRun = new ArrayList<>(finishListeners);
            finishListeners.clear();
        }

        for (Runnable listener : toRun)
        {
            listener.run();
        }
    }

    private void setTenantConfiguration(boolean success, boolean gdprApplies)
    {
        requestsState.setTenantState(success ? TenantState.SUCCESS : TenantState.FAILURE);
        tenantConfiguration.setGdprApplies(gdprApplies);
    }

}
