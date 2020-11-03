package com.ringpublishing.gdpr.internal.storage;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StorageJsonParserTest
{

    private final String TAG = StorageJsonParserTest.class.getSimpleName();

    @Test
    public void getNestedJSONObject_WhenAccessChild_ThenHaveChildJSONObject()
    {
        String jsonString = TestResources.getDlData(getClass());

        String keywords = "consents.lpubconsent";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject nestedJSONObject = StorageJsonParser.getNestedJSONObject(keywords, jsonObject);

            JSONObject childJson = jsonObject.getJSONObject("consents");

            assertEquals(childJson.toString(), nestedJSONObject.toString());
        }
        catch (JSONException e)
        {
            assertNull(e);
            Log.e(TAG, "Test fail", e);
        }
    }

    @Test
    public void getNestedJSONObject_WhenAccessParent_ThenHaveParent()
    {
        String jsonString = TestResources.getDlData(getClass());

        String keywords = "consents";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject nestedJSONObject = StorageJsonParser.getNestedJSONObject(keywords, jsonObject);

            assertEquals(jsonObject.toString(), nestedJSONObject.toString());
        }
        catch (JSONException e)
        {
            assertNull(e);
            Log.e(TAG, "Test fail", e);
        }
    }

    @Test
    public void getNestedJSONObject_WhenAccessNonExistParent_ThenHaveParent()
    {
        String jsonString = TestResources.getDlData(getClass());

        String keywords = "nonexist";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject nestedJSONObject = StorageJsonParser.getNestedJSONObject(keywords, jsonObject);

            assertEquals(jsonObject.toString(), nestedJSONObject.toString());
        }
        catch (JSONException e)
        {
            assertNull(e);
            Log.e(TAG, "Test fail", e);
        }
    }

    @Test
    public void getNestedJSONObject_WhenAccessNonExistChild_ThenHaveNull()
    {
        String jsonString = TestResources.getDlData(getClass());

        String keywords = "consents.nonexist";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject nestedJSONObject = StorageJsonParser.getNestedJSONObject(keywords, jsonObject);

            assertNull(nestedJSONObject);
        }
        catch (JSONException e)
        {
            assertNull(e);
            Log.e(TAG, "Test fail", e);
        }
    }
}