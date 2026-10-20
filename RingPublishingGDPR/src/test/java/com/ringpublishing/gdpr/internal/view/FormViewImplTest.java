package com.ringpublishing.gdpr.internal.view;

import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.ringpublishing.gdpr.R;
import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.ConfigurationCallback;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.network.Network;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.task.FetchConfigurationTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import java.time.Duration;

import androidx.appcompat.app.AppCompatActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class FormViewImplTest
{

    private static final String HOST = "https://cmp.example.com";

    private Api api;

    private RingPublishingGDPRListener listener;

    private TenantConfiguration tenantConfiguration;

    private FetchConfigurationTask fetchTask;

    private AppCompatActivity activity;

    private FormViewImpl formView;

    @Before
    public void setUp()
    {
        api = Mockito.mock(Api.class);
        final Network network = Mockito.mock(Network.class);
        when(network.createUserAgentHeader()).thenReturn("agent");
        when(api.getNetwork()).thenReturn(network);
        listener = Mockito.mock(RingPublishingGDPRListener.class);
        tenantConfiguration = new TenantConfiguration();
        fetchTask = Mockito.spy(new FetchConfigurationTask(api, Mockito.mock(Storage.class), new RequestsState(), tenantConfiguration, listener));

        final ActivityController<AppCompatActivity> controller = Robolectric.buildActivity(AppCompatActivity.class);
        activity = controller.get();
        activity.setTheme(androidx.appcompat.R.style.Theme_AppCompat);
        controller.create().start().resume().visible();
        formView = new FormViewImpl(activity, api, tenantConfiguration, Mockito.mock(Storage.class), listener, fetchTask);
    }

    private void attach()
    {
        activity.setContentView(formView);
    }

    private void detach()
    {
        ((ViewGroup) formView.getParent()).removeView(formView);
    }

    private void idleMainLooper()
    {
        shadowOf(Looper.getMainLooper()).idle();
    }

    private String loadedUrl()
    {
        final WebView webView = formView.findViewById(R.id.webview);
        return shadowOf(webView).getLastLoadedUrl();
    }

    private ConfigurationCallback captureCallback(int apiCalls)
    {
        final ArgumentCaptor<ConfigurationCallback> captor = ArgumentCaptor.forClass(ConfigurationCallback.class);
        verify(api, times(apiCalls)).configuration(captor.capture());
        return captor.getValue();
    }

    @Test
    public void testAttach_WhenHostPresent_ThenLoadsHostWithoutRefetchOrListener()
    {
        tenantConfiguration.setHost(HOST);
        tenantConfiguration.setAdditionalQueryParameters(java.util.Collections.singletonMap("a", "b"));

        attach();
        idleMainLooper();

        assertEquals(tenantConfiguration.getHostWithAdditionalQuery(), loadedUrl());
        assertTrue(loadedUrl().contains("a=b"));
        verify(api, never()).configuration(Mockito.any());
        assertFalse(fetchTask.isRunning());
    }

    @Test
    public void testAttach_WhenHostNullAndFetchRunning_ThenWaitsAndLoadsAfterSuccess()
    {
        fetchTask.run(() -> { });
        final ConfigurationCallback callback = captureCallback(1);

        attach();
        idleMainLooper();

        verify(api, times(1)).configuration(Mockito.any());
        assertNull(loadedUrl());

        callback.onSuccess(HOST, true);
        idleMainLooper();

        assertEquals(HOST, loadedUrl());
        verify(listener, never()).onError(eq(RingPublishingGDPRError.WEBVIEW_MISSING_HOST), anyString());
    }

    @Test
    public void testAttach_WhenHostNullAndPreviousFetchFailed_ThenRefetchesAndLoadsOnSuccess()
    {
        fetchTask.run(() -> { });
        captureCallback(1).onFailure();

        attach();
        final ConfigurationCallback callback = captureCallback(2);
        assertNull(loadedUrl());

        callback.onSuccess(HOST, true);
        idleMainLooper();

        assertEquals(HOST, loadedUrl());
        verify(api, times(2)).configuration(Mockito.any());
        verify(listener, never()).onError(eq(RingPublishingGDPRError.WEBVIEW_MISSING_HOST), anyString());
    }

    @Test
    public void testAttach_WhenHostNullAndRefetchFails_ThenFailsOnceWithMissingHostAndNoRefetchLoop()
    {
        fetchTask.run(() -> { });
        captureCallback(1).onFailure();

        attach();
        captureCallback(2).onFailure();
        idleMainLooper();

        assertNull(loadedUrl());
        verify(api, times(2)).configuration(Mockito.any());
        verify(listener, times(1)).onError(eq(RingPublishingGDPRError.WEBVIEW_MISSING_HOST), anyString());
        verify(listener, times(1)).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
    }

    @Test
    public void testDetach_WhenWaitingForFetch_ThenLaterSuccessDoesNotLoadUrl()
    {
        fetchTask.run(() -> { });
        final ConfigurationCallback callback = captureCallback(1);
        attach();
        idleMainLooper();

        detach();
        callback.onSuccess(HOST, true);
        idleMainLooper();

        assertNull(loadedUrl());
        verify(listener, never()).onError(eq(RingPublishingGDPRError.WEBVIEW_MISSING_HOST), anyString());
    }

    @Test
    public void testTimeout_WhenWaitingForFetch_ThenListenerRemovedAndLaterSuccessDoesNotLoadUrl()
    {
        fetchTask.run(() -> { });
        final ConfigurationCallback callback = captureCallback(1);
        attach();
        idleMainLooper();

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(11));
        verify(listener, times(1)).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
        callback.onSuccess(HOST, true);
        idleMainLooper();

        assertNull(loadedUrl());
        verify(listener, never()).onError(eq(RingPublishingGDPRError.WEBVIEW_MISSING_HOST), anyString());
    }

    @Test
    public void testTimeout_WhenConfigArrivesDuringWaitAndPageNeverLoads_ThenClosesOnceFromFirstLoadAttempt()
    {
        fetchTask.run(() -> { });
        final ConfigurationCallback callback = captureCallback(1);
        attach();
        idleMainLooper();

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6));
        callback.onSuccess(HOST, true);
        idleMainLooper();
        assertEquals(HOST, loadedUrl());

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(5));

        verify(listener, times(1)).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
    }

    @Test
    public void testRetry_WhenWaitingForConfig_ThenRestartsTimeoutFromRetry()
    {
        fetchTask.run(() -> { });
        attach();
        idleMainLooper();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6));

        formView.onRetryClicked();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(5));
        verify(listener, never()).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6));
        verify(listener, times(1)).onError(eq(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR), anyString());
    }

    @Test
    public void testRetry_WhenStillWaiting_ThenOnlyLatestListenerStaysRegistered()
    {
        fetchTask.run(() -> { });
        final ConfigurationCallback callback = captureCallback(1);
        attach();
        idleMainLooper();

        formView.onRetryClicked();
        formView.onRetryClicked();

        final ArgumentCaptor<Runnable> added = ArgumentCaptor.forClass(Runnable.class);
        verify(fetchTask, times(3)).addFinishListener(added.capture());
        final ArgumentCaptor<Runnable> removed = ArgumentCaptor.forClass(Runnable.class);
        verify(fetchTask, times(2)).removeFinishListener(removed.capture());
        assertEquals(added.getAllValues().subList(0, 2), removed.getAllValues());

        callback.onSuccess(HOST, true);
        idleMainLooper();

        assertEquals(HOST, loadedUrl());
        verify(api, times(1)).configuration(Mockito.any());
    }
}
