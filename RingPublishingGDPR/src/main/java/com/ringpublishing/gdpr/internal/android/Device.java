package com.ringpublishing.gdpr.internal.android;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;


public final class Device
{

    private static final int TABLET_SIZE_MIN_INCH = 7;

    private Device()
    {
    }

    public static boolean isTablet()
    {
        return isLargeLayoutSize() && isDeviceResolutionGreaterThanTablets();
    }

    private static boolean isLargeLayoutSize()
    {
        return (Resources.getSystem().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private static boolean isDeviceResolutionGreaterThanTablets()
    {
        try
        {
            DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

            float yInches = displayMetrics.heightPixels / displayMetrics.ydpi;
            float xInches = displayMetrics.widthPixels / displayMetrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);

            if (diagonalInches >= TABLET_SIZE_MIN_INCH)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception e)
        {
            Log.e("User-Agent", "Device resolution error", e);
            return false;
        }
    }
}
