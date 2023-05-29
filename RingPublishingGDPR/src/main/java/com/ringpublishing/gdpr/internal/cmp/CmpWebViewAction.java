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
            Logger.get().error("Cmp site closeForm. Error: " + error);
            closeForm();
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL, error);
        }
        else
        {
            Logger.get().error("Cmp site error: " + error);
            formViewImpl.showError();
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.WEBVIEW_LOADING_FAIL, "User is offline. " + error);
        }
    }

    @Override
    public void onActionInAppTCData(String tcData, boolean success)
    {
        if (success)
        {
            Logger.get().info("Save TCData success");
            try
            {
                storage.saveTCData(tcData);
            }
            catch (JSONException e)
            {
                storage.clearAllConsentData();
                Logger.get().error("saveTCData fail!!" + e.getLocalizedMessage());
                closeForm();
                ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_VERIFY_CONSENTS_STATE, e.getLocalizedMessage());
            }
        }
        else
        {
            storage.clearAllConsentData();
            Logger.get().error("Save TCData fail");
            closeForm();
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_VERIFY_CONSENTS_STATE, "onActionInAppTCData() not success" + tcData);
        }

        CookieManager.getInstance().flush();

        boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_TC_DATA);
        if (closeForm)
        {
            closeForm();
            ringPublishingGDPRListener.onConsentsUpdated();
        }
    }

    @Override
    public void getCompleteConsentData(String error, String dlData)
    {
        if (TextUtils.isEmpty(error))
        {
            try
            {
                storage.saveConsentData(dlData);

                boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA);
                if (closeForm)
                {
                    closeForm();
                    ringPublishingGDPRListener.onConsentsUpdated();
                }
            }
            catch (JSONException e)
            {
                storage.clearAllConsentData();
                closeForm();
                Logger.get().error("Fail saving consent data" + e.getLocalizedMessage());
                ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_VERIFY_CONSENTS_STATE, "Parse dlData exception: " + e.getLocalizedMessage());
            }
        }
        else
        {
            storage.clearAllConsentData();
            closeForm();
            Logger.get().error("Save dlData fail.Error " + error);
            ringPublishingGDPRListener.onError(RingPublishingGDPRError.CANNOT_VERIFY_CONSENTS_STATE, "getCompleteConsentData error: " + error + " dlData:" + dlData);
        }

        CookieManager.getInstance().flush();
    }

    private void closeForm()
    {
        Logger.get().info("Cmp closeForm");
        storage.saveLastAPIConsentsCheckStatus(null);
        storage.setConsentOutdated(false);
        formViewImpl.hideForm();
    }

}
