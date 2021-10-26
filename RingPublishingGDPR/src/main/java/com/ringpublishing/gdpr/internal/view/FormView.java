package com.ringpublishing.gdpr.internal.view;

import android.content.Context;
import android.widget.LinearLayout;

import com.ringpublishing.gdpr.internal.callback.GDPRActivityCallback;

import androidx.annotation.Nullable;

public abstract class FormView extends LinearLayout
{

    public FormView(Context context)
    {
        super(context);
    }

    public abstract void showConsentsWelcomeScreen();

    public abstract void showConsentsSettingsScreen();

    public abstract void onFailure(String errorMessage);

    public abstract void setActivityCallback(@Nullable GDPRActivityCallback ringPublishingGDPRActivity);
}
