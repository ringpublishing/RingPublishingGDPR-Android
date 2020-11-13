package com.ringpublishing.gdpr;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;

import org.junit.Before;
import org.junit.Test;

import androidx.preference.PreferenceManager;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RingPublishingGDPRTest
{

    RingPublishingGDPR ringPublishingGDPR;

    @Before
    public void setUp()
    {
        ringPublishingGDPR = RingPublishingGDPR.getInstance();
    }

    private void initializeSdkWithConfig()
    {
        Application applicationContext = (Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        String tenantId = "tenantId";
        String brandName = "brandName";
        RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig = new RingPublishingGDPRUIConfig(Typeface.DEFAULT, Color.YELLOW);
        ringPublishingGDPR.initialize(applicationContext, tenantId, brandName, ringPublishingGDPRUIConfig);
        ringPublishingGDPR.clearConsentsData();
        clearSharedPrefs(applicationContext);
    }

    @Test
    public void initialize_WhenInitializeCalled_ThenIsInitialized()
    {
        assertFalse(ringPublishingGDPR.isInitialized());

        initializeSdkWithConfig();

        assertTrue(ringPublishingGDPR.isInitialized());
    }


    @Test
    public void shouldShowConsentForm_WhenFreshInitializeCalled_ThenShouldShowConsentsForm()
    {
        initializeSdkWithConfig();
        assertTrue(ringPublishingGDPR.shouldShowConsentForm());
    }

    private void clearSharedPrefs(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}