package com.ringpublishing.gdpr.internal.cmp;

import android.util.Log;
import android.webkit.JavascriptInterface;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import static com.ringpublishing.gdpr.internal.cmp.CmpEvent.valueOf;

public class CmpWebViewJavaScriptInterface
{

    private static final String TAG = CmpWebViewJavaScriptInterface.class.getCanonicalName();

    private static final String TD_DATA_KEY_EVENT_STATUS = "eventStatus";

    private final CmpWebViewActionCallback webViewCallback;

    CmpWebViewJavaScriptInterface(@NonNull CmpWebViewActionCallback webViewCallback)
    {
        this.webViewCallback = webViewCallback;
    }

    @JavascriptInterface
    public void onTcfEvent(String tcData)
    {
        try
        {
            final JSONObject jsonObject = new JSONObject(tcData);
            String eventStatus = jsonObject.getString(TD_DATA_KEY_EVENT_STATUS);

            switch (valueOf(eventStatus))
            {
                case tcloaded:
                {
                    Log.i(TAG, "Event: TC_LOADED");
                    webViewCallback.onActionLoaded();
                    break;
                }
                case cmpuishown:
                {
                    Log.i(TAG, "Event: SHOWN");
                    webViewCallback.onActionLoaded();
                    break;
                }
                case useractioncomplete:
                {
                    webViewCallback.onActionComplete();
                    break;
                }
                default:
                {
                    Log.w(TAG, "Event: WARNING UNKNOWN EVENT" + eventStatus);
                }
            }
        }
        catch (JSONException e)
        {
            Log.w(TAG, "Event Error parsing tcData: " + tcData, e);
        }
    }


    @JavascriptInterface
    public void onError(String error)
    {
        Log.i(TAG, "Error" + error);
        webViewCallback.onActionError(error);
    }

    @JavascriptInterface
    public void getInAppTCData(String tcData, boolean success)
    {
        Log.i(TAG, "getInAppTCData" + tcData);
        webViewCallback.onActionInAppTCData(tcData, success);
    }

    @JavascriptInterface
    public void getCompleteConsentData(String error, String dlData)
    {
        Log.i(TAG, "getCompleteConsentData(error=" + error + ",data=" + dlData);
        webViewCallback.getCompleteConsentData(error, dlData);
    }

    @JavascriptInterface
    public void showWelcomeScreen()
    {
        Log.i(TAG, "showWelcomeScreen call success");
    }

    @JavascriptInterface
    public void showSettingsScreen()
    {
        Log.i(TAG, "showSettingsScreen call success");
    }

}
