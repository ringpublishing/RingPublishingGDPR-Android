package com.ringpublishing.gdpr.internal.storage;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class TestResources
{

    private static final String TAG = TestResources.class.getCanonicalName();

    public static String getJson(final Class sourceClass, String fileName)
    {
        final InputStream is = sourceClass.getClassLoader().getResourceAsStream(fileName);
        final Scanner json = new Scanner(is).useDelimiter("\\z");

        final String result = json.hasNext() ? json.next() : "";

        try
        {
            is.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Fail read test resource" + fileName, e);
        }

        return result;
    }

    public static String getDlData(final Class sourceClass)
    {
        return getJson(sourceClass, "dlData.json");
    }

    public static String getTcData(final Class sourceClass)
    {
        return getJson(sourceClass, "tcData.json");
    }
}
