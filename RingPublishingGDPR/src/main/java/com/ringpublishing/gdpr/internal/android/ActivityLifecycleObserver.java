package com.ringpublishing.gdpr.internal.android;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

import com.ringpublishing.gdpr.RingPublishingGDPRActivity;

public class ActivityLifecycleObserver implements ActivityLifecycleCallbacks
{

    private final String formActivitySimpleName = RingPublishingGDPRActivity.class.getCanonicalName();

    private Activity foregroundActivity;

    private Runnable onFirstActivityStarted;

    public ActivityLifecycleObserver(Application application)
    {
        application.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle)
    {
        foregroundActivity = activity;
    }

    @Override
    public void onActivityStarted(Activity activity)
    {
        foregroundActivity = activity;
        runOnFirstActivityStart();
    }

    @Override
    public void onActivityResumed(Activity activity)
    {
        foregroundActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity)
    {
        clearCurrentForegroundActivity(activity);
    }

    @Override
    public void onActivityStopped(Activity activity)
    {
        clearCurrentForegroundActivity(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle)
    {
    }

    @Override
    public void onActivityDestroyed(Activity activity)
    {
        clearCurrentForegroundActivity(activity);
    }

    private void runOnFirstActivityStart()
    {
        if (!isActivityDisplayed() && onFirstActivityStarted != null)
        {
            onFirstActivityStarted.run();
            onFirstActivityStarted = null;
        }
    }

    private void clearCurrentForegroundActivity(Activity activity)
    {
        if (foregroundActivity != null && foregroundActivity.equals(activity))
        {
            foregroundActivity = null;
        }
    }

    public boolean isApplicationDisplayed()
    {
        return foregroundActivity != null;
    }

    public boolean isActivityDisplayed()
    {
        return isApplicationDisplayed() && formActivitySimpleName.equalsIgnoreCase(foregroundActivity.getClass().getSimpleName());
    }

    public void executeOnFirstActivityStarted(Runnable onFirstActivityStarted)
    {
        this.onFirstActivityStarted = onFirstActivityStarted;
    }
}

