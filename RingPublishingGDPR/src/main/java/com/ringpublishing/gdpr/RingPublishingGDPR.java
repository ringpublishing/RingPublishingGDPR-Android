package com.ringpublishing.gdpr;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.ringpublishing.gdpr.internal.android.ActivityLifecycleObserver;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.api.Api.VerifyApiCallback;
import com.ringpublishing.gdpr.internal.callback.RingPublishingGDPRActivityCallback;
import com.ringpublishing.gdpr.internal.callback.RingPublishingGDPRApplicationCallback;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewActionCallback;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.view.FormView;
import com.ringpublishing.gdpr.internal.view.FormViewController;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Main class of RingPublishing GDPR Sdk.
 * Should be initialized on application start with configuration.
 * On first activity user should check that should show consent form and wait for result or go forward with initialization.
 * More information in README.md
 */
public final class RingPublishingGDPR
{

    private static final String TAG = RingPublishingGDPR.class.getCanonicalName();

    private static RingPublishingGDPR instance;

    private FormViewImpl formViewImpl;

    private Storage storage;

    private Api api;

    private ActivityLifecycleObserver activityLifecycleObserver;

    private RingPublishingGDPRActivityCallback ringPublishingGDPRActivityCallback;

    private RingPublishingGDPRApplicationCallback ringPublishingGDPRApplicationCallback;

    private FormViewController formViewController;

    private boolean initialized;

    private int timeoutInSeconds;

    private boolean gdprApplies = true;

    private final List<RingPublishingGDPRListener> ringPublishingGDPRListeners = new ArrayList<>();

    private RingPublishingGDPR()
    {
    }

    public static synchronized RingPublishingGDPR getInstance()
    {
        if (instance == null)
        {
            instance = new RingPublishingGDPR();
        }

        return instance;
    }

    /**
     * Initialization point of SDK with configuration.
     * Should be called once on application start.
     *
     * When is called first time, just initialize sdk, because we known that Consent View should be displayed.
     * On next application launches, call this method automatically asynchronously verify that consents are actual.
     * When consents needs to be updated, then using application context, open automatically Consent View in new Activity
     * On case when application will be put to background during this check, open Consent View will be scheduled to next application launch.
     * When your activity already opened consent view by RingPublishingGDPRActivity, then asynchronously verification will not display it second time.
     *
     * @param application Reference to android application object
     * @param tenantId Identifier of application send in request for Consent configuration to Ring API. Example "1234"
     * @param brandName Name of application send to Consent API. Using this parameter Consent view style can be customized
     * @param ringPublishingGDPRUIConfig UI configuration for TypeFace and theme. Styles error screen.
     */
    public void initialize(@NonNull final Application application,
                           @NonNull final String tenantId,
                           @NonNull final String brandName,
                           @NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig)
    {
        initialize(true, application, tenantId, brandName, ringPublishingGDPRUIConfig);
    }

    /**
     * Initialization point of SDK with configuration.
     * Should be called once on application start.
     *
     * When is called first time, just initialize sdk, because we known that Consent View should be displayed.
     * On next application launches, call this method automatically asynchronously verify that consents are actual.
     * When consents needs to be updated, then using application context, open automatically Consent View in new Activity
     * On case when application will be put to background during this check, open Consent View will be scheduled to next application launch.
     * When your activity already opened consent view by RingPublishingGDPRActivity, then asynchronously verification will not display it second time.
     *
     * @param gdprApplies Does GDPR applies in current context? When true, you can use also constructor without this parameter.
     * @param application Reference to android application object
     * @param tenantId Identifier of application send in request for Consent configuration to Ring API. Example "1234"
     * @param brandName Name of application send to Consent API. Using this parameter Consent view style can be customized
     * @param ringPublishingGDPRUIConfig UI configuration for TypeFace and theme. Styles error screen.
     */
    public void initialize(boolean gdprApplies, @NonNull final Application application,
                           @NonNull final String tenantId,
                           @NonNull final String brandName,
                           @NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig)
    {
        if (initialized)
        {
            Log.w(TAG, "Second initialization ignored");
            return;
        }

        this.gdprApplies = gdprApplies;
        final Context context = application.getApplicationContext();
        this.api = new Api(context, tenantId, brandName);
        this.storage = new Storage(context);
        this.formViewController = new FormViewController(api, ringPublishingGDPRUIConfig);
        this.activityLifecycleObserver = new ActivityLifecycleObserver(application);
        this.ringPublishingGDPRApplicationCallback = createRingPublishingGDPRApplicationCallback();
        initialized = true;

        storage.configureGDPRApplies(gdprApplies);

        if (storage.didAskUserForConsents() && !isOutdated() && gdprApplies)
        {
            verifyConsents(context);
        }
    }

    /**
     * Use this method to check that RingPublishingGDPRActivity should be displayed
     * @return true when Consent View should be displayed. It could be case when Consent View was never displayed, or is outdated.
     * When gdpr not apply for your context, this method will return false
     */
    public boolean shouldShowConsentForm()
    {
        if(!gdprApplies)
        {
            return false;
        }
        return (!storage.didAskUserForConsents() || isOutdated());
    }

    /**
     * This method can be used on application services that can start before application object is initialized
     * to make sure that RingPublishingGDPR is initialized already.
     * @return true when initialize method is already called
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Clear all Consents values saved in default shared preferences.
     */
    public void clearConsentsData()
    {
        storage.clearAllConsentData();
    }

    /**
     * Returns boolean value which determines whether consent for vendors and theirs purposes for processing data was established
     */
    public boolean areVendorConsentsGiven()
    {
        return storage.getInt("RingPublishing_VendorsConsent") == 1;
    }

    /**
     * Timeout for network request
     * Use this only in case when default timeout is to small.
     *
     * @param timeoutInSeconds is time that application waiting to finish all network communicatin during once user action. Default is 10 seconds
     */
    public void setNetworkTimeout(final int timeoutInSeconds)
    {
        this.timeoutInSeconds = timeoutInSeconds;
        if (formViewImpl != null)
        {
            formViewImpl.setTimeoutInSeconds(timeoutInSeconds);
        }
    }

    /**
     * Create intent to open default consent screen
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    public Intent createShowWelcomeScreenIntent(Context context)
    {
        return RingPublishingGDPRActivity.createShowWelcomeScreenIntent(context);
    }

    /**
     * Create intent to open settings consent screen
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    public Intent createShowSettingsScreenIntent(Context context)
    {
        return RingPublishingGDPRActivity.createShowSettingsScreenIntent(context);
    }

    public void addRingPublishingGDPRListeners(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.add(ringPublishingGDPRListener);
    }

    public void removeRingPublishingGDPRListeners(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRListeners.remove(ringPublishingGDPRListener);
    }

    void notifyConsentsUpdated()
    {
        for (RingPublishingGDPRListener listener: ringPublishingGDPRListeners)
        {
            listener.onConsentsUpdated();
        }
    }

    @Nullable
    FormView getFormView(Context applicationContext)
    {
        if (formViewImpl == null)
        {
            formViewImpl = new FormViewImpl(applicationContext, formViewController, createCmpWebViewCallback(), this);
            if (timeoutInSeconds > 0)
            {
                formViewImpl.setTimeoutInSeconds(timeoutInSeconds);
            }
        }

        return formViewImpl;
    }

    void setActivityCallback(RingPublishingGDPRActivityCallback ringPublishingGDPRActivityCallback)
    {
        this.ringPublishingGDPRActivityCallback = ringPublishingGDPRActivityCallback;
    }

    @NotNull
    private RingPublishingGDPRApplicationCallback createRingPublishingGDPRApplicationCallback()
    {
        return context -> {
            final Intent showWelcomeScreenIntent = RingPublishingGDPRActivity.createShowWelcomeScreenIntent(context);
            showWelcomeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showWelcomeScreenIntent);
        };
    }

    private boolean isOutdated()
    {
        boolean outdated = storage.isOutdated();
        Log.i(TAG, "isOutdated: " + outdated);
        return outdated;
    }

    private void verifyConsents(@NonNull Context applicationContext)
    {
        final Map<String, String> consents = storage.getRingConsents();

        if (consents == null || consents.isEmpty())
        {
            Log.w(TAG, "Fail verify consents. Consents are empty");
            return;
        }

        api.verify(consents, new VerifyApiCallback()
        {
            @Override
            public void onOutdated(String rawStatus)
            {
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                storage.setOutdated(true);
                FormView formView = getFormView(applicationContext);
                showFromApplication(formView);
            }

            @Override
            public void onActual(String rawStatus)
            {
                storage.saveLastAPIConsentsCheckStatus(rawStatus);
                storage.setOutdated(false);
            }

            @Override
            public void onFail(String status)
            {
                storage.saveLastAPIConsentsCheckStatus(status);
            }
        });
    }

    @NotNull
    private CmpWebViewActionCallback createCmpWebViewCallback()
    {
        return new CmpWebViewActionCallback()
        {
            @Override
            public void onActionLoaded()
            {
                Log.i(TAG, "Cmp site is ready");
                formViewImpl.post(() -> formViewImpl.cmpReady());
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
                        clearConsentsData();
                        Log.e(TAG, "saveTCData fail!!", e);
                    }

                }
                else
                {
                    clearConsentsData();
                    Log.e(TAG, "Save TCData fail");
                }

                boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_TC_DATA);
                if (closeForm)
                {
                    closeForm();
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
                        clearConsentsData();
                        Log.e(TAG, "Fail saving consent data", e);
                    }
                }
                else
                {
                    clearConsentsData();
                    Log.e(TAG, "Save dlData fail");
                }

                boolean closeForm = formViewImpl.waitingActionFinish(ActionType.GET_COMPLETE_CONSENT_DATA);
                if (closeForm)
                {
                    closeForm();
                }
            }
        };
    }

    void closeForm()
    {
        storage.saveLastAPIConsentsCheckStatus(null);
        storage.setOutdated(false);
        ringPublishingGDPRActivityCallback.hide(formViewImpl);
    }

    private void showFromApplication(FormView formView)
    {
        if (formView == null)
        {
            Log.e(TAG, "Form view is null");
            return;
        }

        if (activityLifecycleObserver == null)
        {
            Log.e(TAG, "Activity Lifecycle Observer is null");
        }

        final Context context = formView.getContext();
        if (context == null)
        {
            Log.e(TAG, "Context form view is null");
            return;
        }

        if (activityLifecycleObserver.isApplicationDisplayed())
        {
            if (activityLifecycleObserver.isActivityDisplayed())
            {
                Log.i(TAG, "Activity is displayed no need to open it again");
            }
            else
            {
                if (ringPublishingGDPRApplicationCallback != null)
                {
                    Log.i(TAG, "Start activity");
                    ringPublishingGDPRApplicationCallback.startActivity(context);
                }
                else
                {
                    Log.i(TAG, "Starter to open async after check outdated is not set.");
                }
            }
        }
        else
        {
            Log.i(TAG, "No activity on top we need to ad activity to queue and open it after start your first activity");
            activityLifecycleObserver.executeOnFirstActivityStarted(() -> ringPublishingGDPRApplicationCallback.startActivity(context));
        }
    }

}

