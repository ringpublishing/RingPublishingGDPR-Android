package com.ringpublishing.gdpr.internal.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.ringpublishing.gdpr.internal.android.Device;

public class UserAgent
{

    private static final String USER_AGENT_NON_TABLET_SUFFIX = "Mobile ";

    private static final String DEFAULT_APP_NAME = "App";

    private static final String DEFAULT_VERSION = "0.0.0";

    public static String getInterceptorHeader(Context context)
    {
        try
        {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ApplicationInfo aInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            return getInterceptorHeader(getAppName(context, aInfo), pInfo.versionName);
        }
        catch (PackageManager.NameNotFoundException ex)
        {
            return getInterceptorHeader(DEFAULT_APP_NAME, DEFAULT_VERSION);
        }
    }

    private static String getAppName(Context context, ApplicationInfo aInfo)
    {
        String result = "App";
        CharSequence nonLocalizedLabel = aInfo.nonLocalizedLabel;
        if (nonLocalizedLabel != null)
        {
            result = nonLocalizedLabel.toString();
        }
        else if (aInfo.labelRes != 0)
        {
            result = context.getString(aInfo.labelRes);
        }
        else if (aInfo.packageName != null)
        {
            result = aInfo.packageName;
        }
        return result;
    }

    public static String getInterceptorHeaderWithDefaultName(String versionName)
    {
        return getInterceptorHeader(DEFAULT_APP_NAME, versionName);
    }

    public static String getInterceptorHeaderWithDefaultVersion(String appName)
    {
        return getInterceptorHeader(appName, DEFAULT_VERSION);
    }

    public static String getInterceptorHeader(String appName, String versionName)
    {
        final String tabletInfo = Device.isTablet() ? "" : USER_AGENT_NON_TABLET_SUFFIX;
        return String.format("%s %sRingPublishing %s/%s", System.getProperty("http.agent"), tabletInfo, appName, versionName);
    }

}

