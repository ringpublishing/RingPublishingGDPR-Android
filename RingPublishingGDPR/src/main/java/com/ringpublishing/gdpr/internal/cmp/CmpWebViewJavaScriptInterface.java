package com.ringpublishing.gdpr.internal.cmp;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.ringpublishing.gdpr.internal.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

import static com.ringpublishing.gdpr.internal.cmp.CmpEvent.valueOf;

public class CmpWebViewJavaScriptInterface
{

    private static final String TAG = CmpWebViewJavaScriptInterface.class.getCanonicalName();

    private static final String TD_DATA_KEY_EVENT_STATUS = "eventStatus";

    private final CmpWebViewActionCallback webViewCallback;

    private final Logger log = Logger.get();

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
                    log.info( "Event: TC_LOADED");
                    webViewCallback.onActionLoaded();
                    break;
                }
                case cmpuishown:
                {
                    log.info( "Event: SHOWN");
                    webViewCallback.onActionLoaded();
                    break;
                }
                case useractioncomplete:
                {
                    log.info( "Event: USER ACTION COMPLETTE");
                    webViewCallback.onActionComplete();
                    break;
                }
                default:
                {
                    log.warn("Event: WARNING UNKNOWN EVENT" + eventStatus);
                }
            }
        }
        catch (JSONException e)
        {
            log.warn("Event Error parsing tcData: " + tcData + e.getLocalizedMessage());
        }
    }

    @JavascriptInterface
    public void onError(String error)
    {
        log.info( "Cmp WebView Error" + error);
        webViewCallback.onActionError(error);
    }

    @JavascriptInterface
    public void getInAppTCData(String tcData, boolean success)
    {
        log.info( "getInAppTCData" + tcData);
        webViewCallback.onActionInAppTCData(tcData, success);
    }

    @JavascriptInterface
    public void getCompleteConsentData(String error, String dlData)
    {
        log.info( "getCompleteConsentData(error=" + error + ",data=" + dlData);
        webViewCallback.getCompleteConsentData(error, dlData);
    }

    @JavascriptInterface
    public void showWelcomeScreen()
    {
        log.info( "showWelcomeScreen call success");
    }

    @JavascriptInterface
    public void showSettingsScreen()
    {
        log.info( "showSettingsScreen call success");
    }

}
