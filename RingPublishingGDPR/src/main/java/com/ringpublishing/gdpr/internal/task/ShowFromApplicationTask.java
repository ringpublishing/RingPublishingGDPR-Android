package com.ringpublishing.gdpr.internal.task;

import android.content.Context;
import android.util.Log;

import com.ringpublishing.gdpr.internal.android.ActivityLifecycleObserver;
import com.ringpublishing.gdpr.internal.callback.GDPRApplicationCallback;
import com.ringpublishing.gdpr.internal.view.FormView;

public class ShowFromApplicationTask
{
    private final String TAG = ShowFromApplicationTask.class.getSimpleName();

    private final ActivityLifecycleObserver activityLifecycleObserver;

    public ShowFromApplicationTask(ActivityLifecycleObserver activityLifecycleObserver)
    {
        this.activityLifecycleObserver = activityLifecycleObserver;
    }

    public void run(FormView formView, GDPRApplicationCallback ringPublishingGDPRApplicationCallback)
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
