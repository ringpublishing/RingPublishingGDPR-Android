package com.ringpublishing.gdpr.internal.storage;

import android.content.Context;

import com.ringpublishing.gdpr.internal.TestAndroidResources;
import com.ringpublishing.gdpr.internal.storage.Storage.Consent;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StorageTest
{

    private Context context;

    private Storage storage;

    @Before
    public void setup()
    {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        storage = new Storage(context);
        storage.clearAllConsentData();
    }

    @Test
    public void didAskUserForConsents_whenTcSaved_ThenHaveValue()
    {
        boolean didAskUserForConsents = storage.didAskUserForConsents();
        assertFalse(didAskUserForConsents);
        storage.setString(Consent.IABTCF_TCString.name(), "tc");
        boolean didAskUserForConsentsAfterSave = storage.didAskUserForConsents();
        assertTrue(didAskUserForConsentsAfterSave);
    }

    @Test
    public void isOutdated_WhenhaveFlagInPrefrences_ThenisOutdated()
    {
        boolean outdated = storage.isOutdated();
        assertFalse(outdated);
        storage.setBoolean("consentOutdated", true);
        boolean outdatedAfterSave = storage.isOutdated();
        assertTrue(outdatedAfterSave);
    }

    @Test
    public void setOutdated_WhensetOutdated_ThenhaveCorrectValue()
    {
        boolean outdated = storage.getBoolean("consentOutdated", false);
        assertFalse(outdated);
        storage.setOutdated(true);
        boolean outdatedAfterSave = storage.isOutdated();
        assertTrue(outdatedAfterSave);
    }

    @Test
    public void saveTCData_WhenAllData_ThenSaveAll()
    {
        storage.clearAllConsentData();
        String tcData = TestAndroidResources.getTcData(getClass());
        assertTrue(tcData.length() > 0);
        try
        {
            storage.saveTCData(tcData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        assertEquals(1, storage.getInt("IABTCF_CmpSdkID"));
        assertEquals(2, storage.getInt("IABTCF_CmpSdkVersion"));
        assertEquals(4, storage.getInt("IABTCF_PolicyVersion"));
        assertEquals(3, storage.getInt("IABTCF_gdprApplies"));
        assertEquals(7, storage.getInt("IABTCF_PurposeOneTreatment"));
        assertEquals(6, storage.getInt("IABTCF_UseNonStandardStacks"));

        assertEquals("PL", storage.getString("IABTCF_PublisherCC"));
        assertEquals(TestAndroidResources.TC_STRING, storage.getString("IABTCF_TCString"));
        assertEquals(TestAndroidResources.TC_VENDOR_CONSENTS, storage.getString("IABTCF_VendorConsents"));
        assertEquals(TestAndroidResources.TC_LEGITIMATE_INTEREST, storage.getString("IABTCF_VendorLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_PURPOSE_CONSENTS, storage.getString("IABTCF_PurposeConsents"));
        assertEquals(TestAndroidResources.TC_PURPOSE_LEGITIMATE_INTEREST, storage.getString("IABTCF_PurposeLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_SPECIAL_FEATURE_OPTIONS, storage.getString("IABTCF_SpecialFeaturesOptIns"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CONSENTS, storage.getString("IABTCF_PublisherConsent"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_CONSENTS, storage.getString("IABTCF_PublisherCustomPurposesConsents"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherCustomPurposesLegitimateInterests"));

        assertEquals(TestAndroidResources.TC_ADDTL, storage.getString("IABTCF_AddtlConsent"));
    }

    @Test
    public void saveTCData_WhenNoPurpose_ThenSaveOthersCorrectly()
    {
        storage.clearAllConsentData();
        String tcData = TestAndroidResources.getTcDataWithoutPurpose(getClass());
        assertTrue(tcData.length() > 0);
        try
        {
            storage.saveTCData(tcData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        assertEquals(1, storage.getInt("IABTCF_CmpSdkID"));
        assertEquals(2, storage.getInt("IABTCF_CmpSdkVersion"));
        assertEquals(4, storage.getInt("IABTCF_PolicyVersion"));
        assertEquals(3, storage.getInt("IABTCF_gdprApplies"));
        assertEquals(7, storage.getInt("IABTCF_PurposeOneTreatment"));
        assertEquals(6, storage.getInt("IABTCF_UseNonStandardStacks"));

        assertEquals("PL", storage.getString("IABTCF_PublisherCC"));
        assertEquals(TestAndroidResources.TC_STRING, storage.getString("IABTCF_TCString"));
        assertEquals(TestAndroidResources.TC_VENDOR_CONSENTS, storage.getString("IABTCF_VendorConsents"));
        assertEquals(TestAndroidResources.TC_LEGITIMATE_INTEREST, storage.getString("IABTCF_VendorLegitimateInterests"));
        //moved from here
        assertEquals(TestAndroidResources.TC_SPECIAL_FEATURE_OPTIONS, storage.getString("IABTCF_SpecialFeaturesOptIns"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CONSENTS, storage.getString("IABTCF_PublisherConsent"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_CONSENTS, storage.getString("IABTCF_PublisherCustomPurposesConsents"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherCustomPurposesLegitimateInterests"));

        assertEquals(TestAndroidResources.TC_ADDTL, storage.getString("IABTCF_AddtlConsent"));

        // to here
        assertEquals("", storage.getString("IABTCF_PurposeConsents"));
        assertEquals("", storage.getString("IABTCF_PurposeLegitimateInterests"));
    }

    @Test
    public void saveTCData_WhenNoGdpr_ThenSaveOthersCorrectly()
    {
        storage.clearAllConsentData();
        String tcData = TestAndroidResources.getTcDataWithoutGdpr(getClass());
        assertTrue(tcData.length() > 0);
        try
        {
            storage.saveTCData(tcData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        assertEquals(1, storage.getInt("IABTCF_CmpSdkID"));
        assertEquals(2, storage.getInt("IABTCF_CmpSdkVersion"));
        assertEquals(4, storage.getInt("IABTCF_PolicyVersion"));
        // move from here
        assertEquals(7, storage.getInt("IABTCF_PurposeOneTreatment"));
        assertEquals(6, storage.getInt("IABTCF_UseNonStandardStacks"));

        assertEquals("PL", storage.getString("IABTCF_PublisherCC"));
        assertEquals(TestAndroidResources.TC_STRING, storage.getString("IABTCF_TCString"));
        assertEquals(TestAndroidResources.TC_VENDOR_CONSENTS, storage.getString("IABTCF_VendorConsents"));
        assertEquals(TestAndroidResources.TC_LEGITIMATE_INTEREST, storage.getString("IABTCF_VendorLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_PURPOSE_CONSENTS, storage.getString("IABTCF_PurposeConsents"));
        assertEquals(TestAndroidResources.TC_PURPOSE_LEGITIMATE_INTEREST, storage.getString("IABTCF_PurposeLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_SPECIAL_FEATURE_OPTIONS, storage.getString("IABTCF_SpecialFeaturesOptIns"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CONSENTS, storage.getString("IABTCF_PublisherConsent"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherLegitimateInterests"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_CONSENTS, storage.getString("IABTCF_PublisherCustomPurposesConsents"));
        assertEquals(TestAndroidResources.TC_PUBLISHER_CUSTOM_PURPOSE_LEGITIMATE_INTEREST, storage.getString("IABTCF_PublisherCustomPurposesLegitimateInterests"));

        assertEquals(TestAndroidResources.TC_ADDTL, storage.getString("IABTCF_AddtlConsent"));

        // to here

        assertEquals(0, storage.getInt("IABTCF_gdprApplies"));
    }

    @Test
    public void saveConsentData_WhenhaveCorrectData_ThenSaveAll()
    {
        storage.clearAllConsentData();
        String dlData = TestAndroidResources.getDlData(getClass());
        assertTrue(dlData.length() > 0);
        try
        {
            storage.saveConsentData(dlData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        assertEquals(TestAndroidResources.DL_CONSENTS, storage.getString("RingPublishing_Consents"));
        assertEquals(1, storage.getInt("RingPublishing_VendorsConsent"));
    }


    @Test
    public void saveConsentData_WhenhaveNoConsents_ThenSaveOthers()
    {
        storage.clearAllConsentData();
        String dlData = TestAndroidResources.getDlDataNoConsents(getClass());
        assertTrue(dlData.length() > 0);
        try
        {
            storage.saveConsentData(dlData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        assertEquals(1, storage.getInt("RingPublishing_VendorsConsent"));

        assertEquals("", storage.getString("RingPublishing_Consents"));
    }

    @Test
    public void saveLastAPIConsentsCheckStatus_WhenSaveToStorage_ThenHaveCorrectValue()
    {
        storage.saveLastAPIConsentsCheckStatus("status");
        assertEquals("status", storage.getString("RingPublishing_LastAPIConsentsCheckStatus"));
    }

    @Test
    public void getLastAPIConsentsCheckStatus_whenSaveToStorage_ThenReadCorrectValue()
    {
        storage.saveLastAPIConsentsCheckStatus("status");
        assertEquals("status", storage.getLastAPIConsentsCheckStatus());
    }

    @Test
    public void clearAllConsentData_WhenclearSavedConsents_ThenreadEmptyResult()
    {
        String dlData = TestAndroidResources.getDlData(getClass());
        assertTrue(dlData.length() > 0);
        try
        {
            storage.saveConsentData(dlData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }

        storage.clearAllConsentData();
        Map<String, String> ringConsents = storage.getRingConsents();
        assertTrue(ringConsents.isEmpty());
    }

    @Test
    public void clearAllPublisherRestrictions_WhenClearSavedRestrictions_ThenNoSavedRestrictions()
    {
        String tcData = TestAndroidResources.getTcDataWithRestrictions(getClass());
        assertTrue(tcData.length() > 0);
        try
        {
            storage.saveTCData(tcData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }

        assertEquals("0", storage.getString("IABTCF_PublisherRestrictions0"));
        assertEquals("1", storage.getString("IABTCF_PublisherRestrictions1"));
        assertEquals("2", storage.getString("IABTCF_PublisherRestrictions2"));

        storage.clearAllPublisherRestrictions();

        assertEquals("", storage.getString("IABTCF_PublisherRestrictions0"));
        assertEquals("", storage.getString("IABTCF_PublisherRestrictions1"));
        assertEquals("", storage.getString("IABTCF_PublisherRestrictions2"));

    }

    @Test
    public void getRingConsents_WhenSaveConsentsData_ThenReadConsentsHaveCorrectValues()
    {
        storage.clearAllConsentData();
        String dlData = TestAndroidResources.getDlData(getClass());
        assertTrue(dlData.length() > 0);
        try
        {
            storage.saveConsentData(dlData);
        }
        catch (JSONException e)
        {
            assertNull(e);
        }
        Map<String, String> ringConsents = storage.getRingConsents();
        assertEquals(2, ringConsents.size());
        assertEquals(TestAndroidResources.DL_CONSENTS_LPUB, ringConsents.get("lpubconsent"));
        assertEquals(TestAndroidResources.DL_CONSENTS_LADP, ringConsents.get("ladpconsent"));
    }
}