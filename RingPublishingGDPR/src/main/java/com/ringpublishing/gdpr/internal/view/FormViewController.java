package com.ringpublishing.gdpr.internal.view;

import android.util.Log;

import com.ringpublishing.gdpr.internal.cmp.CmpAction;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.task.TimeoutTask;
import com.ringpublishing.gdpr.internal.task.TimeoutTask.TimeoutCallback;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class FormViewController implements TimeoutCallback
{

    private final String TAG = FormViewController.class.getCanonicalName();

    @NonNull
    private final List<String> actionsQueue = new ArrayList<>();

    @NonNull
    private final List<ActionType> waitingToCloseActions = new ArrayList<>();

    @NonNull
    private final FormViewImpl formViewImpl;

    @NonNull
    private final TimeoutTask timeoutTask = new TimeoutTask();

    public FormViewController(@NonNull FormViewImpl formView)
    {
        this.formViewImpl = formView;
    }

    public void setTimeoutInSeconds(int timeoutSeconds)
    {
        this.timeoutTask.setTimeout(timeoutSeconds);
    }

    void onDetach()
    {
        this.timeoutTask.cancel();
    }

    void addAction(String action)
    {
        actionsQueue.add(action);
    }

    void executeWaitingActions()
    {
        Log.i(TAG, "FormViewImpl. call executeWaitingActions() action: " + actionsQueue.size());
        if (actionsQueue.isEmpty())
        {
            return;
        }

        for (String action : actionsQueue)
        {
            formViewImpl.performAction(action);
            Log.i(TAG, "FormViewImpl.executeWaitingActions() action: " + action);
        }

        actionsQueue.clear();
    }

    void loadCmpSite()
    {
        timeoutTask.start(this);
    }

    void showContent()
    {
        timeoutTask.cancel();
    }

    @Override
    public void onTimeout()
    {
        Log.w(TAG, "Loading cmp site timeout");
        formViewImpl.onFailure("Loading cmp site timeout");
    }

    public void callFormSubmittedActions()
    {
        waitingToCloseActions.add(ActionType.GET_TC_DATA);
        waitingToCloseActions.add(ActionType.GET_COMPLETE_CONSENT_DATA);

        for (ActionType waitingToCloseAction : waitingToCloseActions)
        {
            formViewImpl.performAction(CmpAction.get(waitingToCloseAction));
        }

        timeoutTask.start(this);
    }

    public boolean waitingActionFinish(ActionType action)
    {
        waitingToCloseActions.remove(action);
        if (waitingToCloseActions.isEmpty())
        {
            timeoutTask.cancel();
            return true;
        }
        return false;
    }

}
