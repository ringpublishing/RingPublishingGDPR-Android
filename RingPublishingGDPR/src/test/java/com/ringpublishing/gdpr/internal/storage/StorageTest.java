package com.ringpublishing.gdpr.internal.storage;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class StorageTest
{

    private static final String CMP_SDK_ID_KEY = "IABTCF_CmpSdkID";

    private static final String GDPR_APPLIES_KEY = "IABTCF_gdprApplies";

    private Storage storage;

    @Before
    public void setUp()
    {
        final Context context = ApplicationProvider.getApplicationContext();
        storage = new Storage(context);
    }

    @Test
    public void testConfigureCmpSdkId_WhenCalled_ThenCmpIdWrittenWithoutGdprApplies()
    {
        storage.configureCmpSdkId();

        assertEquals(GDPRConstants.CMP_SDK_ID, storage.getInt(CMP_SDK_ID_KEY));
        assertFalse(storage.containsInt(GDPR_APPLIES_KEY));
    }

    @Test
    public void testConfigureGDPRApplies_WhenTrue_ThenOneWrittenAndCmpIdUntouched()
    {
        storage.configureGDPRApplies(true);

        assertEquals(1, storage.getInt(GDPR_APPLIES_KEY));
        assertFalse(storage.containsInt(CMP_SDK_ID_KEY));
    }

    @Test
    public void testConfigureGDPRApplies_WhenFalse_ThenZeroWrittenAndCmpIdUntouched()
    {
        storage.configureGDPRApplies(false);

        assertEquals(0, storage.getInt(GDPR_APPLIES_KEY));
        assertFalse(storage.containsInt(CMP_SDK_ID_KEY));
        assertFalse(storage.isGDPRApplies());
    }

    @Test
    public void testClearAllConsentData_WhenCmpIdConfigured_ThenCmpIdKept()
    {
        storage.configureCmpSdkId();
        storage.configureGDPRApplies(true);

        storage.clearAllConsentData();

        assertTrue(storage.containsInt(CMP_SDK_ID_KEY));
        assertEquals(GDPRConstants.CMP_SDK_ID, storage.getInt(CMP_SDK_ID_KEY));
    }
}
