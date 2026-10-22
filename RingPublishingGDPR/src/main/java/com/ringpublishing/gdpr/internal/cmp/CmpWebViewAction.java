package com.ringpublishing.gdpr.internal.cmp;

import android.text.TextUtils;
import android.webkit.CookieManager;

import com.ringpublishing.gdpr.RingPublishingGDPRError;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

import org.json.JSONException;

import androidx.annotation.NonNull;

public class CmpWebViewAction implements CmpWebViewActionCallback
{
    @NonNull
    private final Storage storage;

    @NonNull
    private final FormViewImpl formViewImpl;

    @NonNull
    private final RingPublishingGDPRListener ringPublishingGDPRListener;

    public CmpWebViewAction(@NonNull Storage storage, @NonNull RingPublishingGDPRListener ringPublishingGDPRListener, @NonNull FormViewImpl formViewImpl)
    {
        this.storage = storage;
        this.ringPublishingGDPRListener = ringPublishingGDPRListener;
        this.formViewImpl = formViewImpl;
    }

    @Override
    public void onActionLoaded()
    {
        Logger.get().info( "Cmp site is ready onActionLoaded");
        formViewImpl.post(formViewImpl::cmpReady);
    }

    @Override
    public void onActionComplete()
    {
        Logger.get().info( "Cmp site onActionComplete");
        formViewImpl.formSubmittedAction();
    }

    @Override
    public void onActionError(String error)
    {
        if (formViewImpl.isOnline())
        {
            closeFormByError("Error when try execute action in webview. Error: " + error);
        }
        else
        {
            Logger.get().error("User is offline. Error when try execute action in webview. Error: " + error);
            formViewImpl.showError();
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL, "User is offline. Error when try execute action in webview. Error: " + error);
        }
    }

    @Override
    public void onActionInAppTCData(String tcData, boolean success)
    {
        if (!formViewImpl.isWaitingFor(ActionType.GET_TC_DATA))
        {
            Logger.get().warn("Late TC data ignored. The form is not waiting for it");
            CookieManager.getInstance().flush();
            return;
        }

        if (!success)
        {
            storage.clearAllConsentData();
            formViewImpl.clearWaitingActions();
            closeFormByError("When receive tcData from javascript, result is not success. TcData is: " + tcData);
            CookieManager.getInstance().flush();
            return;
        }

        try
        {
            Logger.get().info("Try to save TCData from javascript");
            storage.saveTCData(tcData);
        }
        catch (JSONException e)
        {
            storage.clearAllConsentData();
            formViewImpl.clearWaitingActions();
            closeFormByError("When receive tcData from javascript and want to save TCData, " +
                    "then have JSONException error:" + e.getLocalizedMessage() + " TcData to parse is: " + tcData);
            CookieManager.getInstance().flush();
            return;
        }

        CookieManager.getInstance().flush();

        if (formViewImpl.waitingActionFinish(ActionType.GET_TC_DATA))
        {
            closeForm();
            ringPublishingGDPRListener.onConsentsUpdated();
        }
    }

    @Override
    public void getCompleteConsentData(String error, String dlData)
    {
        if (!formViewImpl.isWaitingFor(ActionType.GET_COMPLETE_CONSENT_DATA))
        {
            Logger.get().warn("Late complete consent data ignored. The form is not waiting for it");
            CookieManager.getInstance().flush();
            return;
        }

        if (!TextUtils.isEmpty(error))
        {
            storage.clearAllConsentData();
            formViewImpl.clearWaitingActions();
            Logger.get().error("Save dlData fail.Error " + error);
            closeFormByError("When try to get complete consent data, then have error: " + error + " dlData:" + dlData);
            CookieManager.getInstance().flush();
            return;
        }

        try
        {
            storage.saveConsentData(dlData);
        }
        catch (JSONException e)
        {
            storage.clearAllConsentData();
            formViewImpl.clearWaitingActions();
            Logger.get().error("Fail saving consent data" + e.getLocalizedMessage());
            closeFormByError("When try to save consent data, then have JSONException with error: "
                    + e.getLocalizedMessage() + " Parsed data is: " + dlData);
            CookieManager.getInstance().flush();
            return;
        }

        CookieManager.getInstance().flush();

        if (formViewImpl.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA))
        {
            closeForm();
            ringPublishingGDPRListener.onConsentsUpdated();
        }
    }

    public void onSubmitTimeout()
    {
        storage.clearAllConsentData();
        formViewImpl.clearWaitingActions();
        closeFormByError("Consent data response timeout reached");
    }

    private void closeFormByError(String errorMessage)
    {
        Logger.get().error("Close form by error. Error message: " + errorMessage);

        ringPublishingGDPRListener.onError(RingPublishingGDPRError.CLOSE_FORM_WITH_ERROR, errorMessage);
        formViewImpl.hideForm();
    }

    private void closeForm()
    {
        Logger.get().info("Cmp closeForm");
        storage.saveLastAPIConsentsCheckStatus(null);
        storage.setConsentOutdated(false);
        formViewImpl.hideForm();
    }

}
