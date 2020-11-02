package com.ringpublishing.gdpr.internal.storage;

import org.json.JSONException;
import org.json.JSONObject;

class StorageJsonParser
{

    static JSONObject getNestedJSONObject(String keyWithDots, JSONObject tcDataJson) throws JSONException
    {
        String[] keyParts = keyWithDots.split("\\.");

        JSONObject resultJson = tcDataJson;

        for (int i = 1; i < keyParts.length; i++)
        {
            if (resultJson.has(keyParts[i - 1]))
            {
                resultJson = resultJson.getJSONObject(keyParts[i - 1]);
                if (!resultJson.has(keyParts[i]))
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }

        return resultJson;
    }

}
