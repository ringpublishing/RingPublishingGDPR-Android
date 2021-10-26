package com.ringpublishing.gdpr.internal.cmp;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import com.ringpublishing.gdpr.RingPublishingGDPRNotifier;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

import org.json.JSONException;

import androidx.annotation.NonNull;

public class CmpWebViewAction implements CmpWebViewActionCallback
{

    private final String TAG = CmpWebViewAction.class.getCanonicalName();

    @NonNull
    private final Storage storage;

    @NonNull
    private final FormViewImpl formViewImpl;

    @NonNull
    private final RingPublishingGDPRNotifier ringPublishingGDPRNotifier;

    public CmpWebViewAction(@NonNull Storage storage, @NonNull RingPublishingGDPRNotifier ringPublishingGDPRNotifier, @NonNull FormViewImpl formViewImpl)
    {
        this.storage = storage;
        this.ringPublishingGDPRNotifier = ringPublishingGDPRNotifier;
        this.formViewImpl = formViewImpl;
    }

    @Override
    public void onActionLoaded()
    {
        Log.i(TAG, "Cmp site is ready");
        formViewImpl.post(formViewImpl::cmpReady);
    }

    @Override
    public void onActionComplete()
    {
        formViewImpl.formSubmittedAction();
    }

    @Override
    public void onActionError(String error)
    {
        Log.w(TAG, "Error: " + error);
        if (formViewImpl.isOnline())
        {
            closeForm();
        }
        else
        {
            formViewImpl.showError();
        }
    }

    @Override
    public void onActionInAppTCData(String tcData, boolean success)
    {
        if (success)
        {
            try
            {
                storage.saveTCData(tcData);
            }
            catch (JSONException e)
            {
                storage.clearAllConsentData();
                Log.e(TAG, "saveTCData fail!!", e);
            }
        }
        else
        {
            storage.clearAllConsentData();
            Log.e(TAG, "Save TCData fail");
        }

        CookieManager.getInstance().flush();

        boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_TC_DATA);
        if (closeForm)
        {
            closeForm();
            ringPublishingGDPRNotifier.notifyConsentsUpdated();
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
            }
            catch (JSONException e)
            {
                storage.clearAllConsentData();
                Log.e(TAG, "Fail saving consent data", e);
            }
        }
        else
        {
            storage.clearAllConsentData();
            Log.e(TAG, "Save dlData fail");
        }

        CookieManager.getInstance().flush();

        boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA);
        if (closeForm)
        {
            closeForm();
            ringPublishingGDPRNotifier.notifyConsentsUpdated();
        }
    }

    private void closeForm()
    {
        storage.saveLastAPIConsentsCheckStatus(null);
        storage.setConsentOutdated(false);
        formViewImpl.hideForm();
    }

}
