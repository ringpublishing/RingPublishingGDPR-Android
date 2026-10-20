package com.ringpublishing.gdpr.internal.task;

import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.storage.Storage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class FetchConfigurationTaskTest
{

    private Api api;

    private Storage storage;

    private RingPublishingGDPRListener listener;

    private RequestsState requestsState;

    private TenantConfiguration tenantConfiguration;

    private Runnable finishCallback;

    private FetchConfigurationTask task;

    @Before
    public void setUp()
    {
        api = Mockito.mock(Api.class);
        storage = Mockito.mock(Storage.class);
        listener = Mockito.mock(RingPublishingGDPRListener.class);
        finishCallback = Mockito.mock(Runnable.class);
        requestsState = new RequestsState();
        tenantConfiguration = new TenantConfiguration();
        task = new FetchConfigurationTask(api, storage, requestsState, tenantConfiguration, listener);
    }

    private ConfigurationCallback runAndCaptureCallback()
    {
        task.run(finishCallback);
        final ArgumentCaptor<ConfigurationCallback> captor = ArgumentCaptor.forClass(ConfigurationCallback.class);
        verify(api).configuration(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testRun_WhenFetchFails_ThenPersistedGdprAppliesKept()
    {
        runAndCaptureCallback().onFailure();

        verify(storage, never()).configureGDPRApplies(anyBoolean());
        assertTrue(requestsState.isFailure());
        assertFalse(tenantConfiguration.isGdprApplies());
        final org.mockito.InOrder order = inOrder(finishCallback, listener);
        order.verify(finishCallback).run();
        order.verify(listener).onError(Mockito.eq(RingPublishingGDPRError.CANNOT_FETCH_TENANT_CONFIGURATION), Mockito.anyString());
    }

    @Test
    public void testRun_WhenFetchSucceeds_ThenGdprAppliesPersistedAndHostSet()
    {
        runAndCaptureCallback().onSuccess("https://host", true);

        verify(storage).configureGDPRApplies(true);
        assertFalse(requestsState.isFailure());
        assertTrue(tenantConfiguration.isGdprApplies());
        assertEquals("https://host", tenantConfiguration.getHost());
        verify(finishCallback).run();
    }
}
