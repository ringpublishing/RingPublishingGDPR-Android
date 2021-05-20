package com.ringpublishing.gdpr.internal.cmp;

import android.text.TextUtils;
import android.util.Log;

import com.ringpublishing.gdpr.RingPublishingGDPR;
import com.ringpublishing.gdpr.RingPublishingGDPRListener;
import com.ringpublishing.gdpr.internal.callback.GDPRActivityCallback;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmpWebViewAction implements CmpWebViewActionCallback
{

    private final String TAG = CmpWebViewAction.class.getSimpleName();

    private final RingPublishingGDPR ringPublishingGDPR;

    private final Storage storage;

    private FormViewImpl formViewImpl;

    private GDPRActivityCallback gdprActivityCallback;

    private final List<RingPublishingGDPRListener> ringPublishingGDPRListeners = Collections.synchronizedList(new ArrayList<>());

    public CmpWebViewAction(RingPublishingGDPR ringPublishingGDPR, Storage storage)
    {
        this.ringPublishingGDPR = ringPublishingGDPR;
        this.storage = storage;
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
        if(formViewImpl.isOnline())
        {
            closeForm(gdprActivityCallback);
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
                ringPublishingGDPR.clearConsentsData();
                Log.e(TAG, "saveTCData fail!!", e);
            }
        }
        else
        {
            ringPublishingGDPR.clearConsentsData();
            Log.e(TAG, "Save TCData fail");
        }

        boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_TC_DATA);
        if (closeForm)
        {
            closeForm(gdprActivityCallback);
            notifyConsentsUpdated();
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
                ringPublishingGDPR.clearConsentsData();
                Log.e(TAG, "Fail saving consent data", e);
            }
        }
        else
        {
            ringPublishingGDPR.clearConsentsData();
            Log.e(TAG, "Save dlData fail");
        }

        boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA);
        if (closeForm)
        {
            closeForm(gdprActivityCallback);
            notifyConsentsUpdated();
        }
    }

    private void closeForm(GDPRActivityCallback gdprActivityCallback)
    {
        storage.saveLastAPIConsentsCheckStatus(null);
        storage.setConsentOutdated(false);
        if(gdprActivityCallback != null)
        {
            gdprActivityCallback.hideForm();
        }
    }

    private void notifyConsentsUpdated()
    {
        if (ringPublishingGDPRListeners != null)
        {
            synchronized (ringPublishingGDPRListeners)
            {
                for (RingPublishingGDPRListener listener: ringPublishingGDPRListeners)
                {
                    listener.onConsentsUpdated();
                }
            }
        }
    }

    public void setGdprActivityCallback(GDPRActivityCallback gdprActivityCallback)
    {
        this.gdprActivityCallback = gdprActivityCallback;
    }

    public void setFormViewImpl(FormViewImpl formViewImpl)
    {
        this.formViewImpl = formViewImpl;
    }

    public void addRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.add(ringPublishingGDPRListener);
    }

    public void removeRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.remove(ringPublishingGDPRListener);
    }
}
