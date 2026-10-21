package com.ringpublishing.gdpr;

import android.app.Activity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class RingPublishingGDPRActivityTest
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
    public void testOnCreate_WhenSdkNotInitialized_ThenCancelsAndFinishes()
    {
        final RingPublishingGDPRActivity activity = Robolectric.buildActivity(RingPublishingGDPRActivity.class).create().get();

        assertTrue(activity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).getResultCode());
    }

    @Test
    public void testOnDestroy_WhenSdkNotInitialized_ThenDoesNotThrow()
    {
        Robolectric.buildActivity(RingPublishingGDPRActivity.class).create().destroy();
    }
}
