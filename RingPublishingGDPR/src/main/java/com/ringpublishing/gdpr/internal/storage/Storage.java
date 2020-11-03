package com.ringpublishing.gdpr.internal.storage;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ringpublishing.gdpr.internal.android.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Storage extends Preferences
{

    private static final String TAG = Storage.class.getSimpleName();

    private static final String KEY_PREFIX_RING_PUBLISHING = "RingPublishing_";

    private static final String KEY_PREFIX_IABTCF = "IABTCF_";

    private static final String KEY_PREFIX_PUBLISHER_RESTRICTIONS = "IABTCF_PublisherRestrictions";

    private static final String RING_PUBLISHING_LAST_API_CONSENTS_CHECK_STATUS = "RingPublishing_LastAPIConsentsCheckStatus";

    private static final String KEY_OUTDATED = "consentOutdated";

    private final Gson gson = new Gson();

    public Storage(final Context context)
    {
        super(context);
    }

    public boolean didAskUserForConsents()
    {
        return !TextUtils.isEmpty(getString(Consent.IABTCF_TCString));
    }

    public boolean isOutdated()
    {
        return getBoolean(KEY_OUTDATED, false);
    }

    public void setOutdated(final boolean isOutdated)
    {
        setBoolean(KEY_OUTDATED, isOutdated);
    }

    public void saveTCData(final String tcData) throws JSONException
    {
        final JSONObject tcDataJson = new JSONObject(tcData);
        saveConsentData(tcDataJson);
        savePublisherRestrictions(tcDataJson);
    }

    private void saveConsentData(JSONObject json) throws JSONException
    {
        for (Consent consent : Consent.values())
        {
            final JSONObject jsonNested = StorageJsonParser.getNestedJSONObject(consent.jsonKey, json);

            if (jsonNested != null && jsonNested.has(consent.getNestedJsonKey()))
            {
                switch (consent.type)
                {
                    case NUMBER:
                    {
                        setInt(consent.key, jsonNested.getInt(consent.getNestedJsonKey()));
                        break;
                    }
                    case STRING:
                    {
                        setString(consent.key, jsonNested.getString(consent.getNestedJsonKey()));
                        break;
                    }
                }
            }
            else
            {
                remove(consent.key);
                Log.i(TAG, "Missing consent for field: " + consent.key);
            }
        }
    }

    private void savePublisherRestrictions(final JSONObject tcDataJson) throws JSONException
    {
        clearAllPublisherRestrictions();

        JSONObject publisher = StorageJsonParser.getNestedJSONObject("publisher.restrictions", tcDataJson);

        if (publisher == null)
        {
            Log.i(TAG, "No publisher restrictions to save");
            return;
        }

        JSONObject restrictions = publisher.getJSONObject("restrictions");

        final Iterator<String> keys = restrictions.keys();
        while (keys.hasNext())
        {
            final String key = keys.next();
            savePublisherRestriction(key, restrictions.get(key));
        }
    }

    private void savePublisherRestriction(final String key, final Object value)
    {
        setObject(KEY_PREFIX_PUBLISHER_RESTRICTIONS + key, value);
    }

    public void saveConsentData(final String dlData) throws JSONException
    {
        JSONObject json = new JSONObject(dlData);

        if (json == null)
        {
            Log.w(TAG, "No dlData to save");
        }
        // Ring
        for (ConsentRing key : ConsentRing.values())
        {
            if (json.has(key.jsonKey))
            {
                setObject(key.key, json.get(key.jsonKey));
            }
            else
            {
                Log.w(TAG, "Missing ring consent for field: " + key.key);
            }
        }
    }

    public void saveLastAPIConsentsCheckStatus(final String status)
    {
        setString(RING_PUBLISHING_LAST_API_CONSENTS_CHECK_STATUS, status);
    }

    public String getLastAPIConsentsCheckStatus()
    {
        return getString(RING_PUBLISHING_LAST_API_CONSENTS_CHECK_STATUS);
    }

    public void clearAllConsentData()
    {
        removeAllByPrefix(KEY_PREFIX_RING_PUBLISHING);
        removeAllByPrefix(KEY_PREFIX_IABTCF);
    }

    void clearAllPublisherRestrictions()
    {
        removeAllByPrefix(KEY_PREFIX_PUBLISHER_RESTRICTIONS);
    }

    public String getString(Consent keyString)
    {
        return getString(keyString.key);
    }

    public String getString(ConsentRing keyString)
    {
        return getString(keyString.key);
    }

    public Map<String, String> getRingConsents()
    {
        Map<String, String> consents = new HashMap<>();

        final String consentsString = getString(ConsentRing.RingPublishing_Consents);

        if (TextUtils.isEmpty(consentsString))
        {
            return consents;
        }

        try
        {
            consents = gson.fromJson(consentsString, consents.getClass());
        }
        catch (JsonSyntaxException e)
        {
            Log.e(TAG, "Fail to reade saved ring consents to verify method", e);
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, "Fail to reade saved ring consents to verify method", e);
        }

        return consents;
    }


    enum Type
    {
        NUMBER,
        STRING
    }

    enum Consent
    {
        // Standardized consents Numbers
        IABTCF_CmpSdkID("IABTCF_CmpSdkID", "cmpId", Type.NUMBER),
        IABTCF_CmpSdkVersion("IABTCF_CmpSdkVersion", "cmpVersion", Type.NUMBER),
        IABTCF_PolicyVersion("IABTCF_PolicyVersion", "tcfPolicyVersion", Type.NUMBER),
        IABTCF_gdprApplies("IABTCF_gdprApplies", "gdprApplies", Type.NUMBER),
        IABTCF_PurposeOneTreatment("IABTCF_PurposeOneTreatment", "purposeOneTreatment", Type.NUMBER),
        IABTCF_UseNonStandardStacks("IABTCF_UseNonStandardStacks", "useNonStandardStacks", Type.NUMBER),

        // Standardized consents String
        IABTCF_PublisherCC("IABTCF_PublisherCC", "publisherCC", Type.STRING),
        IABTCF_TCString("IABTCF_TCString", "tcString", Type.STRING),
        IABTCF_VendorConsents("IABTCF_VendorConsents", "vendor.consents", Type.STRING),
        IABTCF_VendorLegitimateInterests("IABTCF_VendorLegitimateInterests", "vendor.legitimateInterests", Type.STRING),
        IABTCF_PurposeConsents("IABTCF_PurposeConsents", "purpose.consents", Type.STRING),
        IABTCF_PurposeLegitimateInterests("IABTCF_PurposeLegitimateInterests", "purpose.legitimateInterests", Type.STRING),
        IABTCF_SpecialFeaturesOptIns("IABTCF_SpecialFeaturesOptIns", "specialFeatureOptins", Type.STRING),
        IABTCF_PublisherConsent("IABTCF_PublisherConsent", "publisher.consents", Type.STRING),
        IABTCF_PublisherLegitimateInterests("IABTCF_PublisherLegitimateInterests", "publisher.legitimateInterests", Type.STRING),
        IABTCF_PublisherCustomPurposesConsents("IABTCF_PublisherCustomPurposesConsents", "publisher.customPurpose.consents", Type.STRING),
        IABTCF_PublisherCustomPurposesLegitimateInterests("IABTCF_PublisherCustomPurposesLegitimateInterests", "publisher.customPurpose.legitimateInterests", Type.STRING),

        // Google’s Additional Consent
        IABTCF_AddtlConsent("IABTCF_AddtlConsent", "addtlConsent", Type.STRING);

        private final String key;

        private final String jsonKey;

        private final Type type;

        Consent(final String key, final String jsonKey, Type type)
        {
            this.key = key;
            this.jsonKey = jsonKey;
            this.type = type;
        }

        public String getNestedJsonKey()
        {
            if (jsonKey.contains("."))
            {
                return jsonKey.substring(jsonKey.lastIndexOf(".") + 1);
            }

            return jsonKey;
        }
    }

    public enum ConsentRing
    {
        RingPublishing_Consents("RingPublishing_Consents", "consents"),
        RingPublishing_VendorsConsent("RingPublishing_VendorsConsent", "vendorsConsent");

        private final String key;

        private final String jsonKey;

        ConsentRing(final String key, final String jsonKey)
        {
            this.key = key;
            this.jsonKey = jsonKey;
        }
    }

}
