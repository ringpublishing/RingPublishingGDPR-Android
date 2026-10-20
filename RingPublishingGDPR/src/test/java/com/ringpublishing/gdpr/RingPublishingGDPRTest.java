package com.ringpublishing.gdpr;

import android.app.Application;
import android.content.Context;

import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.task.ApiSynchronizationTask;
import com.ringpublishing.gdpr.internal.task.ConsentVerifyTask;
import com.ringpublishing.gdpr.internal.task.FetchConfigurationTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RingPublishingGDPRTest
{

    @Before
    @After
    public void resetSingleton() throws Exception
    {
        final Field instance = RingPublishingGDPR.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void testInitialize_WhenCalledTwice_ThenApiCreatedOnce()
    {
        final Application application = Mockito.mock(Application.class);
        Mockito.when(application.getApplicationContext()).thenReturn(Mockito.mock(Context.class));
        final RingPublishingGDPRUIConfig uiConfig = Mockito.mock(RingPublishingGDPRUIConfig.class);

        try (MockedConstruction<Api> apis = Mockito.mockConstruction(Api.class);
             MockedConstruction<Storage> storages = Mockito.mockConstruction(Storage.class);
             MockedConstruction<FetchConfigurationTask> fetchTasks = Mockito.mockConstruction(FetchConfigurationTask.class);
             MockedConstruction<ApiSynchronizationTask> syncTasks = Mockito.mockConstruction(ApiSynchronizationTask.class);
             MockedConstruction<ConsentVerifyTask> verifyTasks = Mockito.mockConstruction(ConsentVerifyTask.class))
        {
            final RingPublishingGDPR gdpr = RingPublishingGDPR.getInstance();

            gdpr.initialize(application, "1", "brand", uiConfig);
            gdpr.initialize(application, "1", "brand", uiConfig);

            assertTrue(gdpr.isInitialized());
            assertEquals(1, apis.constructed().size());
            assertEquals(1, storages.constructed().size());
        }
    }

    @Test
    public void testInitializedField_WhenInspected_ThenVolatile() throws Exception
    {
        final Field initialized = RingPublishingGDPR.class.getDeclaredField("initialized");

        assertTrue(Modifier.isVolatile(initialized.getModifiers()));
    }

    @Test
    public void testInitializeInternal_WhenInspected_ThenSynchronized() throws Exception
    {
        assertTrue(Modifier.isSynchronized(initializeInternalMethod().getModifiers()));
    }

    @Test
    public void testInitializeWithParameters_WhenInspected_ThenSynchronized() throws Exception
    {
        assertTrue(Modifier.isSynchronized(RingPublishingGDPR.class
                .getMethod("initialize", Application.class, String.class, String.class, RingPublishingGDPRUIConfig.class, Map.class).getModifiers()));
        assertTrue(Modifier.isSynchronized(RingPublishingGDPR.class
                .getMethod("initialize", Application.class, String.class, String.class, RingPublishingGDPRUIConfig.class, boolean.class, Map.class)
                .getModifiers()));
    }

    private Method initializeInternalMethod() throws Exception
    {
        return RingPublishingGDPR.class.getDeclaredMethod("initializeInternal", Application.class, String.class, String.class,
                RingPublishingGDPRUIConfig.class, Boolean.class);
    }

    @Test
    public void testSetAdditionalQueryParameters_WhenCalledBeforeInitialize_ThenStoresCopyAndReplacesPrevious() throws Exception
    {
        final RingPublishingGDPR gdpr = RingPublishingGDPR.getInstance();
        final Map<String, String> first = new HashMap<>();
        first.put("a", "1");

        gdpr.setAdditionalQueryParameters(first);
        first.put("b", "2");

        assertEquals(Collections.singletonMap("a", "1"), storedParameters(gdpr));

        gdpr.setAdditionalQueryParameters(Collections.singletonMap("c", "3"));

        assertEquals(Collections.singletonMap("c", "3"), storedParameters(gdpr));
    }

    @Test
    public void testSetAdditionalQueryParameters_WhenInspected_ThenSynchronized() throws Exception
    {
        assertTrue(Modifier.isSynchronized(RingPublishingGDPR.class.getMethod("setAdditionalQueryParameters", Map.class).getModifiers()));
    }

    private Object storedParameters(RingPublishingGDPR gdpr) throws Exception
    {
        final Field tenantField = RingPublishingGDPR.class.getDeclaredField("tenantConfiguration");
        tenantField.setAccessible(true);
        final Object tenantConfiguration = tenantField.get(gdpr);
        final Field parametersField = tenantConfiguration.getClass().getDeclaredField("additionalQueryParameters");
        parametersField.setAccessible(true);
        return parametersField.get(tenantConfiguration);
    }
}
