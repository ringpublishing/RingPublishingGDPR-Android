package com.ringpublishing.gdpr.internal.cmp;

import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CmpActionTest
{

    @Test
    public void testGetActionWelcome_WhenGetActionWelcome_ThenCorrectValue()
    {
        String expected = "window.dlApi.showConsentTool(null, function(result) { Android.showWelcomeScreen();});";
        String action = CmpAction.get(ActionType.SHOW_WELCOME);
        assertEquals(expected, action);
    }


    @Test
    public void testGetActionSettings_WhenGetActionSettings_ThenCorrectValue()
    {
        String expected = "window.dlApi.showConsentTool(\"details\", function(result) { Android.showSettingsScreen();});";
        String action = CmpAction.get(ActionType.SHOW_SETTINGS);
        assertEquals(expected, action);
    }


}