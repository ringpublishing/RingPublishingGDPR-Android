package com.ringpublishing.gdpr;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ringpublishing.gdpr.internal.android.ActivityLifecycleObserver;
import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.callback.GDPRActivityCallback;
import com.ringpublishing.gdpr.internal.callback.GDPRApplicationCallback;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.model.VerifyState;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.task.ApiSynchronizationTask;
import com.ringpublishing.gdpr.internal.cmp.CmpWebViewAction;
import com.ringpublishing.gdpr.internal.task.ConsentVerifyTask;
import com.ringpublishing.gdpr.internal.task.FetchConfigurationTask;
import com.ringpublishing.gdpr.internal.task.ShowFromApplicationTask;
import com.ringpublishing.gdpr.internal.view.FormViewController;
import com.ringpublishing.gdpr.internal.view.FormViewImpl;

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

    private boolean initialized;

    private int timeoutInSeconds = BuildConfig.DEFAULT_TIMEOUT;

    private Storage storage;

    private final RequestsState requestsState = new RequestsState();

    private final TenantConfiguration tenantConfiguration = new TenantConfiguration();

    private FormViewImpl formViewImpl;

    private FormViewController formViewController;

    private CmpWebViewAction cmpActionCallbackCreator;

    private ApiSynchronizationTask apiSynchronizationTask;

    private ShowFromApplicationTask showFromApplicationTask;

    private FetchConfigurationTask fetchConfigurationTask;

    private ConsentFormListener consentFormListener;

    private GDPRApplicationCallback gdprApplicationCallback;

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
        initializeInternal(application, tenantId, brandName, ringPublishingGDPRUIConfig, null);
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
     * @param forcedGDPRApplies Determines if module was initialized with forced GDPR applies state
     */
    public void initialize(@NonNull final Application application,
                           @NonNull final String tenantId,
                           @NonNull final String brandName,
                           @NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig,
                           boolean forcedGDPRApplies)
    {
        initializeInternal(application, tenantId, brandName, ringPublishingGDPRUIConfig, forcedGDPRApplies);
    }

    /**
     * Use this method to check that RingPublishingGDPRActivity should be displayed
     * Call this method in first application Activity
     * Wait for callback in ConsentFormListener to decide that consent form should be displayed.
     */
    public void shouldShowConsentForm(ConsentFormListener consentFormListener)
    {
        this.consentFormListener = consentFormListener;

        if (consentFormListener == null)
        {
            return;
        }

        apiSynchronizationTask.run(consentFormListener);
    }

    /**
     * Remove ConsentFormListener listener reference
     */
    public void removeConsentFormListener()
    {
        this.consentFormListener = null;
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
     * Should GDPR apply in current context?
     *
     * This property at module initialization (and before) has value saved from last app session.
     * This property will be populated with fresh value somewhere between:
     * - after module initialization
     * - before module calls one of methods with consents status,
     *  either 'shouldShowConsentForm()' or 'onConsentsUpdated()'
     */
    public boolean isGDPRApplies()
    {
        return storage.isGDPRApplies();
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


    /**
     * Add listener that informs application about saving or updating consents.
     * @param ringPublishingGDPRListener listener to observe consents update
     */
    public void addRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        cmpActionCallbackCreator.addRingPublishingGDPRListener(ringPublishingGDPRListener);
    }

    /**
     * Remove listener that informs application about saving or updating consents.
     * @param ringPublishingGDPRListener listener to observe consents update
     */
    public void removeRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        cmpActionCallbackCreator.removeRingPublishingGDPRListener(ringPublishingGDPRListener);
    }

    @Nullable
    FormViewImpl createFormView(Context applicationContext)
    {
        final FormViewImpl formViewImpl = new FormViewImpl(applicationContext, formViewController, cmpActionCallbackCreator);
        cmpActionCallbackCreator.setFormViewImpl(formViewImpl);

        if (timeoutInSeconds > 0)
        {
            formViewImpl.setTimeoutInSeconds(timeoutInSeconds);
        }

        return formViewImpl;
    }

    void setActivityCallback(GDPRActivityCallback gdprActivityCallback)
    {
        cmpActionCallbackCreator.setGdprActivityCallback(gdprActivityCallback);
    }

    private void initializeInternal(@NonNull final Application application,
                                    @NonNull final String tenantId,
                                    @NonNull final String brandName,
                                    @NonNull final RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig,
                                    @Nullable Boolean forcedGDPRApplies)
    {
        if (initialized)
        {
            Log.w(TAG, "Second initialization ignored");
            return;
        }

        Context context = application.getApplicationContext();
        Api api = new Api(context, tenantId, brandName, timeoutInSeconds, forcedGDPRApplies);
        this.storage = new Storage(context);
        this.formViewController = new FormViewController(api, ringPublishingGDPRUIConfig);
        this.gdprApplicationCallback = createRingPublishingGDPRApplicationCallback();

        this.showFromApplicationTask = new ShowFromApplicationTask(new ActivityLifecycleObserver(application));
        this.cmpActionCallbackCreator = new CmpWebViewAction(this, storage);
        this.formViewImpl = createFormView(context);
        this.apiSynchronizationTask = new ApiSynchronizationTask(requestsState, tenantConfiguration, storage, () -> showFromApplicationTask.run(formViewImpl, gdprApplicationCallback));
        this.fetchConfigurationTask = new FetchConfigurationTask(api, storage, requestsState, tenantConfiguration, formViewController);
        initialized = true;

        runApplicationStartWork(api);
    }

    @NonNull
    private GDPRApplicationCallback createRingPublishingGDPRApplicationCallback()
    {
        return context -> {
            final Intent showWelcomeScreenIntent = RingPublishingGDPRActivity.createShowWelcomeScreenIntent(context);
            showWelcomeScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(showWelcomeScreenIntent);
        };
    }

    private void runApplicationStartWork(Api api)
    {
        requestsState.setIsLoading();

        fetchConfigurationTask.run(() -> apiSynchronizationTask.run(consentFormListener));

        if (storage.isConsentOutdated())
        {
            requestsState.setVerifyState(VerifyState.OUTDATED);
        }
        else
        {
            if (storage.didAskUserForConsents())
            {
                new ConsentVerifyTask(storage, api, requestsState).run(() -> apiSynchronizationTask.run(consentFormListener));
            }
            else
            {
                requestsState.setVerifyState(VerifyState.ACTUAL);
            }
        }
    }

}

