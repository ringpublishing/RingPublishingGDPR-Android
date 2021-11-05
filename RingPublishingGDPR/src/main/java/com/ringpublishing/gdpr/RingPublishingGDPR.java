package com.ringpublishing.gdpr;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ringpublishing.gdpr.internal.api.Api;
import com.ringpublishing.gdpr.internal.model.RequestsState;
import com.ringpublishing.gdpr.internal.model.TenantConfiguration;
import com.ringpublishing.gdpr.internal.model.VerifyState;
import com.ringpublishing.gdpr.internal.storage.Storage;
import com.ringpublishing.gdpr.internal.task.ApiSynchronizationTask;
import com.ringpublishing.gdpr.internal.task.ConsentVerifyTask;
import com.ringpublishing.gdpr.internal.task.FetchConfigurationTask;
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

    @NonNull
    private final RequestsState requestsState = new RequestsState();

    @NonNull
    private final TenantConfiguration tenantConfiguration = new TenantConfiguration();

    @NonNull
    private final RingPublishingGDPRNotifier ringPublishingGDPRNotifier = new RingPublishingGDPRNotifier();

    @NonNull
    private RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener = (int errorCode, String errorMessage) -> {};

    private ApiSynchronizationTask apiSynchronizationTask;

    private FetchConfigurationTask fetchConfigurationTask;

    @Nullable
    private ConsentFormListener consentFormListener;

    private Api api;

    private RingPublishingGDPRUIConfig ringPublishingGDPRUIConfig;

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
     * <p>
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
     * <p>
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
    public void shouldShowConsentForm(@Nullable ConsentFormListener consentFormListener)
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
     *
     * @return true when initialize method is already called
     */
    public boolean isInitialized()
    {
        return initialized;
    }

    /**
     * Should GDPR apply in current context?
     * <p>
     * This property at module initialization (and before) has value saved from last app session.
     * This property will be populated with fresh value somewhere between:
     * - after module initialization
     * - before module calls one of methods with consents status,
     * either 'shouldShowConsentForm()' or 'onConsentsUpdated()'
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
    }

    /**
     * Create intent to open default consent screen
     *
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    public Intent createShowWelcomeScreenIntent(Context context)
    {
        return RingPublishingGDPRActivity.createShowWelcomeScreenIntent(context);
    }

    /**
     * Create intent to open settings consent screen
     *
     * @param context to create intent
     * @return Intent that can be used to startActivity
     */
    public Intent createShowSettingsScreenIntent(Context context)
    {
        return RingPublishingGDPRActivity.createShowSettingsScreenIntent(context);
    }


    /**
     * Add listener that informs application about saving or updating consents.
     *
     * @param ringPublishingGDPRListener listener to observe consents update
     */
    public void addRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRNotifier.addRingPublishingGDPRListener(ringPublishingGDPRListener);
    }

    /**
     * Remove listener that informs application about saving or updating consents.
     *
     * @param ringPublishingGDPRListener listener to observe consents update
     */
    public void removeRingPublishingGDPRListener(RingPublishingGDPRListener ringPublishingGDPRListener)
    {
        ringPublishingGDPRNotifier.removeRingPublishingGDPRListener(ringPublishingGDPRListener);
    }

    /**
     * Set listener for ring GDPRs errors
     * @param ringPublishingGDPROnErrorListener error listener  (int errorCode, @NonNull String errorMessage) -> {}
     */
    public void setRingPublishingGDPROnErrorListener(@NonNull RingPublishingGDPROnErrorListener ringPublishingGDPROnErrorListener)
    {
        this.ringPublishingGDPROnErrorListener = ringPublishingGDPROnErrorListener;
    }

    @NonNull
    FormViewImpl createFormView(@NonNull Context activityContext)
    {
        final FormViewImpl formViewImpl = new FormViewImpl(activityContext, api, tenantConfiguration, storage, ringPublishingGDPRNotifier);
        formViewImpl.setViewStyle(ringPublishingGDPRUIConfig);
        if (timeoutInSeconds > 0)
        {
            formViewImpl.setTimeoutInSeconds(timeoutInSeconds);
        }

        return formViewImpl;
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

        final Context context = application.getApplicationContext();
        this.api = new Api(context, tenantId, brandName, timeoutInSeconds, forcedGDPRApplies);
        this.storage = new Storage(context);
        this.ringPublishingGDPRUIConfig = ringPublishingGDPRUIConfig;
        this.apiSynchronizationTask = new ApiSynchronizationTask(requestsState, tenantConfiguration, storage, ringPublishingGDPROnErrorListener);
        this.fetchConfigurationTask = new FetchConfigurationTask(api, storage, requestsState, tenantConfiguration, ringPublishingGDPROnErrorListener);
        initialized = true;

        runApplicationStartWork(api);
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
                new ConsentVerifyTask(storage, api, requestsState, ringPublishingGDPROnErrorListener).run(() -> apiSynchronizationTask.run(consentFormListener));
            }
            else
            {
                requestsState.setVerifyState(VerifyState.ACTUAL);
            }
        }
    }

}

