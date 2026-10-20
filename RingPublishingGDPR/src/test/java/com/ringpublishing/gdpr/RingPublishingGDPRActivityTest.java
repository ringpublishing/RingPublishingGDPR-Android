package com.ringpublishing.gdpr;

import android.app.Activity;

import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RingPublishingGDPRActivityTest
{

    private RingPublishingGDPRActivity createActivity()
    {
        final RingPublishingGDPRActivity activity = Mockito.mock(RingPublishingGDPRActivity.class);
        Mockito.doCallRealMethod().when(activity).finishIfNotInitialized(Mockito.any());
        return activity;
    }

    @Test
    public void testFinishIfNotInitialized_WhenNotInitialized_ThenCancelsAndFinishes()
    {
        final RingPublishingGDPRActivity activity = createActivity();
        final RingPublishingGDPR gdpr = Mockito.mock(RingPublishingGDPR.class);
        when(gdpr.isInitialized()).thenReturn(false);

        assertTrue(activity.finishIfNotInitialized(gdpr));

        verify(activity).setResult(Activity.RESULT_CANCELED);
        verify(activity).finish();
    }

    @Test
    public void testFinishIfNotInitialized_WhenInitialized_ThenDoesNothing()
    {
        final RingPublishingGDPRActivity activity = createActivity();
        final RingPublishingGDPR gdpr = Mockito.mock(RingPublishingGDPR.class);
        when(gdpr.isInitialized()).thenReturn(true);

        assertFalse(activity.finishIfNotInitialized(gdpr));

        verify(activity, never()).setResult(Mockito.anyInt());
        verify(activity, never()).finish();
    }

    @Test
    public void testHandleIntent_WhenFormViewNotCreated_ThenDoesNotThrow() throws Exception
    {
        invokePrivate(createActivity(), "handleIntent");
    }

    @Test
    public void testClearLayout_WhenLayoutNotCreated_ThenDoesNotThrow() throws Exception
    {
        invokePrivate(createActivity(), "clearLayout");
    }

    private void invokePrivate(RingPublishingGDPRActivity activity, String name) throws Exception
    {
        final Method method = RingPublishingGDPRActivity.class.getDeclaredMethod(name);
        method.setAccessible(true);
        method.invoke(activity);
    }
}
