package com.ringpublishing.gdpr.internal.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

import androidx.preference.PreferenceManager;

public class Preferences
{

    private static final String TAG = Preferences.class.getSimpleName();

    private SharedPreferences preferences;

    public Preferences(Context context)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getBoolean(String key, boolean defValue)
    {
        return preferences.getBoolean(key, defValue);
    }

    public void setBoolean(String key, boolean value)
    {
        Log.i(TAG, "Save Boolean preference: " + key + " value:" + value);
        preferences.edit().putBoolean(key, value).apply();
    }

    public void setString(String key, String value)
    {
        Log.i(TAG, "Save String preference: " + key + " value:" + value);
        preferences.edit().putString(key, value).apply();
    }

    public void setObject(String key, Object value)
    {
        if (value instanceof String)
        {
            setString(key, (String) value);
        }
        else if (value instanceof Integer)
        {
            setInt(key, (Integer) value);
        }
        else if (value instanceof Boolean)
        {
            setBoolean(key, (Boolean) value);
        }
        else if (value instanceof JSONObject)
        {
            final String jsonString = value.toString();
            if (!TextUtils.isEmpty(jsonString) && jsonString.length() > 2)
            {
                setString(key, jsonString);
            }
        }
        else if (value instanceof JSONArray)
        {
            final String jsonString = value.toString();
            if (!TextUtils.isEmpty(jsonString) && jsonString.length() > 2)
            {
                setString(key, value.toString());
            }
        }
        else
        {
            Log.e(TAG, "Unsupported object to save. Fail. Key" + key + " value " + value);
        }
    }

    public String getString(String key)
    {
        return preferences.getString(key, "");
    }

    public int getInt(String key)
    {
        return preferences.getInt(key, 0);
    }

    public void setInt(String key, int value)
    {
        Log.i(TAG, "Save Int preference: " + key + " value:" + value);
        preferences.edit().putInt(key, value).apply();
    }

    protected Set<String> getFilteredKeys(String prefix)
    {
        Set<String> resultKeys = new TreeSet<>();

        Set<String> allKeys = preferences.getAll().keySet();
        for (String key : allKeys)
        {
            if (key.startsWith(prefix))
            {
                resultKeys.add(key);
            }
        }
        return resultKeys;
    }


    protected void remove(String key)
    {
        preferences.edit().remove(key).apply();
    }

    protected void removeAll(Set<String> filteredKeys)
    {
        for (String key : filteredKeys)
        {
            remove(key);
        }
    }

    protected void removeAllByPrefix(String prefix)
    {
        final Set<String> filteredKeys = getFilteredKeys(prefix);
        removeAll(filteredKeys);
    }
}
