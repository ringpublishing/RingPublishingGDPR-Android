package com.ringpublishing.gdpr.internal.cmp;

public final class CmpAction
{

    private CmpAction()
    {
    }

    public static String get(ActionType actionType)
    {
        switch (actionType)
        {
            case SHOW_WELCOME:
                return showWelcomeScreen();
            case SHOW_SETTINGS:
                return showSettingsScreen();
            case GET_TC_DATA:
                return getTCData();
            case GET_COMPLETE_CONSENT_DATA:
                return getCompleteConsentData();
        }
        return null;
    }

    private static String showWelcomeScreen()
    {
        return "window.dlApi.showConsentTool(null, function(result) { "
                + "Android.showWelcomeScreen();});";
    }

    private static String showSettingsScreen()
    {
        return "window.dlApi.showConsentTool(\"details\", function(result) { "
                + "Android.showSettingsScreen();"
                + "});";
    }

    private static String getTCData()
    {
        return "window.__tcfapi('getInAppTCData', 2, function(data, success) {  if(success === true) { Android.getInAppTCData(JSON.stringify(data), success) } });";
    }

    private static String getCompleteConsentData()
    {
        return "window.dlApi.getCompleteConsentData(function(error, data) { if(error !== true) { Android.getCompleteConsentData(error, JSON.stringify(data)) } });";
    }

    public enum ActionType
    {
        SHOW_WELCOME,
        SHOW_SETTINGS,
        GET_TC_DATA,
        GET_COMPLETE_CONSENT_DATA
    }

}
