package com.ringpublishing.gdpr.internal.view;

import com.ringpublishing.gdpr.internal.cmp.CmpAction;
import com.ringpublishing.gdpr.internal.cmp.CmpAction.ActionType;
import com.ringpublishing.gdpr.internal.log.Logger;
import com.ringpublishing.gdpr.internal.task.TimeoutTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import androidx.annotation.NonNull;

public class FormViewController
{
    @NonNull
    private final List<String> actionsQueue = new ArrayList<>();

    @NonNull
    private final List<ActionType> waitingToCloseActions = new CopyOnWriteArrayList<>();

    @NonNull
    private final FormViewImpl formViewImpl;

    @NonNull
    private final TimeoutTask loadTimeoutTask = new TimeoutTask();

    @NonNull
    private final TimeoutTask submitTimeoutTask = new TimeoutTask();

    private final Logger log = Logger.get();


    public FormViewController(@NonNull FormViewImpl formView)
    {
        this.formViewImpl = formView;
    }

    public void setTimeoutInSeconds(int timeoutSeconds)
    {
        this.loadTimeoutTask.setTimeout(timeoutSeconds);
        this.submitTimeoutTask.setTimeout(timeoutSeconds);
    }

    void onDetach()
    {
        this.loadTimeoutTask.cancel();
        this.submitTimeoutTask.cancel();
    }

    void addAction(String action)
    {
        actionsQueue.add(action);
    }

    void executeWaitingActions()
    {
        log.info( "FormViewImpl. call executeWaitingActions() with action: " + actionsQueue.size());
        if (actionsQueue.isEmpty())
        {
            return;
        }

        for (String action : actionsQueue)
        {
            formViewImpl.performAction(action);
            log.info( "FormViewImpl call executeWaitingActions() with action: " + action);
        }

        actionsQueue.clear();
    }

    void loadCmpSite()
    {
        // The previous page can no longer answer a pending submit.
        clearWaitingActions();
        loadTimeoutTask.start(this::onLoadTimeout);
    }

    void showContent()
    {
        loadTimeoutTask.cancel();
    }

    void cancelLoadTimeout()
    {
        loadTimeoutTask.cancel();
    }

    private void onLoadTimeout()
    {
        log.warn("Loading cmp site timeout reached. Respond with failure callback");
        formViewImpl.onFailure("Loading cmp site timeout reached");
    }

    private void onSubmitTimeout()
    {
        log.warn("Consent data response timeout reached. Close form by error");
        formViewImpl.onSubmitTimeout();
    }

    public void callFormSubmittedActions()
    {
        waitingToCloseActions.clear();
        waitingToCloseActions.add(ActionType.GET_TC_DATA);
        waitingToCloseActions.add(ActionType.GET_COMPLETE_CONSENT_DATA);

        for (ActionType waitingToCloseAction : waitingToCloseActions)
        {
            formViewImpl.performAction(CmpAction.get(waitingToCloseAction));
        }

        submitTimeoutTask.start(this::onSubmitTimeout);
    }

    public boolean waitingActionFinish(ActionType action)
    {
        waitingToCloseActions.remove(action);
        if (waitingToCloseActions.isEmpty())
        {
            submitTimeoutTask.cancel();
            return true;
        }
        return false;
    }

    public boolean isWaitingFor(ActionType action)
    {
        return waitingToCloseActions.contains(action);
    }

    public void clearWaitingActions()
    {
        waitingToCloseActions.clear();
        submitTimeoutTask.cancel();
    }

}
